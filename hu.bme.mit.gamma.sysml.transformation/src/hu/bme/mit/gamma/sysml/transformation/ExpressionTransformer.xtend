package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.Expression
import hu.bme.mit.gamma.property.model.StateFormula
import hu.bme.mit.gamma.property.util.PropertyUtil
import java.util.function.Function
import org.omg.sysml.lang.sysml.InvocationExpression
import org.omg.sysml.lang.sysml.LiteralBoolean
import org.omg.sysml.lang.sysml.LiteralInteger
import org.omg.sysml.lang.sysml.LiteralRational
import org.omg.sysml.lang.sysml.OperatorExpression
import org.omg.sysml.lang.sysml.Package
import org.omg.sysml.util.FeatureUtil

class ExpressionTransformer extends AbstractTransformer {
	
	protected final Function<org.omg.sysml.lang.sysml.Expression, Expression> featureHandler
	
	protected static extension PropertyUtil propertyUtil = PropertyUtil.INSTANCE
	
	new(Function<org.omg.sysml.lang.sysml.Expression, Expression> featureHandler) {
		this.featureHandler = featureHandler
	}
	
	//
	
	def getFeatureHandler() {
		return featureHandler
	}
	
	//
	
	def dispatch Expression transformExpression(org.omg.sysml.lang.sysml.Expression expression) {
		return featureHandler.apply(expression)
	}
	
	//
	
	def dispatch Expression transformExpression(LiteralBoolean expression) {
		val value = expression.isValue
		return (value) ? createTrueExpression : createFalseExpression
	}
	
	def dispatch Expression transformExpression(LiteralInteger expression) {
		val value = expression.value
		return value.toIntegerLiteral
	}
	
	def dispatch Expression transformExpression(LiteralRational expression) {
		val value = expression.value
		return value.toDecimalLiteral
	}
	
	def dispatch Expression transformExpression(OperatorExpression expression) {
		val operator = expression.operator
		val operands = expression.operand
		val gammaOperands = operands.map[it.transformExpression]
		switch (operator) {
			case "+": {
				val add = createAddExpression
				add.operands += gammaOperands
				return add
			}
			case "-": {
				val subtract = createSubtractExpression
				subtract.leftOperand = gammaOperands.head
				subtract.rightOperand = gammaOperands.last
				return subtract
			}
			case "*": {
				val multiply = createMultiplyExpression
				multiply.operands += gammaOperands
				return multiply
			}
			case "/": {
				val divide = createDivideExpression
				divide.leftOperand = gammaOperands.head
				divide.rightOperand = gammaOperands.last
				return divide
			}
			case "<": {
				val less = createLessExpression
				less.leftOperand = gammaOperands.head
				less.rightOperand = gammaOperands.last
				return less
			}
			case "<=": {
				val lessEqual = createLessEqualExpression
				lessEqual.leftOperand = gammaOperands.head
				lessEqual.rightOperand = gammaOperands.last
				return lessEqual
			}
			case ">": {
				val greater = createGreaterExpression
				greater.leftOperand = gammaOperands.head
				greater.rightOperand = gammaOperands.last
				return greater
			}
			case ">=": {
				val greaterEqual = createGreaterEqualExpression
				greaterEqual.leftOperand = gammaOperands.head
				greaterEqual.rightOperand = gammaOperands.last
				return greaterEqual
			}
			case "==",
			case "=": {
				val greaterEqual = createEqualityExpression
				greaterEqual.leftOperand = gammaOperands.head
				greaterEqual.rightOperand = gammaOperands.last
				return greaterEqual
			}
			case "!=",
			case "\\=": {
				val greaterEqual = createInequalityExpression
				greaterEqual.leftOperand = gammaOperands.head
				greaterEqual.rightOperand = gammaOperands.last
				return greaterEqual
			}
			case "and",
			case "&&",
			case "&": {
				val and = createAndExpression
				and.operands += gammaOperands
				return and
			}
			case "xor",
			case "^": {
				val xor = createXorExpression
				xor.operands += gammaOperands
				return xor
			}
			case "or",
			case "||",
			case "|": {
				val or = createOrExpression
				or.operands += gammaOperands
				return or
			}
			case "not",
			case "!": {
				val not = createNotExpression
				not.operand = gammaOperands.head
				return not
			}
			default:
				// It may be handled by the injected transformer
				return featureHandler.apply(expression)
		}
	}
	
	//
		
	def dispatch StateFormula transformProperty(org.omg.sysml.lang.sysml.Expression expression) {
		return createAtomicFormula(transformExpression(expression))
	} 
		
	def dispatch StateFormula transformProperty(InvocationExpression expression) {
		val type = expression.type.head
		val packageName = type?.getSelfOrContainerOfType(Package)?.name
		val params = expression.parameter
		if (packageName != "GammaLib" || params.size != 2 || type === null) {
			// May be an operation expression
			return createAtomicFormula(transformExpression(expression))
		}
		val parameter = FeatureUtil.getValuationFor(params.head)?.value
		switch (type.name) {
			case "EF": {
				return createEF(transformProperty(parameter))
			}
			case "EG": {
				return createEG(transformProperty(parameter))
			}
			case "AF": {
				return createAF(transformProperty(parameter))
			}
			case "AG": {
				return createAG(transformProperty(parameter))
			}
			default: {
				throw new Exception("Unsupported invocation: " + type.name + 
					". You may use standard arithmetic, relational and logical operators," + 
					" as well as the temporal operators of the Gamma library.")
			}	
		}
	}
	
}