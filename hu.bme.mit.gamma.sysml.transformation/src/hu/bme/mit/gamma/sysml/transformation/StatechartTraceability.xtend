package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.EnumerationLiteralDefinition
import hu.bme.mit.gamma.expression.model.TypeDeclaration
import hu.bme.mit.gamma.expression.model.VariableDeclaration
import hu.bme.mit.gamma.statechart.composite.AsynchronousAdapter
import hu.bme.mit.gamma.statechart.interface_.Event
import hu.bme.mit.gamma.statechart.interface_.Interface
import hu.bme.mit.gamma.statechart.interface_.Port
import hu.bme.mit.gamma.statechart.statechart.State
import hu.bme.mit.gamma.statechart.statechart.SynchronousStatechartDefinition
import hu.bme.mit.gamma.statechart.statechart.Transition
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.DataType
import org.omg.sysml.lang.sysml.EnumerationUsage
import org.omg.sysml.lang.sysml.Feature
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.Redefinition
import org.omg.sysml.lang.sysml.StateUsage
import org.omg.sysml.lang.sysml.TransitionUsage
import org.omg.sysml.lang.sysml.Type
import org.omg.sysml.lang.sysml.Usage

import static com.google.common.base.Preconditions.checkNotNull

import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class StatechartTraceability {
	
	protected final Usage root
	protected AsynchronousAdapter adapter
	protected SynchronousStatechartDefinition statechart
	
	// Coming from outside
	
	protected final Map<DataType, TypeDeclaration> typeDeclarations // Global
	protected final Map<EnumerationUsage, EnumerationLiteralDefinition> enumLiterals // Global
	protected final Map<PortDefinition, Interface> interfaces // Global
	// The event type is encoded using a String as multiple type classes in SysML should be mapped
	// to the same Gamma type, e.g., DataType("Integer") and Function("LiteralIntegerEvaluation")
	protected final Map<Pair<PortDefinition, String>, Event> inEvents // Global
	protected final Map<Pair<PortDefinition, String>, Event> outEvents // Global
	
	// Internal, statechart-specific maps: keys are Usages to support ReferenceUsages as well (redefinitions)
		
	protected final Map<Usage, Port> ports = newHashMap
	protected final Map<Usage, VariableDeclaration> variableDeclarations = newHashMap
	protected final Map<Usage, State> states = newHashMap
	protected final Map<Usage, Transition> transitions = newHashMap
	
	protected Port defaultPort
	protected Interface defaultInterface
	
	//
	protected final Set<Redefinition> redefinitions = newHashSet
	//
	// Storing redefinition-Gamma element pairs just for debugging
	protected final Map<Feature, EObject> redefiningFeatures = newHashMap
	
	//
	
	new(Usage root,
			Map<DataType, TypeDeclaration> typeDeclarations,
			Map<EnumerationUsage, EnumerationLiteralDefinition> enumLiterals,
			Map<PortDefinition, Interface> interfaces,
			Map<Pair<PortDefinition, String>, Event> inEvents,
			Map<Pair<PortDefinition, String>, Event> outEvents) {
		this.root = root
		this.typeDeclarations = typeDeclarations
		this.enumLiterals = enumLiterals
		this.interfaces = interfaces
		this.inEvents = inEvents
		this.outEvents = outEvents
	}
	
	//
	
	def getRoot() {
		return root
	}
	
	def setAdapter(AsynchronousAdapter adapter) {
		this.adapter = adapter
	}
	
	def getAdapter() {
		return adapter
	}
	
	def setStatechart(SynchronousStatechartDefinition statechart) {
		this.statechart = statechart
	}
	
	def getStatechart() {
		return statechart
	}
	
	//
	
	def putType(DataType dataType, TypeDeclaration typeDeclaration) {
		checkNotNull(dataType)
		checkNotNull(typeDeclaration)
		typeDeclarations += dataType -> typeDeclaration
	}
	
	def getType(DataType dataType) {
		checkNotNull(dataType)
		val typeDeclaration = typeDeclarations.get(dataType)
		checkNotNull(typeDeclaration)
		return typeDeclaration
	}
	
	def containsType(DataType dataType) {
		checkNotNull(dataType)
		return typeDeclarations.containsKey(dataType)
	}
	
	//
	
	def putLiteral(EnumerationUsage enumerationUsage, EnumerationLiteralDefinition enumLiteral) {
		checkNotNull(enumerationUsage)
		checkNotNull(enumLiteral)
		enumLiterals += enumerationUsage -> enumLiteral
	}
	
	def getLiteral(EnumerationUsage enumerationUsage) {
		checkNotNull(enumerationUsage)
		val enumLiteral = enumLiterals.get(enumerationUsage)
		checkNotNull(enumLiteral)
		return enumLiteral
	}
	
	//
	
	def putPort(PortDefinition portDefinition, Interface _interface) {
		// portDefinition can be null: default interface
		checkNotNull(_interface)
		interfaces += portDefinition -> _interface
	}
	
	def getPort(PortDefinition portDefinition) {
		// portDefinition can be null: default interface
		val _interface = interfaces.get(portDefinition)
		checkNotNull(_interface)
		return _interface
	}
	
	def containsPort(PortDefinition portDefinition) {
		// portDefinition can be null: default port
		return interfaces.containsKey(portDefinition)
	}
	
	//
	
	def putAttribute(Usage attributeUsage, VariableDeclaration variableDeclaration) {
		checkNotNull(attributeUsage)
		checkNotNull(variableDeclaration)
		variableDeclarations += attributeUsage -> variableDeclaration
	}
	
	def getAttribute(Usage attributeUsage) {
		checkNotNull(attributeUsage)
		val variableDeclaration = variableDeclarations.get(attributeUsage)
		checkNotNull(variableDeclaration)
		return variableDeclaration
	}
	
	def containsAttribute(Usage attributeUsage) {
		return variableDeclarations.containsKey(attributeUsage)
	}
	
	//
	
	def putState(Usage stateUsage, State state) {
		checkNotNull(stateUsage)
		checkNotNull(state)
		states += stateUsage -> state
	}
	
	def getState(Usage stateUsage) {
		checkNotNull(stateUsage)
		val state = states.get(stateUsage)
		checkNotNull(state)
		return state
	}
	
	def containsState(Usage stateUsage) {
		checkNotNull(stateUsage)
		return states.containsKey(stateUsage)
	}
	
	def removeState(Usage stateUsage) {
		checkNotNull(stateUsage)
		states.remove(stateUsage)
	}
	
	//
	
	def putTransition(Usage transitionUsage, Transition transition) {
		checkNotNull(transitionUsage)
		checkNotNull(transition)
		transitions += transitionUsage -> transition
	}
	
	def getTransition(Usage transitionUsage) {
		checkNotNull(transitionUsage)
		val transition = transitions.get(transitionUsage)
		checkNotNull(transition)
		return transition
	}
	
//	def getTransitionUsage(Transition transition) {
//		checkNotNull(transition)
//		for (transitionUsage : transitions.keySet) {
//			val transitionValue = transitions.get(transitionUsage)
//			if (transition === transitionValue) {
//				return transitionUsage
//			}
//		}
//		throw new IllegalArgumentException("Not found transition usage for: " + transition)
//	}
	
	def containsTransition(Usage transitionUsage) {
		checkNotNull(transitionUsage)
		return transitions.containsKey(transitionUsage)
	}
	
	//
	
	def putPort(Usage portUsage, Port port) {
		// portUsage can be null: default port
		checkNotNull(port)
		ports += portUsage -> port
	}
	
	def getPort(Usage portUsage) {
		// portUsage can be null: default port
		val port = ports.get(portUsage)
		checkNotNull(port)
		return port
	}
	
	def containsPort(Usage portUsage) {
		// portUsage can be null: default port
		return ports.containsKey(portUsage)
	}
	
	//
	
	def putIn(Pair<PortUsage, Type> type, Event event) {
		checkNotNull(type)
		// type.key (port usage) can be null
		checkNotNull(type.value)
		checkNotNull(event)
		
		val port = type.key
		val portDefinition = port?.providedPortDefinition
		val eventType = type.value
		val commonTypeName = eventType.commonTypeName
		
		port.inEvents += portDefinition -> commonTypeName -> event
	}
	
	def getIn(Pair<PortUsage, Type> type) {
		checkNotNull(type)
		// type.key (port usage) can be null
		checkNotNull(type.value)
		
		val port = type.key
		val portDefinition = port?.providedPortDefinition
		val eventType = type.value
		val commonTypeName = eventType.commonTypeName
		
		val event = port.inEvents.get(portDefinition -> commonTypeName)
		checkNotNull(event)
		return event
	}
	
	def containsIn(Pair<PortUsage, Type> type) {
		checkNotNull(type)
		// type.key (port usage) can be null
		checkNotNull(type.value)
		
		val port = type.key
		val portDefinition = port?.providedPortDefinition
		val eventType = type.value
		val commonTypeName = eventType.commonTypeName
 
		return port.inEvents.containsKey(portDefinition -> commonTypeName)
	}
	
	def putOut(Pair<PortUsage, Type> type, Event event) {
		checkNotNull(type)
		// type.key (port usage) can be null
		checkNotNull(type.value)
		checkNotNull(event)
		
		val port = type.key
		val portDefinition = port?.providedPortDefinition
		val eventType = type.value
		val commonTypeName = eventType.commonTypeName
		
		port.outEvents += portDefinition -> commonTypeName -> event
	}
	
	def getOut(Pair<PortUsage, Type> type) {
		checkNotNull(type)
		// type.key (port usage) can be null
		checkNotNull(type.value)
		
		val port = type.key
		val portDefinition = port?.providedPortDefinition
		val eventType = type.value
		val commonTypeName = eventType.commonTypeName
		
		val event = port.outEvents.get(portDefinition -> commonTypeName)
		checkNotNull(event)
		return event
	}
	
	def containsOut(Pair<PortUsage, Type> type) {
		checkNotNull(type)
		// type.key (port usage) can be null
		checkNotNull(type.value)
		
		val port = type.key
		val portDefinition = port?.providedPortDefinition
		val eventType = type.value
		val commonTypeName = eventType.commonTypeName
		
		return port.outEvents.containsKey(portDefinition -> commonTypeName)
	}
	
	//
	
	private def getInEvents(PortUsage port) {
		if (port === null || !port.required) {
			return inEvents
		}
		return outEvents
	}
	
	private def getOutEvents(PortUsage port) {
		if (port === null || !port.required) {
			return outEvents
		}
		return inEvents
	}
	
	//
	
	def getDefaultInterface() {
		return defaultInterface
	}
	
	def setDefaultInterface(Interface defaultInterface) {
		this.defaultInterface = defaultInterface
	}
	
	def hasDefaultPort() {
		return defaultPort !== null
	}
	
	def getDefaultPort() {
		return defaultPort
	}
	
	def setDefaultPort(Port defaultPort) {
		this.defaultPort = defaultPort
	}
	
	def getPorts() {
		return ports
	}
	
	//
	
	def putRedefinition(Redefinition redefinition) {
		checkNotNull(redefinition)
		redefinitions += redefinition
	}
	
	def getRedefinitions() {
		return redefinitions
	}
	
	//
	
	def traceRedefiningFeatures(Iterable<? extends Redefinition> redefinitions) {
		for (redefinition : redefinitions) {
			redefinition.traceRedefiningFeature
		}
	}
	
	def traceRedefiningFeature(Redefinition redefinition) {
		checkNotNull(redefinition)
		// To handle potential reference hierarchies 
		val redefinedFeature = redefinition.redefinedFeature.lastFeature
		//
		val redefiningFeature = redefinition.redefiningFeature as Usage
		checkNotNull(redefinedFeature)
		checkNotNull(redefiningFeature)
		
		val gammaElement =
		switch (redefinedFeature) {
			AttributeUsage: {
				val gammaVariable = getAttribute(redefinedFeature)
				putAttribute(redefiningFeature, gammaVariable)
			}
			StateUsage: {
				val gammaState = getState(redefinedFeature)
				putState(redefiningFeature, gammaState)
			}
			TransitionUsage: {
				val gammaTransition = getTransition(redefinedFeature)
				putTransition(redefiningFeature, gammaTransition)
			}
			PortUsage: {
				val gammaPort = getPort(redefinedFeature)
				putPort(redefiningFeature, gammaPort)
			}
			default:
				throw new IllegalArgumentException("Not known redefined feature: " + redefinedFeature)
		}
		redefiningFeatures += redefiningFeature -> gammaElement
	}
	
	//
	
	def getStates() {
		return states.keySet
	}
	
	def getVariables() {
		return variableDeclarations.keySet
	}
	
}