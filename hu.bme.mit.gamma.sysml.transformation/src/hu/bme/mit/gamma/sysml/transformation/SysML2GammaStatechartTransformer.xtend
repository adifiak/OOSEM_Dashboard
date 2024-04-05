package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.VariableDeclaration
import hu.bme.mit.gamma.statechart.composite.AsynchronousAdapter
import hu.bme.mit.gamma.statechart.interface_.Port
import hu.bme.mit.gamma.statechart.statechart.GuardEvaluation
import hu.bme.mit.gamma.statechart.statechart.InitialState
import hu.bme.mit.gamma.statechart.statechart.Region
import hu.bme.mit.gamma.statechart.statechart.State
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition
import hu.bme.mit.gamma.statechart.statechart.Transition
import java.util.Set
import java.util.logging.Level
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.ExhibitStateUsage
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.StateUsage
import org.omg.sysml.lang.sysml.SuccessionAsUsage
import org.omg.sysml.lang.sysml.TransitionUsage
import org.omg.sysml.lang.sysml.Usage

import static extension hu.bme.mit.gamma.action.derivedfeatures.ActionModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class SysML2GammaStatechartTransformer extends AtomicElementTransformer {
	
	protected final Usage part
	protected final StateUsage stateUsage
	
	protected final SynchronousStatechartDefinition statechartDefinition
	protected AsynchronousAdapter adapter // Assigned as the last step
	
	///
	
	protected Set<Transition> initialTransitions = newHashSet
	
	///
	
	protected final extension PortTransformer portTransformer
	protected final extension DeclarationTransformer declarationTransformer
	protected final extension TriggerTransformer triggerTransformer
	protected final extension ExpressionTransformer expressionTransformer
	protected final extension ActionTransformer actionTransformer
	
	new(StatechartTraceability traceability, String statechartName) {
		super(traceability)
		//
		this.part = traceability.root
		val ownedStates = part.getAllFeatures(StateUsage)
		this.stateUsage = ownedStates.getFirstOfType(ExhibitStateUsage)
		logger.log(Level.INFO, "Retrieved exhibit state usage " + stateUsage.name)
		
		this.statechartDefinition = createSynchronousStatechartDefinition => [
			it.name = statechartName
		]
		//
		this.portTransformer = new PortTransformer(traceability)
		this.declarationTransformer = new DeclarationTransformer(traceability)
		this.triggerTransformer = new TriggerTransformer(traceability)
		this.expressionTransformer = FeatureTransformer.of(traceability)
		this.actionTransformer = new ActionTransformer(traceability)
	}
	
	def execute() {
		//
		traceability.statechart = statechartDefinition
		//
		val ports = part.getAllFeatures(PortUsage)
		for (port : ports) {
			statechartDefinition.ports += port.transform
		}
		//
		val attributes = part.getAllFeatures(AttributeUsage)
		for (attribute : attributes) {
			// TODO handle bindings (fix values)
			statechartDefinition.variableDeclarations += attribute.transform
		}
		
		// Top state usage
		val topGammaState = stateUsage.transformState
		traceability.removeState(stateUsage) // As stateUsage is not a valid state but a "statechart"
		statechartDefinition.regions += topGammaState.regions
		// Adding initial transitions manually, as during transformation,
		// the statechart does not contain the regions and state nodes
		statechartDefinition.transitions += initialTransitions
		// The target state has to be reset, too in the case of parallel regions
		for (initialTransition : initialTransitions) {
			val source = initialTransition.sourceState
			val target = initialTransition.targetState
			if (source.parentRegion !== target.parentRegion) {
				initialTransition.targetState = target.parentState
			}
		}
		//
		
		val transitions = newArrayList
		for (state : #[stateUsage] // Contains the entry transitions
				+ traceability.getStates) {
			transitions += state.getAllFeatures(TransitionUsage)
		}
		for (transition : transitions) {
			statechartDefinition.transitions += transition.transform
		}
		// Events are transformed in transition transformation, hence the port setting here
		if (traceability.hasDefaultPort) {
			statechartDefinition.ports += traceability.getDefaultPort
		}
		
		// SysML v2 semantics related settings; may change in the future
		if (statechartDefinition.hasOrthogonalRegions) {
			// This makes a difference only for orthogonal regions
			statechartDefinition.guardEvaluation = GuardEvaluation.BEGINNING_OF_STEP
		}
		
		// Wrapping the statechart into an adapter
		adapter = statechartDefinition.wrapIntoDefaultAdapter(part.name, part.messageQueueName, 4)
		traceability.adapter = adapter
		
		return traceability
	}
	
	protected def Port transform(PortUsage port) {
		logger.log(Level.INFO, "Transforming port " + port.name)
		
		val gammaPort = port.transformPort
		
		return gammaPort
	}
	
	protected def VariableDeclaration transform(AttributeUsage attribute) {
		logger.log(Level.INFO, "Transforming attribute " + attribute.name)
		
		val gammaVariable = attribute.transformAttribute
		
		return gammaVariable
	}
	
	protected def Region transformRegion(StateUsage state) {
		val region = createRegion
		
		region.name = state.parentRegionName
		region.stateNodes += state.transformState
		// Initial node after transforming the substates
		val gammaInitialNode = state.transformEntryNode
		gammaInitialNode.name = state.getInitialStateName(region)
		
		region.stateNodes += gammaInitialNode

		return region
	}
	
	protected def State transformState(StateUsage state) {
		logger.log(Level.INFO, "Transforming state " + state.name)
		
		val gammaState = createState
		gammaState.name = state.stateName
		
		val isParallel = state.parallel
		val isComposite = state.compositeState // The basic "isComposite" feature does not work...
		val substates = state.getAllFeatures(StateUsage)
		if (isParallel) {
			for (substate : substates) {
				gammaState.regions += substate.transformRegion
			}
		}
		else if (isComposite) {
			val subregion = createRegion
			subregion.name = state.subegionName
			gammaState.regions += subregion
			
			for (substate : substates) {
				subregion.stateNodes += substate.transformState
			}
			// Initial node after transforming the substates
			val gammaInitialNode = state.transformEntryNode
			gammaInitialNode.name = state.getInitialStateName(subregion)
			
			subregion.stateNodes += gammaInitialNode
		}
		
		// Entry and exit actions
		val entryAction = state.entryAction
		if (entryAction !== null) {
			val gammaEntryAction = entryAction.transformAction
			if (!gammaEntryAction.effectlessAction) {
				gammaState.entryActions += gammaEntryAction
			}
		}
		
		val exitAction = state.exitAction
		if (exitAction !== null) {
			val gammaExitAction = exitAction.transformAction
			if (!gammaExitAction.effectlessAction) {
				gammaState.exitActions += gammaExitAction
			}
		}
		//
		
		traceability.putState(state, gammaState)
		
		return gammaState
	}
	
	protected def InitialState transformEntryNode(StateUsage state) {
		val entry = state.getFirstOfAllContentsOfType(SuccessionAsUsage)
		val target = entry.targetFeature.getFirstOfType(StateUsage)
		val gammaTarget = traceability.getState(target)
		
		val gammaInitialState = statechartUtil.createInitialState(state.initialStateName)
		
		val gammaTransition = gammaInitialState.createTransition(gammaTarget)
		
		// Marking initial transitions
		initialTransitions += gammaTransition
		
		return gammaInitialState
	}
	
	protected def Transition transform(TransitionUsage transition) {
		val source = transition.source
		val target = transition.target
		logger.log(Level.INFO, "Transforming transition " + source.name + " -> " + target.name)
		val gammaSource = if (source instanceof StateUsage) {
			traceability.getState(source)
		}
		else {
			// Unused?
			val targetState = target as StateUsage
			val gammaTarget = traceability.getState(targetState)
			val parentRegion = gammaTarget.parentRegion
			val entryNode = createInitialState => [
				it.name = target.entryNodeName
			]
			parentRegion.stateNodes += entryNode
			entryNode
			//
		}
		val gammaTarget = if (target instanceof StateUsage) {
			traceability.getState(target)
		}
		else {
			val parentRegion = gammaSource.parentRegion
			val exitNode = createState => [
				it.name = target.getExitNodeName(parentRegion)
			]
			parentRegion.stateNodes += exitNode
			exitNode
		}
		
		val gammaTransition = gammaSource.createTransition(gammaTarget)
		traceability.putTransition(transition, gammaTransition)
		// Tracing so the following transformations steps can refer to the transition
		
		// Trigger
		val trigger = transition.triggerAction.head
		gammaTransition.trigger = trigger.transformTrigger
		// Guards
		val guard = transition.guardExpression.head
		gammaTransition.guard = guard?.transformExpression
		// Effect
		val effects = transition.effectAction
		gammaTransition.effects += effects.map[it.transformAction]
		
		
		return gammaTransition
	}
	
}