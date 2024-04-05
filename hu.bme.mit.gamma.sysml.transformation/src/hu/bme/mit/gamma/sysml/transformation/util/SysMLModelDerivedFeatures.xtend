package hu.bme.mit.gamma.sysml.transformation.util

import hu.bme.mit.gamma.genmodel.model.GenmodelModelFactory
import hu.bme.mit.gamma.util.GammaEcoreUtil
import org.omg.sysml.lang.sysml.AcceptActionUsage
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.ConjugatedPortDefinition
import org.omg.sysml.lang.sysml.Element
import org.omg.sysml.lang.sysml.ExhibitStateUsage
import org.omg.sysml.lang.sysml.Expression
import org.omg.sysml.lang.sysml.Feature
import org.omg.sysml.lang.sysml.FeatureChainExpression
import org.omg.sysml.lang.sysml.FeatureReferenceExpression
import org.omg.sysml.lang.sysml.ItemDefinition
import org.omg.sysml.lang.sysml.Package
import org.omg.sysml.lang.sysml.PartUsage
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.ReferenceUsage
import org.omg.sysml.lang.sysml.RequirementUsage
import org.omg.sysml.lang.sysml.SendActionUsage
import org.omg.sysml.lang.sysml.StateUsage
import org.omg.sysml.lang.sysml.SysMLPackage
import org.omg.sysml.lang.sysml.TriggerInvocationExpression
import org.omg.sysml.lang.sysml.TriggerKind
import org.omg.sysml.lang.sysml.Type
import org.omg.sysml.lang.sysml.Usage
import org.omg.sysml.util.FeatureUtil

class SysMLModelDerivedFeatures {
	
	protected static final extension GammaEcoreUtil ecoreUtil = GammaEcoreUtil.INSTANCE
	
	//
	
	static def isCompositePart(Usage part) {
		return !part.isAtomicPart
	}
	
	static def isAtomicPart(Usage part) {
		return !part.getAllFeatures(ExhibitStateUsage).empty
	}
	
	static def isCompositeState(StateUsage state) {
		return !state.nestedState.empty
	}
	
	static def getParts(Type type) {
		return type.getAllFeatures(PartUsage) // Some connections are also part usages
				.filter[it.eClass.classifierID == SysMLPackage.PART_USAGE]
				.toList
	}
	
	//
	
	static def isRequired(PortUsage portUsage) {
		val portDefinition = portUsage.portDefinition.head
		return portDefinition instanceof ConjugatedPortDefinition
	}
	
	static def getProvidedPortDefinition(PortDefinition portDefinition) {
		if (portDefinition instanceof ConjugatedPortDefinition) {
			return portDefinition.originalPortDefinition
		}
		return portDefinition
	}
	
	static def getProvidedPortDefinition(PortUsage portUsage) {
		val portDefinition = portUsage.portDefinition.head
		return portDefinition.providedPortDefinition
	}
	
	static def getPortUsage(AcceptActionUsage trigger) {
		val receiver = trigger.receiverArgument
		return receiver.portUsage
	}
	
	static def getAfterTriggerExpression(AcceptActionUsage trigger) {
		val argument = trigger.payloadArgument
		if (argument instanceof TriggerInvocationExpression) {
			val triggerKind = argument.kind
			if (triggerKind == TriggerKind.AFTER) {
				val features = argument.ownedFeature
				val value = features.head // SysML maybe should put this instead into argument.argument.head?
				return value as Expression
			}
		}
		return null
	}
	
	static def getPortUsage(SendActionUsage trigger) {
		val sender = trigger.senderArgument
		if (sender !== null) {
			return sender.portUsage
		}
		
		val receiver = trigger.receiverArgument
		return receiver.portUsage
	}
	
	static def getPortUsage(Expression reference) {
		if (reference instanceof FeatureReferenceExpression) {
			val value = reference?.referent
			switch (value) {
				PortUsage:
					return value
				ReferenceUsage: {
					val types = value?.type
					val type = types?.head
					return type as PortUsage
				}
				default:
					return null
			}
		}
		return null
	}
	
	//
	
	static def getEventType(Expression payloadArgument) {
		val ownedTyping = payloadArgument.ownedTyping.head // Works
		val itemType = ownedTyping?.type
		if (itemType !== null) {
			return itemType // ItemDefinition
		}
		val basicType = payloadArgument.type.head // E.g., Integer, Boolean
		return basicType
	}
	
	static def isBasicEvent(Expression payloadArgument) {
		val type = payloadArgument.eventType
		return type.isBasicEvent
	}
	
	static def isBasicEvent(Type type) {
		return !type.itemEvent
	}
	
	static def isItemEvent(Type type) {
		return type instanceof ItemDefinition
	}
	
	static def hasEventParameter(Type type) {
		return !type.eventParameters.empty
	}
	
