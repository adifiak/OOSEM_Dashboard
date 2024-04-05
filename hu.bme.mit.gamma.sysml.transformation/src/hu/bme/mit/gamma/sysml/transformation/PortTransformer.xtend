package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.statechart.interface_.RealizationMode
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.PortUsage

import static com.google.common.base.Preconditions.checkState

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class PortTransformer extends AtomicElementTransformer {
	
	protected final extension TypeTransformer typeTransformer
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.typeTransformer = new TypeTransformer(traceability)
	}
	
	def getOrTransformPort(PortUsage port) {
		if (traceability.containsPort(port)) {
			return traceability.getPort(port)
		}
		else {
			checkState(port === null)
			val defaultGammaPort = port.transformPort
			traceability.defaultPort = defaultGammaPort
			traceability.defaultInterface = defaultGammaPort.interface
			return defaultGammaPort
		}
	}
	
	protected def transformPort(PortUsage port) {
		var interfaceName = interfaceName
		var portName = portName
		var isConjugated = true // Required is default as we prefer out events
		
		var PortDefinition portDefinition = null
		if (port !== null) {
			portDefinition = port.providedPortDefinition
			isConjugated = port.portDefinition.head.isConjugated // port.isConjugated is buggy?
		
			interfaceName = portDefinition.interfaceName
			portName = port.portName
		}
		
		val gammaInterface =
		if (traceability.containsPort(portDefinition)) {
			traceability.getPort(portDefinition)
		}
		else {
			val newGammaInterface = createInterface
			newGammaInterface.name = interfaceName
			
			traceability.putPort(portDefinition, newGammaInterface)
			
			newGammaInterface
		}
		
		val gammaPort = createPort
		gammaPort.name = portName
		
		val gammaInterfaceRealization = createInterfaceRealization
		gammaInterfaceRealization.realizationMode = (isConjugated) ?
			RealizationMode.REQUIRED : RealizationMode.PROVIDED
		gammaInterfaceRealization.interface = gammaInterface
		
		gammaPort.interfaceRealization = gammaInterfaceRealization
		
		traceability.putPort(port, gammaPort)
		
		return gammaPort
	}
	
}