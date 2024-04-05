package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.statechart.interface_.EventDirection
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.Type

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class EventTransformer extends AtomicElementTransformer {
	
	protected final extension TypeTransformer typeTransformer
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.typeTransformer = new TypeTransformer(traceability)
	}
	
	def getOrTransformInEventType(Type eventType, PortUsage port) {
		if (traceability.containsIn(port -> eventType)) {
			return traceability.getIn(port -> eventType)
		}
		else {
			return eventType.transformInEventType(port)
		}
	}
	
	protected def transformInEventType(Type eventType, PortUsage port) {
		val gammaPort =	traceability.getPort(port)
		val direction = (gammaPort.provided) ? EventDirection.IN : EventDirection.OUT
		val gammaEventName = eventType.inEventName
		val gammaParameterName = eventType.parameterName
		val gammaEvent = eventType.transformEventType(port, direction, gammaEventName, gammaParameterName)
		
		traceability.putIn(port -> eventType, gammaEvent)
		
		return gammaEvent
	}
	
	def getOrTransformOutEventType(Type eventType, PortUsage port) {
		if (traceability.containsOut(port -> eventType)) {
			return traceability.getOut(port -> eventType)
		}
		else {
			return eventType.transformOutEventType(port)
		}
	}
	
	protected def transformOutEventType(Type eventType, PortUsage port) {
		val gammaPort =	traceability.getPort(port)
		val direction = (gammaPort.provided) ? EventDirection.OUT : EventDirection.IN
		val gammaEventName = eventType.outEventName
		val gammaParameterName = eventType.parameterName
		
		val gammaEvent = eventType.transformEventType(port, direction, gammaEventName, gammaParameterName)
				
		traceability.putOut(port -> eventType, gammaEvent)
		
		return gammaEvent
	}
	
	private def transformEventType(Type eventType, PortUsage port, EventDirection direction,
			String eventName, String parameterName) {
		val gammaPort =	traceability.getPort(port)
		val gammaInterface = gammaPort.interface
		
		val gammaEvent = createEvent
		gammaEvent.name = eventName
		
		// Integers, Booleans, etc., represent basic event types, so we create default payload parameters
		if (eventType.basicEvent) {
			val gammaParameter = eventType.createParameter(parameterName)
			gammaEvent.parameterDeclarations += gammaParameter
		}
		// Attribute usages represent parameters
		val eventAttributes = eventType.eventParameters
		for (eventAttribute : eventAttributes) {
			val paramterType = eventAttribute.type.head
			val gammaParameter = paramterType.createParameter(parameterName)
			gammaEvent.parameterDeclarations += gammaParameter
			// TODO later attribute-parameter mappings might have to be traced: use global map
		}
		//
		
		val gammaEventDeclaration = createEventDeclaration
		gammaEventDeclaration.event = gammaEvent
		gammaEventDeclaration.direction = direction
		
		gammaInterface.events += gammaEventDeclaration
		
		return gammaEvent
	}
	
	private def createParameter(Type type, String name) {
		val gammaType = type.transformType
		val gammaParameter = gammaType.createParameterDeclaration(name)
		return gammaParameter
	}
	
}