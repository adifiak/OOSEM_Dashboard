package hu.bme.mit.kerml.atomizer.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.StringJoiner;
import org.eclipse.emf.common.util.EList;
import org.omg.sysml.lang.sysml.Association;
import org.omg.sysml.lang.sysml.Classifier;
import org.omg.sysml.lang.sysml.Connector;
import org.omg.sysml.lang.sysml.Disjoining;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public class ExtentManager {

	private static ExtentManager instance = new ExtentManager();
	private Map<Type, Extent> extents = new LinkedHashMap<Type, Extent>();
	private AtomReuseStrategy atomReuseStrategy = new AtomReuseStrategy.NoReuseStrategy();
	private Map<Feature, Set<Connector>> connMap;
	private Map<Type, Set<Association>> assocMap;
	private Map<Atom, Map<Feature, Extent>> featureExtents = new HashMap<>();

	public static ExtentManager getInstance() {
		return instance;
	}

	public void registerAssociations(Map<Type, Set<Association>> assocMap) {
		this.assocMap = assocMap;

	}

	public void registerConnectors(Map<Feature, Set<Connector>> connMap) {
		this.connMap = connMap;

	}

	public void setAtomReuseStrategy(AtomReuseStrategy atomReuseStrategy) {
		Preconditions.checkNotNull(atomReuseStrategy, "AtomReuseStrategy can not be null.");
		this.atomReuseStrategy = atomReuseStrategy;
	}

	public Extent getExtent(Type type, Atom context) {
		if (type == null) {
			throw new NullPointerException("Can't create extent for type NULL");
		}
//              String contextString =""; 
//              if(context == null) {
//                      contextString = "null";
//              }
//              else {
//                      contextString = context.toString();
//              }

		System.out.println("getting extent of " + type.getName() + " in context of " + context);
		if (type.effectiveName().equals("holdsWheel")) {
			System.out.println("now we hold the wheel...");
		}
		if (context == null && extents.containsKey(type)) { // TODO: Mi van, ha van kontextus????
			return extents.get(type);
		}
		
		if (context != null && context.getEffectiveFeatures().containsKey(type)) {
			return context.getEffectiveFeatures().get(type); // TODO: wrapper, hogy az addFeatureExtenthez hasonlóan
																// nézzen ki
		}

		Extent extent = new Extent(this, context, type);
		System.out.println(
				"getExtent: instantiated extent for type: " + type.getDeclaredName() + " " + type.effectiveName() + " hash: " + extent.hashCode());

		if (type instanceof Feature) {
			System.out.println("adding " + extent.hashCode() + " to feature extents of " + context);
			context.addFeatureExtent((Feature) type, extent);
		} else {
			System.out.println("adding " + extent.hashCode() + " to extentmanager");
			extents.put(type, extent);
		}

		for (Type t : type.allSupertypes()) {
			if (t instanceof Feature) {
				extent.addSuperset(getExtent(t, context));
			} else if (t instanceof Classifier) {
				extent.addSuperset(getExtent(t, null));
			}
		}
		for (Disjoining d : type.getOwnedDisjoining()) {
			Type t = d.getDisjoiningType();
			extent.disjoin(getExtent(t, context));
		}
		if (connMap.get(type) != null) {
			for (Connector c : connMap.get(type)) {
//				for(Feature f : c.getEndFeature()) { 
//					System.out.println("checking an end feature of the connector");
//					if (FeatureUtil.getReferencedFeatureOf(f) != type) {
//						System.out.println("this one does not match!");
//					}
//				}
				Feature otherEnd = c.getEndFeature().stream()
						.map(endFeature -> FeatureUtil.getReferencedFeatureOf(endFeature))
						.filter(feature -> feature != type)
						.findFirst().get();
				//Feature otherEnd = FeatureUtil.getReferencedFeatureOf(otherEndFeatre);
				Extent otherExtent = getExtent(otherEnd, context);
				extent.pair(otherExtent, c);
			}
		}

		if (assocMap.get(type) != null) {
			for (Association a : assocMap.get(type)) {
				Feature otherEndFeature = a.getEndFeature().stream().filter(t -> t != type).findFirst().get();
				Feature otherEnd = FeatureUtil.getReferencedFeatureOf(otherEndFeature);
				Extent otherExtent = getExtent(otherEnd, context);
				extent.pair(otherExtent, a);
			}
		}

		return extent;
	}

	public Type getType(Extent extent) {
		return extent.getType();
//              for (Map.Entry<Atom, Map<Feature, Extent>> fExtent : featureExtents.entrySet()) {
//                      for (Map.Entry<Feature, Extent> ex : fExtent.getValue().entrySet()) {
//                              if (ex == extent) {
//                                      System.out.println("Extent is managed by a feature extent in atom: " + fExtent.getKey());
//                                      return ex.getKey();
//                              }
//                      }
//              }
//              List<String> x = extent.getAtoms().stream().map(it -> it.toString()).toList();
//              throw new RuntimeException(/*extent.toString() +*/ " is not managed." + extent.hashCode() + " " + extent.count()); //TODO Baly
	}

	public Set<Type> getTypesOf(Atom atom) {
		Set<Type> result = new LinkedHashSet<>();
		for (Map.Entry<Type, Extent> entry : getExtents().entrySet()) {
			if (entry.getValue().contains(atom)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public String printState() {
		StringJoiner sj = new StringJoiner("\n");
		for (Map.Entry<Type, Extent> e : getExtents().entrySet()) {
			sj.add("Type " + e.getKey().getDeclaredName() + " has extent:\n" + e.getValue().printState());
		}
		return sj.toString();
	}

	public void reset() {
		//extents = new HashMap<>();
		instance = new ExtentManager();
	}

	public String toKermlModel() {
		StringJoiner sj = new StringJoiner("\n");
		return "";
	}

	public Collection<Atom> findSuitableAtoms(Feature feature, Atom context) {
		return atomReuseStrategy.findSuitableAtoms(feature, context, this);
	}

	public void registerFeatureExtent(Atom source, Map<Feature, Extent> atomFeatureExtents) {
		featureExtents.put(source, atomFeatureExtents);
	}

	public Map<Type, Extent> getExtents() {
		return extents;
	}

	public Collection<Extent> collectAllExtents() {
		List<Extent> r = featureExtents.values().stream().flatMap(m -> m.values().stream())
				.collect(Collectors.toList());
		r.addAll(extents.values());
		return r;
	}

	public List<Extent> findUnpairedExtent() {
		Collection<Extent> es = collectAllExtents();
		for (Extent e : es) {
			for (Extent o : e.getPaired()) {
				if (e.count() != o.count()) {
					return List.of(e, o);
				}
			}
		}
		return null;
	}
	// Map connectingElement -> SetOf(end1extent, end2extent)
	public Map<Type, Set<Extent>> findAllPairings() {
		Map<Type, Set<Extent>> pairings = new HashMap<>();
		collectAllExtents().forEach(currentExtent -> {
			currentExtent.getPairings().entrySet().forEach(currentPairingEntry -> {
				currentPairingEntry.getValue().forEach(connectingElement -> {
					if (!pairings.containsKey(connectingElement)) {
						pairings.put(connectingElement, Set.of(currentExtent, currentPairingEntry.getKey()));
					}
				});
			});
		});
		return pairings;
	}
}
