package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.property.model.PropertyPackage
import hu.bme.mit.gamma.property.model.QuantifiedFormula
import hu.bme.mit.gamma.property.util.PropertyUtil
import java.util.List
import java.util.logging.Level
import org.omg.sysml.lang.sysml.Expression

class SysML2GammaPropertyTransformer extends AbstractTransformer {
	
	protected final CompositeTraceability traceability
	protected final List<Expression> expressions = newArrayList
	protected final extension ExpressionTransformer expressionTransformer
	
	protected final extension PropertyUtil propertyUtil = PropertyUtil.INSTANCE
	
	new(Iterable<? extends Expression> expressions, CompositeTraceability traceability) {
		this.traceability = traceability
		this.expressions += expressions
		this.expressionTransformer = ChainingFeatureTransformer.of(traceability)
	}
	
	def PropertyPackage execute() {
		val rootComponent = traceability.getRootComponent
		var PropertyPackage propertyPackage = null
		
		for (expression : expressions) {
			// parse temporal operators, default is AG (+warning)
			var gammaProperty = expression.transformProperty
			if (!(gammaProperty instanceof QuantifiedFormula)) {
				gammaProperty = gammaProperty.createAG
				logger.log(Level.WARNING, "Expression without temporal operator has been interpreted as global invariant (AG).");
			}
			
			if (propertyPackage === null) {
				propertyPackage = rootComponent.wrapFormula(gammaProperty)
			}
			else {
				propertyPackage.formulas += gammaProperty.createCommentableStateFormula
			}
		}
		
		return propertyPackage
	}
	
}