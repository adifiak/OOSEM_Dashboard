package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.action.model.Action
import org.omg.sysml.lang.sysml.ActionUsage
import org.omg.sysml.lang.sysml.AssignmentActionUsage
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.IfActionUsage
import org.omg.sysml.lang.sysml.InvocationExpression
import org.omg.sysml.lang.sysml.PerformActionUsage
import org.omg.sysml.lang.sysml.SendActionUsage

import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class ActionTransformer extends AtomicElementTransformer {
	
	protected final extension ExpressionTransformer expressionTransformer
	protected final extension PortTransformer portTransformer
	protected final extension EventTransformer eventTransformer
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.expressionTransformer = FeatureTransformer.of(traceability)
		this.portTransformer = new PortTransformer(traceability)
		this.eventTransformer = new EventTransformer(traceability)
	}
	
	def dispatch Action transformAction(SendActionUsage action) {
		val payloadArgument = action.payloadArgument // Event
		val portUsage = action.portUsage
		
		val eventType = payloadArgument.eventType
		
		val gammaPort = portUsage.getOrTransformPort
		val gammaEvent = eventType.getOrTransformOutEventType(portUsage)
		
		val gammaRaiseEventAction = createRaiseEventAction
		gammaRaiseEventAction.port = gammaPort
		gammaRaiseEventAction.event = gammaEvent
		
		if (payloadArgument.basicEvent) {
			gammaRaiseEventAction.arguments += payloadArgument.transformExpression
		}
		else if (payloadArgument instanceof InvocationExpression) {
			val arguments = payloadArgument.argument
			for (argument : arguments) {
				// TODO rearrange arguments according to bindings (and not given order)
				gammaRaiseEventAction.arguments += argument.transformExpression
			}
		}
		
		return gammaRaiseEventAction
	}
	
	def dispatch Action transformAction(ActionUsage action) {
		val actions = action.nestedAction
		return actions.transformBlock
	}
	
	def dispatch Action transformAction(AssignmentActionUsage action) {
		val referent = action.referent
		val value = action.valueExpression
		
		val gammaDeclaration =
		switch (referent) {
			AttributeUsage: {
				traceability.getAttribute(referent)
			}
			default:
				throw new IllegalArgumentException("Not known referent: " + referent)
		}
		val gammaValue = value.transformExpression
		
		val gammaAssignment = gammaDeclaration.createAssignment(gammaValue)
		
		return gammaAssignment
	}
	
	def dispatch Action transformAction(IfActionUsage action) {
		val condition = action.ifArgument
		val thenAction = action.thenAction
		val elseAction = action.elseAction
		
		val gammaCondition = condition.transformExpression
		val gammaThenAction = thenAction?.transformAction
		val gammaElseAction = elseAction?.transformAction
		
		val gammaIfAction = gammaCondition.createIfStatement(gammaThenAction, gammaElseAction)
		
		return gammaIfAction
	}
	
	def dispatch Action transformAction(PerformActionUsage action) {
		val actions = action.nestedAction
		return actions.transformBlock
	}
	
	// 
	
	private def Action transformBlock(Iterable<? extends ActionUsage> actions) {
		if (actions.empty) {
			return createBlock
		}
		
		val gammaActions = actions.map[it.transformAction].toList
		val gammaBlock = gammaActions.wrap
		
		return gammaBlock
	}
	
}