	static def getEventParameters(Type type) {
		return type.getAllFeatures(AttributeUsage)
	}
	
	//
	
	static def getCommonTypeName(Type type) {
		val name = type.name
		switch (name) {
			case "Boolean",
			case "LiteralBooleanEvaluation": {
				return "Boolean"
			}
			case "Integer",
			case "LiteralIntegerEvaluation": {
				return "Integer"
			}
			case "Rational",
			case "LiteralRationalEvaluation" : {
				return "Rational"
			}
			case "Real",
			case "LiteralRealEvaluation": {
				return "Real"
			}
			default: 
				return name
		}
	}
	
	//
	
	static def <T extends Feature> getAllFeatures(Type usage, Class<T> type) {
		val internalFeatures = <T>newLinkedHashSet
		val redefinitions = newArrayList
		
		val features = usage.feature // Does not return private members
		val nonAbstractFeatures = features
				.reject[it.libraryElement]
		val typedFeatures = nonAbstractFeatures
				.filter(type)
		val referenceFeatures = nonAbstractFeatures
				.filter(ReferenceUsage)
		
		// Note that this way, ReferenceUsages redefining features of Type "type" are handled
		for (feature : typedFeatures + referenceFeatures) {
			val featureRedefinitions = feature.ownedRedefinition
			if (!featureRedefinitions.empty) {
				// We will consider the redefined internal feature
				redefinitions += featureRedefinitions
			}
			else if (typedFeatures.contains(feature)) {
				// We consider the "original" feature as it does not contain a redefinition
				internalFeatures += feature as T
			}
			// Else if it is a ReferenceUsages without a redefinition, we do not do anything
		}
		
		// TODO reorganize if redefinitions are reworked
		// Collecting the internal features of the redefinitions,
		// replacing the "redefining" features with the "redefined" ones
		for (redefinition : redefinitions) {
			val redefinedFeature = redefinition.redefinedFeature
			if (type.isInstance(redefinedFeature)) {
				internalFeatures += redefinedFeature as T
			}
			// We do not use featureHierarchyHead or lastRedefined feature as we collect the internal
			// (redefined) features, which are retrieved correctly if a featureHierarchy is used
		}
		
		return internalFeatures
	}
	
	static def isLibraryElement(Element element) {
		return element.eResource.file
				.projectFile.name == "sysml.library"
	}
	
	static def getSecondLevelRedefinitions(Usage usage) {
		return usage // 0. level: root
				.eContents // 1. level: FeatureMembership
				.map[it.eContents] // 2. level: AttributeUsage
				.flatten.filter(Feature)
				.map[it.ownedRedefinition]
				.flatten.toList
	}
	
	//
	
	static def getFeatures(FeatureChainExpression expression) {
		val featureReferenceExpressions = expression.operand
				.filter(FeatureReferenceExpression)
				.toList
		// Strange SysML chaining solution
		val features = newArrayList
		features += featureReferenceExpressions
				.map[it.referent]
		val targetFeature = expression.targetFeature
		val chainingFeature = targetFeature.chainingFeature
		features += (chainingFeature.empty) ?
				#[targetFeature] : chainingFeature
		
		return features
	}
	
	static def getFeatureHierarchyHead(Feature feature) {
		val featureChain = newArrayList
		featureChain += feature.chainingFeature
		
		if (!featureChain.empty) {
			val last = featureChain.last
			featureChain -= last
		}
		return featureChain
	}
	
	static def getLastFeature(Feature feature) {
		val featureChain = feature.chainingFeature
		
		if (featureChain.empty) {
			return feature
		}
		val last = featureChain.last
		return last
	}
	
	// Verification & requirements
	
	static def asTestGeneration(RequirementUsage objective) {
	 	val GenmodelModelFactory factory = GenmodelModelFactory.eINSTANCE
		
		val name = objective.type.head?.name
		val packageName = objective.type.head?.getSelfOrContainerOfType(Package)?.name
		
		if (name == "TestGenerationObjective" && packageName == "GammaLib") {
			val features = objective.feature
			val coverageKind = features.filter[it.effectiveName == "coverage"].head
			var coverageFeatureRef = FeatureUtil.getValuationFor(coverageKind).value
			var coverage = coverageFeatureRef.evaluate(objective).head
			if (coverage instanceof Usage) {
				switch (coverage.name) {
					case "State": {
						return factory.createStateCoverage
					}
					case "Transition" : {
						return factory.createTransitionCoverage
					}
					case "TransitionPair" : {
						return factory.createTransitionPairCoverage
					}
					default: {
						throw new Exception("No coverage criteria has been specified for the test generation objective." + 
							" Make sure to pass it as a parameter or redefine the \"coverage\" attribute.")
					}
				}
			}
		}
		
		return null
	}
}