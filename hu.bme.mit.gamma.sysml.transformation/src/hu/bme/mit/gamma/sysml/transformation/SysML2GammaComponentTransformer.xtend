package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.statechart.composite.AsynchronousAdapter
import hu.bme.mit.gamma.statechart.composite.AsynchronousComponent
import hu.bme.mit.gamma.statechart.interface_.Component
import hu.bme.mit.gamma.statechart.interface_.EventReference
import hu.bme.mit.gamma.statechart.statechart.AnyPortEventReference
import hu.bme.mit.gamma.statechart.statechart.PortEventReference
import hu.bme.mit.gamma.sysml.transformation.util.FeatureHierarchy
import hu.bme.mit.gamma.sysml.transformation.util.RedefinitionHandler
import java.math.BigInteger
import java.util.logging.Level
import org.omg.sysml.lang.sysml.BindingConnectorAsUsage
import org.omg.sysml.lang.sysml.ConnectionUsage
import org.omg.sysml.lang.sysml.Feature
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.Usage

import static com.google.common.base.Preconditions.checkState

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class SysML2GammaComponentTransformer extends CompositeElementTransformer {
	
	protected final Usage part
	//
	
	new(Usage part) {
		super(new CompositeTraceability(part))
		this.part = part
	}
	
	def execute() {
		val partHierarchy = new FeatureHierarchy(part)
		// Creating traceability for atomic components to handle redefinitions
		partHierarchy.traceRedefinition
		// Transforming the hierarchy
		val rootComponent = partHierarchy.transform as AsynchronousComponent
		
		// Handling the internal/external event references of message queues
		rootComponent.handleMessageQueues
		
		if (rootComponent instanceof AsynchronousAdapter) {
			// Manual instantiation: if we have simple statechart, we set the external reference
			traceability.put(partHierarchy, rootComponent.wrappedComponent)
		}
		
		traceability.rootComponent = rootComponent
		
		return traceability
	}
	
	//
	
	protected def traceRedefinition(FeatureHierarchy partHierarchy) {
		val lastPart = partHierarchy.last as Usage
		val isAtomic = lastPart.atomicPart
		if (isAtomic) {
			partHierarchy.traceAtomicRedefinition
		}
		else {
			checkState(lastPart.compositePart)
			partHierarchy.traceCompositeRedefinition
		}
	}
	
	protected def void traceAtomicRedefinition(FeatureHierarchy partHierarchy) {
		val part = partHierarchy.last as Usage
		
		// Creating the traceability in advance to support the tracing of redefinitions
		val statechartTraceability = traceability.createStatechartTraceability(part)
		traceability.putTraceability(partHierarchy, statechartTraceability)
		
		val redefinitions = part.secondLevelRedefinitions
		for (redefinition : redefinitions) {
			// Atomic part, no feature hierarchy
			statechartTraceability.putRedefinition(redefinition)
		}
	}
	
	protected def void traceCompositeRedefinition(FeatureHierarchy partHierarchy) {
		val part = partHierarchy.last as Usage
		// Instances
		val subparts = part.parts
		for (subpart : subparts) {
			val extendedHierarchy = partHierarchy.cloneAndAdd(subpart)
			extendedHierarchy.traceRedefinition // Recursion
		}
		
		// Tracing the redefinitions
		val redefinitions = part.secondLevelRedefinitions
		for (redefinition : redefinitions) {
			// Composite part, there is a feature hierarchy
			traceability.putRedefinition(partHierarchy, redefinition)
		}
	}
	
	//
	
	protected def transform(FeatureHierarchy partHierarchy) {
		val lastPart = partHierarchy.last as Usage
		val isAtomic = lastPart.atomicPart
		if (isAtomic) {
			return partHierarchy.transformAtomic
		}
		else {
			checkState(lastPart.compositePart)
			return partHierarchy.transformComposite
		}
	}
	
	protected def Component transformComposite(FeatureHierarchy partHierarchy) {
		val part = partHierarchy.last as Usage
		logger.log(Level.INFO, "Transforming composite part definition " + part.name)
		
		// Manually redefine features
		val redefinitions = traceability.getRedefinitions(partHierarchy)
		
		val redefiner = new RedefinitionHandler
		redefiner.executeRedefinition(redefinitions)
		//
		
		val gammaComposite = createScheduledAsynchronousCompositeComponent
		gammaComposite.name = partHierarchy.componentName
		
		// Ports
		val portTraceability = traceability.createStatechartTraceability(part)
		val extension portTransformer = new PortTransformer(portTraceability)
		val ports = part.getAllFeatures(PortUsage)
		for (port : ports) {
			val gammaPort = port.transformPort
			gammaComposite.ports += gammaPort
			traceability.put(partHierarchy -> port, gammaPort)
		}
		
		// Instances
		val subparts = part.parts
		for (subpart : subparts) {
			val extendedHierarchy = partHierarchy.cloneAndAdd(subpart)
			
			val gammaSubcomponentType = extendedHierarchy.transform as AsynchronousComponent
			val gammaSubcomponent = gammaSubcomponentType.instantiateAsynchronousComponent
			gammaSubcomponent.name = subpart.instanceName
			gammaComposite.components += gammaSubcomponent
			
			traceability.put(extendedHierarchy, gammaSubcomponent)
		}
		
		// Port bindings
		val bindings = part.getAllFeatures(BindingConnectorAsUsage)
		for (binding : bindings) {
			var PortUsage systemPortUsage = null
			val instancePortUsages = newArrayList
			
			val sourceFeature = binding.sourceFeature
			if (sourceFeature instanceof PortUsage) {
				// control = bind controller.controlExt;
				systemPortUsage = sourceFeature
				instancePortUsages += binding.targetFeature
			}
			else {
				// bind controller.controlExt = control;
				val targetFeature = binding.targetFeature.head
				systemPortUsage = targetFeature as PortUsage
				instancePortUsages += binding.sourceFeature
			}
			
			val gammaSourcePort = traceability.get(partHierarchy -> systemPortUsage)
			
			for (target : instancePortUsages) {
				val gammaInstancePortReference =
						partHierarchy.createInstancePortReference(target)
				val gammaPortBinding = gammaSourcePort.createPortBinding(
						gammaInstancePortReference)
				gammaComposite.portBindings += gammaPortBinding
			}
		}
		
		// Channels
		var channels = part.getAllFeatures(ConnectionUsage)
		for (channel : channels) {
			val source = channel.sourceFeature
			val gammaSourceInstancePortReference =
						partHierarchy.createInstancePortReference(source)
			
			val targets = channel.targetFeature
			for (target : targets) {
				val gammaTargetInstancePortReference =
						partHierarchy.createInstancePortReference(target)
				val gammaChannel = gammaSourceInstancePortReference
						.createChannel(gammaTargetInstancePortReference)
				gammaComposite.channels += gammaChannel
				// Checking provided/required
				if (gammaTargetInstancePortReference.provided) {
					gammaTargetInstancePortReference
							.replaceEachOther(gammaSourceInstancePortReference)
				}
			}
		}
		
		// Tracing the redefinition
		traceability.traceRedefiningFeatures(partHierarchy, redefinitions)
		// Undo manual redefinition
		redefiner.undoRedefinition
		//
		return gammaComposite
	}
	
	protected def Component transformAtomic(FeatureHierarchy partHierarchy) {
		val part = partHierarchy.last as Usage
		
		val statechartTraceability = traceability.getTraceability(partHierarchy) // Already traced
		
		// Manually redefine features
		val redefinitions = statechartTraceability.getRedefinitions
		
		val redefiner = new RedefinitionHandler
		redefiner.executeRedefinition(redefinitions)
		//
		
		val statechartName = partHierarchy.statechartName
		val statechartTransformer = new SysML2GammaStatechartTransformer(
				statechartTraceability, statechartName)
		logger.log(Level.INFO, "Transforming " + part.name + " as a statechart")
		statechartTransformer.execute
		
		// Relaying the statechart ports manually
		val statechartPorts = statechartTraceability.getPorts
		for (portUsage : statechartPorts.keySet // Not handling redefinition-contained references
					.filter(PortUsage)) {
			val gammaPort = statechartPorts.get(portUsage)
			traceability.put(partHierarchy -> portUsage, gammaPort)
		}
		
		// Handling redefinitions in the traceability
		statechartTraceability.traceRedefiningFeatures(redefinitions)
		// Undo manual redefinition
		redefiner.undoRedefinition
		//
		return statechartTraceability.getAdapter
	}
	
	///
	
	private def createInstancePortReference(FeatureHierarchy featureHierarchy, Feature feature) {
		val chain = feature.ownedFeatureChaining
		
		val first = chain.head
		val partUsage = first.chainingFeature as Usage
		val extendedFeatureHierarchy = featureHierarchy.cloneAndAdd(partUsage)
		
		val gammaInstance = traceability.get(extendedFeatureHierarchy)
		
		val second = chain.last
		val targetPortUsage = second.chainingFeature as PortUsage
		val gammaTargetPort = traceability.get(extendedFeatureHierarchy -> targetPortUsage)
		
		val gammaInstancePort =
				gammaInstance.createInstancePortReference(gammaTargetPort)
		return gammaInstancePort		
	}
	
	//
	
	protected def void handleMessageQueues(AsynchronousComponent rootComponent) {
		if (rootComponent.adapter) {
			return // We have to handle only composite components
		}
		
		val simpleSystemPorts = rootComponent.allBoundAsynchronousSimplePorts
		val adapterInstances = rootComponent.allAsynchronousSimpleInstances
		val queues = adapterInstances.map[it.type].filter(AsynchronousAdapter)
				.map[it.messageQueues].flatten.toList
		for (queue : queues) {
			val systemEventReferences = <EventReference>newLinkedHashSet
			val eventReferences = queue.sourceEventReferences
			for (eventReference : eventReferences) {
				if (eventReference instanceof AnyPortEventReference) {
					if (simpleSystemPorts.contains(eventReference.port)) {
						systemEventReferences += eventReference
					}
				}
				else if (eventReference instanceof PortEventReference) {
					if (simpleSystemPorts.contains(eventReference.port)) {
						systemEventReferences += eventReference
					}
				}
				else {
					throw new IllegalArgumentException("Not known event reference: " + eventReference)
				}
			}
			if (!systemEventReferences.containsAll(eventReferences) && !systemEventReferences.empty) {
				val asynchronousAdapter = queue.getContainerOfType(AsynchronousAdapter)
				val partUsage = traceability.getStatecharts.values
						.findFirst[it.adapter == asynchronousAdapter].getRoot
				logger.log(Level.WARNING, "Internal and external messages are mixed in " +
						asynchronousAdapter.name + "." + queue.name)
				val systemQueue = queue.clone
				systemQueue.name  = partUsage.environmentMessageQueueName
				asynchronousAdapter.messageQueues += systemQueue
				systemQueue.eventPassings.clear
				systemQueue.eventPassings += systemEventReferences.map[source |
						createEventPassing => [it.source = source]
				]
				// System queue will have a higher priority
				systemQueue.priority = queue.priority.add(BigInteger.ONE)
			}
		}
	}
	
}