package hu.bme.mit.gamma.sysml.transformation.util

import hu.bme.mit.gamma.util.GammaEcoreUtil
import hu.bme.mit.gamma.util.JavaUtil
import java.util.List
import java.util.Set
import org.omg.sysml.lang.sysml.FeatureValue
import org.omg.sysml.lang.sysml.Redefinition

import static com.google.common.base.Preconditions.checkState

import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class RedefinitionHandler {
	
//	protected final Usage usage
	
	protected final List<Redefinition> redefinitions = newArrayList
	protected final List<Pair<FeatureValue, FeatureValue>> redefinedValues = newArrayList
	protected final Set<FeatureValue> dummies = newHashSet
	
	protected final extension GammaEcoreUtil ecoreUtil = GammaEcoreUtil.INSTANCE
	protected final extension JavaUtil javaUtil = JavaUtil.INSTANCE
	
//	new(Usage usage) {
//		this.usage = usage
//	}
	
	def executeRedefinition(Iterable<? extends Redefinition> redefinitions) {
//		val redefinitions = usage.secondLevelRedefinitions
//		
		for (redefinition : redefinitions) {
			val redefinedFeature = redefinition.redefinedFeature
//			val featureHierarchyHead = redefinedFeature.featureHierarchyHead
//			
//			if (!featureHierarchyHead.empty) {
//				// If redefinition is not directly nested into the target usage,
//				// we remove the first feature and move it one "level" closer to the target usage
//				val featureMembership = redefinition
//						.getContainerOfType(FeatureMembership)
//				val clonedFeatureMembership = featureMembership.clone
//				featureMembership.remove
//				
//				val clonedRedefinition = clonedFeatureMembership
//						.getAllContentsOfType(Redefinition).onlyElement
//				val clonedRedefinedFeature = clonedRedefinition.redefinedFeature
//				val clonedFeatureHierarchyHead = clonedRedefinedFeature.featureHierarchyHead
//				val extendableFeature = clonedFeatureHierarchyHead.head
//				
//				val clonedFeatureChainings = clonedRedefinedFeature
//						.getAllContentsOfType(FeatureChaining)
//				val clonedFirstFeatureChainElement = clonedFeatureChainings.head
//				clonedFirstFeatureChainElement.remove // FeatureChaining - remove first usage
//				
//				// Moving the redefinition one "level" closer to the target usage
//				extendableFeature.ownedRelationship += clonedFeatureMembership
//			}
//			else {
				this.redefinitions += redefinition
				// We are in the target usage
				val lastRedefinedFeature = redefinedFeature.lastFeature
				
				val redefinedFeatureValues = lastRedefinedFeature
						.ownedMembership
						.filterIntoList(FeatureValue)
				
				val size = redefinedFeatureValues.size
				
				val redefiningFeature = redefinition.redefiningFeature
				val redefiningFeatureValues = redefiningFeature
						.ownedMembership
						.filterIntoList(FeatureValue)
				
				// TODO handle bindings and initial values, etc.
				
				if (size == 0 && redefiningFeatureValues.size == 1) {
					// Sizes are different because the original has no initial value
					val redefiningFeatureValue = redefiningFeatureValues.head
					val dummy = redefiningFeatureValue.clone
					
					// Adding the placeholder
					lastRedefinedFeature.ownedRelationship += dummy
					redefinedFeatureValues += dummy
					dummies += dummy
				}
				else {
					checkState(size == redefiningFeatureValues.size)
				}
				
				// Maybe we could replace all content not just feature values
				
				for (var i = 0; i < size; i++) {
					val redefinedFeatureValue = redefinedFeatureValues.get(i)
					val redefiningFeatureValue = redefiningFeatureValues.get(i)
					
					redefinedValues += redefinedFeatureValue -> redefiningFeatureValue
					redefiningFeatureValue.replaceEachOther(redefinedFeatureValue)
					// TODO Change does not work due to the SysML DerivedEObjectELists and BasicInternalEList
				}
//			}
		}
	}
	
	def undoRedefinition() {
		for (redefinedValue : redefinedValues) {
			val redefinedFeatureValue = redefinedValue.key
			val redefiningFeatureValue = redefinedValue.value
			
			redefinedFeatureValue.replaceEachOther(redefiningFeatureValue)
		}
		// Removing the placeholders
		for (dummy : dummies) {
			dummy.remove
		}
	}
	
	def getRedefinitions() {
		return redefinitions
	}
	
}