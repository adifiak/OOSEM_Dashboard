package hu.bme.mit.gamma.sysml.transformation.util

import java.util.Collection
import java.util.List
import org.omg.sysml.lang.sysml.Feature

class FeatureHierarchy {
	//
	List<Feature> features = newArrayList
	//
	
	new(FeatureHierarchy features) {
		this.features += features.features
	}
	
	new(List<Feature> features) {
		this.features += features
	}
	
	new(Feature feature) {
		this.features += feature
	}
	
	new() {}
	
	//
	
	def List<Feature> getFeatures() {
		return features
	}
	
	def void prepend(Feature feature) {
		features.add(0, feature)
	}
	
	def void prepend(FeatureHierarchy featureHierarchy) {
		features.addAll(0, featureHierarchy.features)
	}
	
	def void add(Feature feature) {
		features += feature
	}
	
	def void add(Collection<? extends Feature> features) {
		this.features += features
	}
	
	def void add(FeatureHierarchy featureHierarchy) {
		features += featureHierarchy.features
	}
	
	def int size() {
		return features.size
	}
	
	def Feature getFirst() {
		return features.head
	}
	
	def Feature getLast() {
		return features.last
	}
	
	def boolean isEmpty() {
		return features.isEmpty
	}
	
	def Feature removeFirst() {
		return features.remove(0)
	}
	
	def Feature removeLast() {
		return features.remove(features.size - 1)
	}
	
	def indexOf(Feature feature) {
		return features.indexOf(feature)
	}
	
	def set(int index, Feature feature) {
		features.set(index, feature)
	}
	
	def boolean startsWith(FeatureHierarchy featureHierarchy) {
		val features = featureHierarchy.features
		for (var i = 0; i < features.size; i++) {
			val ownFeature = this.features.get(i)
			val otherFeature = features.get(i)
			if (ownFeature !== otherFeature) {
				return false
			}
		}
		return true
	}
	
	override FeatureHierarchy clone() {
		return new FeatureHierarchy(this)
	}
	
	def FeatureHierarchy cloneAndAdd(Feature feature) {
		val newHierarchy = this.clone
		newHierarchy.add(feature)
		return newHierarchy
	}
	
	def FeatureHierarchy cloneAndAdd(Collection<? extends Feature> features) {
		val newHierarchy = this.clone
		newHierarchy.add(features)
		return newHierarchy
	}
	
	override int hashCode() {
		val prime = 31
		var result = 1
		result = prime * result + ((features === null) ? 0 : features.hashCode)
		return result
	}

	override boolean equals(Object obj) {
		if (this === obj) {
			return true
		}
		if (obj === null) {
			return false
		}
		if (getClass() != obj.class) {
			return false
		}
		val other = obj as FeatureHierarchy
		if (features === null) {
			if (other.features !== null) {
				return false
			}
		}
		else if (!features.equals(other.features)) {
			return false
		}
		return true
	}
}