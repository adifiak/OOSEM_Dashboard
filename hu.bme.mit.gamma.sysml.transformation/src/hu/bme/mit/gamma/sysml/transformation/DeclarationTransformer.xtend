package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.VariableDeclaration
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.Expression
import org.omg.sysml.lang.sysml.FeatureValue

import static extension hu.bme.mit.gamma.expression.derivedfeatures.ExpressionModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*

class DeclarationTransformer extends AtomicElementTransformer {
	
	protected final extension TypeTransformer typeTransformer
	protected final extension ExpressionTransformer expressionTransformer
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.typeTransformer = new TypeTransformer(traceability)
		this.expressionTransformer = FeatureTransformer.of(traceability)
	}
	
	def VariableDeclaration transformAttribute(AttributeUsage attribute) {
		val type = attribute.type.head
		val gammaType = type.transformType
		
		val name = attribute.variableName
		
		val featureValue = attribute.membership
				.getFirstOfType(FeatureValue)
		val expression = featureValue?.memberElement as Expression
		val gammaInitialExpression = (expression !== null) ?
				expression.transformExpression : gammaType.defaultExpression
		
		val gammaVariable = gammaType.createVariableDeclaration(name, gammaInitialExpression)
		
		traceability.putAttribute(attribute, gammaVariable)
		
		return gammaVariable
	}
	
	
}