package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.EnumerationLiteralDefinition
import hu.bme.mit.gamma.expression.model.TypeDeclaration
import hu.bme.mit.gamma.statechart.composite.ComponentInstance
import hu.bme.mit.gamma.statechart.interface_.Component
import hu.bme.mit.gamma.statechart.interface_.Event
import hu.bme.mit.gamma.statechart.interface_.Interface
import hu.bme.mit.gamma.statechart.interface_.Port
import hu.bme.mit.gamma.sysml.transformation.util.FeatureHierarchy
import hu.bme.mit.gamma.util.GammaEcoreUtil
import hu.bme.mit.gamma.util.JavaUtil
import java.util.ArrayList
import java.util.List
import java.util.Map
import org.omg.sysml.lang.sysml.DataType
import org.omg.sysml.lang.sysml.EnumerationUsage
import org.omg.sysml.lang.sysml.PartUsage
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.Redefinition
import org.omg.sysml.lang.sysml.ReferenceUsage
import org.omg.sysml.lang.sysml.Usage

import static com.google.common.base.Preconditions.checkNotNull

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class CompositeTraceability {
	
	protected final Usage root // External usage
	protected Component rootComponent // Resulting root component
	
	protected final Map<FeatureHierarchy, List<Redefinition>> redefinitions = newHashMap
	
	protected final Map<FeatureHierarchy, ComponentInstance> instances = newHashMap // Internal
	protected final Map<FeatureHierarchy, StatechartTraceability> statecharts = newHashMap // Internal statecharts
	
	protected final Map<Pair<FeatureHierarchy, PortUsage>, Port> ports = newHashMap // Instance ports
	
	protected final Map<DataType, TypeDeclaration> typeDeclarations = newHashMap // Global
	protected final Map<EnumerationUsage, EnumerationLiteralDefinition> enumLiterals = newHashMap // Global
	protected final Map<PortDefinition, Interface> interfaces = newHashMap  // Global
	protected final Map<Pair<PortDefinition, String>, Event> inEvents = newHashMap  // Global
	protected final Map<Pair<PortDefinition, String>, Event> outEvents = newHashMap  // Global

	//
	
	protected final extension JavaUtil javaUtil = JavaUtil.INSTANCE
	protected final extension GammaEcoreUtil ecoreUtil = GammaEcoreUtil.INSTANCE
	
	//
	
	new(Usage root) {
		this.root = root
	}
	
	//
	
	def getRoot() {
		return root
	}
	
	def getRootComponent() {
		return rootComponent
	}
	
	def setRootComponent(Component rootComponent) {
		this.rootComponent = rootComponent
	}
	
	// 
	
	def putTraceability(FeatureHierarchy featureHierarchy, StatechartTraceability statechartTraceability) {
		checkNotNull(featureHierarchy)
		checkNotNull(statechartTraceability)
		statecharts += featureHierarchy -> statechartTraceability
	}
	
	def getTraceability(FeatureHierarchy featureHierarchy) {
		checkNotNull(featureHierarchy)
		val statechartTraceability = statecharts.get(featureHierarchy)
		checkNotNull(statechartTraceability)
		return statechartTraceability
	}
	
	def containsTraceability(FeatureHierarchy featureHierarchy) {
		checkNotNull(featureHierarchy)
		return statecharts.containsKey(featureHierarchy)
	}
	
	//
	
	def put(FeatureHierarchy featureHierarchy, ComponentInstance componentInstance) {
		checkNotNull(featureHierarchy)
		checkNotNull(componentInstance)
		instances += featureHierarchy -> componentInstance
	}
	
	def get(FeatureHierarchy featureHierarchy) {
		checkNotNull(featureHierarchy)
		val componentInstance = instances.get(featureHierarchy)
		checkNotNull(componentInstance)
		return componentInstance
	}
	
	def contains(FeatureHierarchy featureHierarchy) {
		checkNotNull(featureHierarchy)
		return instances.containsKey(featureHierarchy)
	}
	
	//
	
	def put(Pair<FeatureHierarchy, PortUsage> portUsage, Port port) {
		// portUsage can be null: default port
		checkNotNull(port)
		ports += portUsage -> port
	}
	
	def get(Pair<FeatureHierarchy, PortUsage> portUsage) {
		// portUsage can be null: default port
		val port = ports.get(portUsage)
		checkNotNull(port)
		return port
	}
	
	def contains(Pair<FeatureHierarchy, PortUsage> portUsage) {
		// portUsage can be null: default port
		return ports.containsKey(portUsage)
	}
	
	//
	
	def putRedefinition(FeatureHierarchy originalFeatureHierarchy, Redefinition redefinition) {
		checkNotNull(originalFeatureHierarchy)
		checkNotNull(redefinition)
		val fullFeatureHierarchyHead = redefinition.deriveFullFeatureHierarchyHead(originalFeatureHierarchy)
		if (containsTraceability(fullFeatureHierarchyHead)) {
			// Redefinition for an atomic part
			val statechartTraceability = getTraceability(fullFeatureHierarchyHead)
			statechartTraceability.putRedefinition(redefinition)
		}
		else {
			// Redefinition for a composite part, the last feature is a part usage, too
			val fullFeatureHierarchy = redefinition.deriveFullFeatureHierarchy(originalFeatureHierarchy)
			val redefinitionList = redefinitions.getOrCreateList(fullFeatureHierarchy)
			redefinitionList += redefinition
		}
	}
	
	def getRedefinitions(FeatureHierarchy featureHierarchy) {
		checkNotNull(featureHierarchy)
		if (containsTraceability(featureHierarchy)) {
			val statechartTraceability = getTraceability(featureHierarchy)
			return statechartTraceability.getRedefinitions
		}
		else {
			val redefinitions = redefinitions.getOrCreateList(featureHierarchy)
			return redefinitions
		}
	}
	
	//
	
	def traceRedefiningFeatures(FeatureHierarchy featureHierarchy,
			Iterable<? extends Redefinition> redefinitions) {
		for (redefinition : redefinitions) {
			featureHierarchy.traceRedefiningFeature(redefinition)
		}
	}
	
	def traceRedefiningFeature(FeatureHierarchy originalFeatureHierarchy, Redefinition redefinition) {
		val fullFeatureHierarchyHead = redefinition.deriveFullFeatureHierarchyHead(originalFeatureHierarchy)
		
		val fullRedefinedFeature = redefinition.redefinedFeature
		val redefinedFeature = fullRedefinedFeature.lastFeature
		val redefiningFeature = redefinition.redefiningFeature
		for (instanceHierarchy : instances.keySet) {
			if (instanceHierarchy.startsWith(fullFeatureHierarchyHead)) {
				if (redefinedFeature instanceof PartUsage) {
					// Duplication basically with the corresponding feature changed
					val clonedInstanceHierarchy = instanceHierarchy.clone
					val index = clonedInstanceHierarchy.indexOf(redefinedFeature)
					clonedInstanceHierarchy.set(index, redefiningFeature)
					
					val instance = instances.get(instanceHierarchy)
					instances += clonedInstanceHierarchy -> instance
				}
				else {
					val statechartTraceability = getTraceability(fullFeatureHierarchyHead)
					statechartTraceability.traceRedefiningFeature(redefinition)
				}
			}
		}
	}
	
	//
	
	protected def deriveFullFeatureHierarchyHead(Redefinition redefinition,
			FeatureHierarchy originalFeatureHierarchy) {
		val originalRedefinedFeature = redefinition.redefinedFeature
		// originalRedefinedFeature can contain reference hierarchies
		val redefinedFeatureHierarchyHead = originalRedefinedFeature.featureHierarchyHead
		val featureHierarchy = originalFeatureHierarchy.cloneAndAdd(redefinedFeatureHierarchyHead)
		
		return featureHierarchy
	}
	
	protected def deriveFullFeatureHierarchy(Redefinition redefinition,
			FeatureHierarchy originalFeatureHierarchy) {
		val fullFeatureHierarchy = redefinition.deriveFullFeatureHierarchyHead(originalFeatureHierarchy)
		
		val fullRedefinedFeature = redefinition.redefinedFeature
		val redefinedFeature = fullRedefinedFeature.lastFeature
		fullFeatureHierarchy.add(redefinedFeature)
		
		return fullFeatureHierarchy
	}
	
	//
	
	def getPorts() {
		return ports
	}
	
	def getInEvents() {
		return inEvents
	}
	
	def getOutEvents() {
		return outEvents
	}
	
	def getInstances() {
		return instances
	}
	
	def getStatecharts() {
		return statecharts
	}
	
	//
	
	def getInterfaces() {
		return interfaces.values // Contains the default interface, if there is one
		// If it is a composite component, default interfaces are not permitted
	}
	
	def getTypeDeclarations() {
		return typeDeclarations.values
	}
	
	def getComponents() {
		val statecharts = statecharts.values
		return (instances.values.map[it.derivedType]
			+ statecharts.map[it.adapter] + statecharts.map[it.statechart]
		).toSet
	}
	
	//
	
	def createStatechartTraceability(Usage atomicRoot) {
		return new StatechartTraceability(atomicRoot,
			typeDeclarations, enumLiterals, interfaces, inEvents, outEvents)
	}
	
	//
	
	def updateWithAliases(List<ReferenceUsage> alternativeUsages) {
		val alternativeRedefinitions = <FeatureHierarchy, List<Redefinition>>newHashMap
		for (entry : redefinitions.entrySet) {
			for (alternativeUsage : alternativeUsages) {
				val index = entry.key.features.indexOf(alternativeUsage)
				if (index >= 0) {
					for (otherAlternativeUsage : alternativeUsages) {
						if (otherAlternativeUsage !== alternativeUsage) {
							val alternativeFeatureHierarchy = new ArrayList(entry.key.features)
							alternativeFeatureHierarchy.set(index, otherAlternativeUsage)
							alternativeRedefinitions += new FeatureHierarchy(alternativeFeatureHierarchy) -> entry.value
						}
					}
				}
			}
		}
		redefinitions += alternativeRedefinitions
		
		val alternativeInstances = <FeatureHierarchy, ComponentInstance>newHashMap
		for (entry : instances.entrySet) {
			for (alternativeUsage : alternativeUsages) {
				val index = entry.key.features.indexOf(alternativeUsage)
				if (index >= 0) {
					for (otherAlternativeUsage : alternativeUsages) {
						if (otherAlternativeUsage !== alternativeUsage) {
							val alternativeFeatureHierarchy = new ArrayList(entry.key.features)
							alternativeFeatureHierarchy.set(index, otherAlternativeUsage)
							alternativeInstances += new FeatureHierarchy(alternativeFeatureHierarchy) -> entry.value
						}
					}
				}
			}
		}
		instances += alternativeInstances
		
		val alternativeStatecharts = <FeatureHierarchy, StatechartTraceability>newHashMap
		for (entry : statecharts.entrySet) {
			for (alternativeUsage : alternativeUsages) {
				val index = entry.key.features.indexOf(alternativeUsage)
				if (index >= 0) {
					for (otherAlternativeUsage : alternativeUsages) {
						if (otherAlternativeUsage !== alternativeUsage) {
							val alternativeFeatureHierarchy = new ArrayList(entry.key.features)
							alternativeFeatureHierarchy.set(index, otherAlternativeUsage)
							alternativeStatecharts += new FeatureHierarchy(alternativeFeatureHierarchy) -> entry.value
						}
					}
				}
			}
		}
		statecharts += alternativeStatecharts
		
		val alternativePorts = <Pair<FeatureHierarchy, PortUsage>, Port>newHashMap
		for (entry : ports.entrySet) {
			for (alternativeUsage : alternativeUsages) {
				val index = entry.key.key.indexOf(alternativeUsage)
				if (index >= 0) {
					for (otherAlternativeUsage : alternativeUsages) {
						if (otherAlternativeUsage !== alternativeUsage) {
							val alternativeFeatureHierarchy = new ArrayList(entry.key.key.features)
							alternativeFeatureHierarchy.set(index, otherAlternativeUsage)
							alternativePorts += (new FeatureHierarchy(alternativeFeatureHierarchy) -> entry.key.value) -> entry.value
						}
					}
				}
			}
		}
		ports += alternativePorts
	}
	
}