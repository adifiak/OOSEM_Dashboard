package hu.bme.mit.kerml.atomizer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.Type;

public class Atom {
	private String name;
	int id;

	private Set<Feature> explicitFeatures = new HashSet<>();
	private Map<Feature, Extent> effectiveFeatures = new HashMap<>();
	private ExtentManager extentManager;
	private static Map<String, Integer> idCounters = new HashMap<>();
	private static List<Atom> allAtoms = new ArrayList<>();

	public Atom(String name, ExtentManager extentManager) {
		this.name = name;
		this.id = idCounters.getOrDefault(name, 0);
		idCounters.put(name, this.id + 1);
		this.extentManager = extentManager;
		System.out.println("instantiated Atom: " + name + id);
		allAtoms.add(this);
	}

	public Set<Type> getTypes() {
		return extentManager.getTypesOf(this);
	}

	public String getName() {
		return name + id;
	}

	public boolean hasExplicitFeatures() {
		return !explicitFeatures.isEmpty();
	}
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean verbose) {
		if (!verbose) {
			return name + id;
		}
		StringJoiner types = new StringJoiner(", ");
		for (Type type : getTypes()) {
			if (type instanceof Feature) {
				types.add(((Feature) type).getFeaturingType().stream().findFirst().map(x -> x.getDeclaredName())
						.orElseGet(() -> "Anything") + "::" + type.getDeclaredName());
			} else {
				types.add(type.getDeclaredName());
			}
		}
		return name + id + " (in extent of: " + types + ")";
	}

	public void addFeature(Feature feature) {
		explicitFeatures.add(feature);
		if (!effectiveFeatures.containsKey(feature)) {
			extentManager.getExtent(feature, this);
		}
	}

	public void addFeatureExtent(Feature feature, Extent extent) {
		assert !effectiveFeatures.containsKey(feature);
		effectiveFeatures.put(feature, extent);
		extentManager.registerFeatureExtent(this, effectiveFeatures);
	}

	public static void reset() {
		idCounters = new HashMap<>();
		allAtoms = new ArrayList<>();
	}

	public static List<Atom> getAllAtoms() {
		return allAtoms;
	}

	public Map<Feature, Extent> getEffectiveFeatures() {
		return effectiveFeatures;
	}

	public Set<Feature> getExplicitFeatures() {
		return explicitFeatures;
	}

	public Type getMostSpecificNonFeatureType() {
		Type mostSpecific = null;
		for (Type t : getTypes()) {
			if (!(t instanceof Feature)) {
				if (mostSpecific == null
						|| extentManager.getExtent(mostSpecific, null).isSupersetOf(extentManager.getExtent(t, null))) {
					mostSpecific = t;
				}
			}
		}
		if (mostSpecific == null) {
			throw new RuntimeException("Could not find any non-feature extents for atom " + toString());
		}
		return mostSpecific;
	}
	
	public static Atom get(String identifier) {
		for (Atom a : allAtoms) {
			if (a.toString(false).equals(identifier)) {
				return a;
			}
		}
		return null;
	}
}
