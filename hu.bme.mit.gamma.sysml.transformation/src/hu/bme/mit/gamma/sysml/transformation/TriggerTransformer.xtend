package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.statechart.interface_.TimeUnit
import hu.bme.mit.gamma.statechart.statechart.State
import org.omg.sysml.lang.sysml.AcceptActionUsage
import org.omg.sysml.lang.sysml.Expression
import org.omg.sysml.lang.sysml.OperatorExpression
import org.omg.sysml.lang.sysml.TransitionUsage

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class TriggerTransformer extends AtomicElementTransformer {
	
	protected final extension PortTransformer portTransformer
	protected final extension EventTransformer eventTransformer
	protected final extension ExpressionTransformer expressionTransformer
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.portTransformer = new PortTransformer(traceability)
		this.eventTransformer = new EventTransformer(traceability)
		this.expressionTransformer = FeatureTransformer.of(traceability)
	}
	
	def transformTrigger(AcceptActionUsage trigger) {
		val portUsage = trigger.portUsage
		
		if (portUsage !== null) {
			return trigger.createEventTrigger
		}
		else {
			return trigger.createTimeoutTrigger
		}
	}
	
	//
	
	protected def createEventTrigger(AcceptActionUsage trigger) {
		val payloadParameter = trigger.payloadParameter // Event
		val portUsage = trigger.portUsage
		
		val eventType = payloadParameter.type.head
		
		val gammaPort = portUsage.getOrTransformPort		
		val gammaEvent = eventType.getOrTransformInEventType(portUsage)
		
		val gammaPortEventReference = createPortEventReference
		gammaPortEventReference.port = gammaPort
		gammaPortEventReference.event = gammaEvent
		
		val gammaEventTrigger = createEventTrigger
		gammaEventTrigger.eventReference = gammaPortEventReference
		
		return gammaEventTrigger
	}
	
	protected def createTimeoutTrigger(AcceptActionUsage trigger) {
		val transitionUsage = trigger.getContainerOfType(TransitionUsage)
		
		val afterTriggerExpression = trigger.getAfterTriggerExpression // after 1 [s]
		var Expression afterTriggerValue = afterTriggerExpression
		var TimeUnit timeUnit = TimeUnit.SECOND
		// 1 + 2 [s] bad -- (1 + 2) [s] good
		if (afterTriggerExpression instanceof OperatorExpression) {
			val operator = afterTriggerExpression.operator
			if (operator == "[") {
				afterTriggerValue = afterTriggerExpression.operand.head
				// timeUnit = .. // Actually only 's' is supported (SI)
			}
		}
		
		val gammaTransition = traceability.getTransition(transitionUsage)
		val source = gammaTransition.sourceState as State
		
		val gammaStatechart = source.containingStatechart
		
		val gammaTimeoutDeclaration = createTimeoutDeclaration => [
			it.name = source.timeoutName
		]
		gammaStatechart.timeoutDeclarations += gammaTimeoutDeclaration
		
		val setTimeoutAction = createSetTimeoutAction
		setTimeoutAction.timeoutDeclaration = gammaTimeoutDeclaration
		val time = createTimeSpecification
		time.value = afterTriggerValue.transformExpression
		time.unit = timeUnit
		setTimeoutAction.time = time
		
		source.entryActions += setTimeoutAction
		
		val timeoutTrigger = createEventTrigger => [
			it.eventReference = createTimeoutEventReference => [
				it.timeout = gammaTimeoutDeclaration
			]
		]
		
		return timeoutTrigger
	}
	
}

/*
class TriggerTransformer extends AtomicElementTransformer {
	
	protected final extension PortTransformer portTransformer
	protected final extension EventTransformer eventTransformer
	// Packages of the metamodels
	final extension InterfaceModelPackage ifPackage = InterfaceModelPackage.eINSTANCE
	final extension StatechartModelPackage stmPackage = StatechartModelPackage.eINSTANCE
	
	private int id = 0;
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.portTransformer = new PortTransformer(traceability)
		this.eventTransformer = new EventTransformer(traceability)
	}
	
	def dispatch transformTrigger(AcceptActionUsage trigger, StateNode gammaSource) {
		val payloadParameter = trigger.payloadParameter // Event
		val payload = FeatureUtil.getValuationFor(payloadParameter)
		if (payload !== null) {
			val triggerInvocation = payload.value as TriggerInvocationExpression
			if (triggerInvocation.kind == TriggerKind.AFTER) {
				val expression = triggerInvocation.ownedRelationship.head?.ownedRelatedElement.head
				if (expression instanceof OperatorExpression) {
					if (expression.operator != "[" || expression.operand.size != 2) {
						throw new Exception("Unsupported expression in AFTER trigger. Use a single integer number with [s] or [ms].")
					}
					val delay = expression.operand.get(0);
					val unit = expression.operand.get(1);
					if (delay instanceof LiteralInteger) {
						val delayInt = delay.value
						if (unit instanceof FeatureReferenceExpression) {
							val membership = unit.referent;
							//if (membership instanceof Membership) {
								//val member = membership.member
								if (membership instanceof AttributeUsage) {
									val unitString = membership.shortName
									switch (unitString) {
										case "s", 
										case "ms": {
											System.out.println("AFTER trigger: " + delayInt + " " + unitString)
											throw new Exception("AFTER triggers are currently not supported.")
										}
										default: {
											throw new Exception("Unsupported type in AFTER trigger. Use a single integer number with [s] or [ms].")
										}
									}
								} else {
									throw new Exception("Unsupported type in AFTER trigger. Use a single integer number with [s] or [ms].")
								}
							//} else {
							//	throw new Exception("Unsupported type in AFTER trigger. Use a single integer number with [s] or [ms].")
							//}
						} else {
							throw new Exception("Unsupported type in AFTER trigger. Use a single integer number with [s] or [ms].")
						}
					} else {
						throw new Exception("Unsupported type in AFTER trigger. Use a single integer number with [s] or [ms].")
					}
				} else {
					throw new Exception("Unsupported expression in AFTER trigger. Use a unit: [s] or [ms]")
				}
			} else {
				throw new Exception("Unsupported trigger: " + triggerInvocation.kind)
			}
		} else {
			val portUsage = trigger.portUsage
			
			val eventType = payloadParameter.type.head
			
			val gammaPort = portUsage.getOrTransformPort		
			val gammaEvent = eventType.getOrTransformInEventType(portUsage)
			
			val gammaPortEventReference = createPortEventReference
			gammaPortEventReference.port = gammaPort
			gammaPortEventReference.event = gammaEvent
			
			val gammaEventTrigger = createEventTrigger
			gammaEventTrigger.eventReference = gammaPortEventReference
			
			return gammaEventTrigger
		}
	}

}
*/