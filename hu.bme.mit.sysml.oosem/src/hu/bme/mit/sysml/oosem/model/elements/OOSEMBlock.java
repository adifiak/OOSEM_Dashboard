package hu.bme.mit.sysml.oosem.model.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

public class OOSEMBlock extends OOSEMElement{
	public OOSEMBlock(EObject o) {
		super(o);
	}
	
	public void registerChildren(OOSEMBlock child) {
		childs.add(child);
	}
	
	public void registerParent(OOSEMBlock parent) {
		parents.add(parent);
	}
	
	public Set<OOSEMBlock> getParents() {
		return parents;
	}
	
	public Set<OOSEMBlock> getChilds() {
		return childs;
	}
	
	public void registerProperty(OOSEMFeature feature) {
		properties.add(feature);
	}
	
	public void registerSubsystem(OOSEMFeature feature) {
		subsystems.add(feature);
	}
	
	public void registerUntrackedFeature(OOSEMFeature feature) {
		untrackedFeatures.add(feature);
	}
	
	public void registerInheritedProperty(OOSEMFeature feature) {
		registerInheretedFeature(properties, feature);
	}
	
	public void registerInheritedSubsystem(OOSEMFeature feature) {
		registerInheretedFeature(subsystems, feature);
	}
	
	public void registerInheritedUntrackedFeature(OOSEMFeature feature) {
		registerInheretedFeature(untrackedFeatures, feature);
	}
	
	public void redefineFeature(OOSEMFeature oldFeature, OOSEMFeature newFeature) {
		var inheretedVersion = inheritanceMapping.get(oldFeature);
		if(inheretedVersion != null)
			oldFeature = inheretedVersion;
		
		replaceIfPresent(properties, oldFeature, newFeature);
		replaceIfPresent(subsystems, oldFeature, newFeature);
		replaceIfPresent(untrackedFeatures, oldFeature, newFeature);
	}
	
	public Set<OOSEMFeature> getProperties() {
		return properties;
	}
	
	public Set<OOSEMFeature> getSubsystems() {
		return subsystems;
	}
	
	public Set<OOSEMFeature> getUntrackedFeatures() {
		return untrackedFeatures;
	}
	
	private Map<OOSEMFeature, OOSEMFeature> inheritanceMapping = new HashMap<>();

	private Set<OOSEMBlock> parents = new HashSet<OOSEMBlock>();
	private Set<OOSEMBlock> childs = new HashSet<OOSEMBlock>();
	
	private Set<OOSEMFeature> properties = new HashSet<OOSEMFeature>();
	private Set<OOSEMFeature> subsystems = new HashSet<OOSEMFeature>();
	private Set<OOSEMFeature> untrackedFeatures = new HashSet<OOSEMFeature>();
	
	private void registerInheretedFeature(Set<OOSEMFeature> featureSet, OOSEMFeature featureToInherit) {
		var inheretedFeature = new OOSEMFeature(featureToInherit);
		inheritanceMapping.put(inheretedFeature.getCopiedFeature(), inheretedFeature);
		featureSet.add(inheretedFeature);
	}
	
	private void replaceIfPresent(Set<OOSEMFeature> container, OOSEMFeature oldFeature, OOSEMFeature newFeature) {
		if (!container.remove(oldFeature)) return;
		container.add(newFeature);
	}
}
