package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.Expression
import java.util.function.Function
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.EnumerationUsage
import org.omg.sysml.lang.sysml.Feature
import org.omg.sysml.lang.sysml.FeatureReferenceExpression
import org.omg.sysml.lang.sysml.ReferenceUsage
import org.omg.sysml.lang.sysml.TransitionUsage
import org.omg.sysml.lang.sysml.Type

import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class FeatureTransformer extends AtomicElementTransformer
			implements Function<org.omg.sysml.lang.sysml.Expression, Expression> {
	
	private new(StatechartTraceability traceability) {
		super(traceability)
	}
	
	static def of(StatechartTraceability traceability) {
		return new ExpressionTransformer(
			new FeatureTransformer(traceability))
	}
	
	//
	
	override apply(org.omg.sysml.lang.sysml.Expression expression) {
		return expression.transformExpression
	}
	
	//
	
	def dispatch Expression transformExpression(org.omg.sysml.lang.sysml.Expression expression) {
		throw new IllegalArgumentException("Not supported expression: " + expression)
	}
	
	def dispatch Expression transformExpression(FeatureReferenceExpression expression) {
		val value = expression.referent
		
		val gammaExpression = value.transformFeature
		return gammaExpression
	}
	
	protected def Expression transformFeature(Feature value) {
		switch (value) {
			EnumerationUsage: {
				val gammaEnumeration = traceability.getLiteral(value)
				return gammaEnumeration.createEnumerationLiteralExpression
			}
			AttributeUsage: {
				val gammaVariable = traceability.getAttribute(value)
				return gammaVariable.createReferenceExpression
			}
			ReferenceUsage: {
				// Check if they are redefinition-contained references
				if (traceability.containsAttribute(value)) {
					val gammaVariable = traceability.getAttribute(value)
					return gammaVariable.createReferenceExpression
				}
				//
				
				val type = value.type.head
				
				switch (type) {
					Type: {
						val transition = value.getContainerOfType(TransitionUsage)
						if (transition !== null) {
							// Parameter reference
							val trigger = transition.triggerAction.head
							val portUsage = trigger.portUsage
							
							val port = traceability.getPort(portUsage)
							val event = traceability.getIn(portUsage -> type)
							val parameter = event.parameterDeclarations.onlyElement
							
							val eventParameterReference = createEventParameterReferenceExpression
							eventParameterReference.port = port
							eventParameterReference.event = event
							eventParameterReference.parameter = parameter
							
							return eventParameterReference
						}
						else {
							throw new IllegalArgumentException("Not handled type: " + type)
						}
					}
					default:
						throw new IllegalArgumentException("Not handled type: " + type)
				}
			}
			default:
				throw new IllegalArgumentException("Not known feature: " + value)
		}
	}
	
}