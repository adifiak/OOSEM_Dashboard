package hu.bme.mit.kerml.atomizer.jobs

import hu.bme.mit.kerml.atomizer.model.Atom
import hu.bme.mit.kerml.atomizer.model.ExtentManager
import org.omg.sysml.lang.sysml.Feature
import org.omg.sysml.lang.sysml.LiteralInfinity
import org.omg.sysml.lang.sysml.LiteralInteger
import org.omg.sysml.lang.sysml.Type
import org.omg.sysml.util.FeatureUtil
import org.eclipse.emf.ecore.EObject
import org.omg.sysml.lang.sysml.Association
import org.omg.sysml.lang.sysml.Connector
import java.util.Collection
import org.eclipse.emf.common.util.EList
import java.util.Map
import java.util.List
import java.util.HashMap
import hu.bme.mit.kerml.atomizer.model.Extent
import java.util.Set
import java.lang.module.Configuration

class Atomizer {
		extension static ExtentManager em = ExtentManager.instance
		
		def static Atom execute(Type target, Atom context) {
			execute(target, context, Map.of())
		}
        def static Atom execute(Type target, Atom context, Map<Feature, Set<Atom>> predefinedFeatureExtents) {
                System.out.println("execute called with type: " + target)
                val extent = getExtent(target, context)
                val atom = new Atom(
                        (target instanceof Feature) ? (target as Feature).type.get(0).declaredName : target.declaredName, em
                )
                extent.add(atom)
                System.out.println("Looking for features of " + atom.toString)
                for (Feature feature : target.feature) {
                        System.out.println("Found feature " + feature.toString() + " of " + atom.toString)
                }
                if (!predefinedFeatureExtents.isEmpty) {
                	System.out.println("Setting predefined features")
                	for (Map.Entry<Feature, Set<Atom>> featureExtent : predefinedFeatureExtents.entrySet) {
                		if (!target.feature.contains(featureExtent.key)) {
                			throw new IllegalArgumentException("The predefined feature is not a feature of this type")
                		}
                		atom.addFeature(featureExtent.key)
                		getExtent(featureExtent.key, atom).addAll(featureExtent.value)
                	}
                }
                System.out.println("Executing remaining features")
                for (Feature feature : target.feature) {
                        if (feature.declaredName != "self" && feature.declaredName != "that" && !(feature instanceof Connector)) {
                                System.out.println(
                                        "Executing feature" + feature.effectiveName + " with multiplicity: \n\tlower: " + feature.lower +
                                                "\n\tupper: " + feature.upper)
                                atom.addFeature(feature)
                                val from = getExtent(feature, atom).count
                                for (i : from ..< feature.lower) {
                                        var done = false
                                        for (a : findSuitableAtoms(feature, atom)) {
                                                if (!done && getExtent(feature, atom).canAdd(a)) {
                                                        getExtent(feature, atom).add(a)
                                                        done = true
                                                }
                                        }
                                        if (!done) {
                                                System.out.println("Instance #" + i)
                                                execute(feature, atom)
                                        }
                                }
                        }
                }

                return atom
        }

        def static toKermlModel() '''
                --------------------------------------------------------------
                «FOR i : Atom.allAtoms»
                        #atom
                        «IF i.mostSpecificNonFeatureType instanceof Association»association«ELSE»classifier«ENDIF» «i.name» specializes «i.mostSpecificNonFeatureType.effectiveName»«
                        IF !i.hasExplicitFeatures»;
                        «ELSE» {
                                «FOR f : i.explicitFeatures»
                                        «var fa = i.effectiveFeatures.get(f)»
                                        «IF fa.type instanceof Connector»connector«ELSE»feature«ENDIF» redefines «f.effectiveName» [«fa.atoms.size»];
                                        «FOR a : fa.atoms»
                                        	«IF i.mostSpecificNonFeatureType instanceof Association»end feature «ELSE»feature «ENDIF»«a.name.toLowerCase» : «a.name» [1] :> «f.effectiveName»;
                                        «ENDFOR»
                                «ENDFOR»
                        }
                        «ENDIF»
                «ENDFOR»
        '''

        def private static upper(Feature feature) {
                var ub = FeatureUtil.getMultiplicityRangeOf(FeatureUtil.getMultiplicityOf(feature)).upperBound
                if (ub instanceof LiteralInteger) {
                        return (ub as LiteralInteger).value
                } else if (ub instanceof LiteralInfinity) {
                        return Integer.MAX_VALUE
                } else {
                        throw new RuntimeException("Upper multiplicity was not LiteralInteger or LiteralInfinity.")
                }

        }

        def private static lower(Feature feature) {
                var lb = FeatureUtil.getMultiplicityRangeOf(FeatureUtil.getMultiplicityOf(feature)).lowerBound
                if (lb === null) {
                        lb = FeatureUtil.getMultiplicityRangeOf(FeatureUtil.getMultiplicityOf(feature)).upperBound
                }
                if (lb instanceof LiteralInteger) {
                        return (lb as LiteralInteger).value
                } else if (lb instanceof LiteralInfinity) {
                        return 0
                } else {
                        //throw new RuntimeException("Lower multiplicity was not LiteralInteger or LiteralInfinity, it was " + lb)
                        return -1
                }
        }

        def static collectConnectors(Collection<EObject> root) {
                var connectors = root.flatMap[it.eAllContents.filter(Connector).toList].toList

                val connMap = new HashMap<Feature, Set<Connector>>
                connectors.forEach[conn |
                        if(conn.connectorEnd.forall[it.lower == 1]) {
                                conn.connectorEnd.forEach[end |
                                        connMap.putSetMapC(FeatureUtil.getReferencedFeatureOf(
                                                conn.connectorEnd.findFirst[it !== end]
                                        ), conn)
                                ]
                        }
                ]
                return connMap
        }

        def static collectAssociations(Collection<EObject> root) {
                val associations = root.flatMap[it.eAllContents.filter(Association).toList].toList

                val assocMap = new HashMap<Type, Set<Association>>
                associations.forEach[assoc |
                        if(assoc.associationEnd.forall[it.lower == 1]) {
                                assoc.associationEnd.forEach[end |
                                        assocMap.putSetMapA(FeatureUtil.getReferencedFeatureOf(end), assoc)
                                ]
                        }
                ]
                return assocMap
        }

        def static satisfyPairings() {
                System.out.println("satisfyPairings")
                var List<Extent> unpaired
                while ((unpaired = findUnpairedExtent) !== null) {
                        System.out.println("found unsatisfied pairing")
                        val e = unpaired.minBy[it.count]
                        val to = unpaired.findFirst[it != e].count
                        System.out.println("executing " + e + " up to " + to)
                        while (e.count < to) {
                                execute(e.type, e.context)
                        }
                }
        }

        def static private putSetMapA(HashMap<Type, Set<Association>> m, Type k, Association v) {
                if (m.get(k) === null) {
                        val h = newHashSet
                        h.add(v)
                        m.put(k, h)
                }
                else {
                        m.get(k).add(v)
                }
        }

        def static private putSetMapC(HashMap<Feature, Set<Connector>> m, Feature k, Connector v) {
                if (m.get(k) === null) {
                        val h = newHashSet
                        h.add(v)
                        m.put(k, h)
                }
                else {
                        m.get(k).add(v)
                }
        }

        def static concretizePairings() {
                val allPairings = findAllPairings
                //this assumes binary, one to one connectors
                for (Map.Entry<Type, Set<Extent>> pairing : allPairings.entrySet) {
                	if(pairing.key instanceof Connector) {
                		val connector = pairing.key as Connector
                		System.out.println("stuff to pair:")
                		for (i : 0 ..< pairing.value.get(0).count) {
                			System.out.println("\t" + pairing.value.get(0).atoms.get(i) + " to " + pairing.value.get(1).atoms.get(i))
                		}
                		val predefinedFeatureExtents = new HashMap
                		for (i : 0 ..< pairing.value.get(0).count) {
                			val connectedAtoms = Set.of(pairing.value.get(0).atoms.get(i), pairing.value.get(1).atoms.get(i));
//	                		val assocAtom = executeConnector(pairing.key as Connector, connectedEnds, pairing.value.get(0).context)
							val participantFeature = connector.feature.findFirst["participant".equals(it.declaredName)]
							if (participantFeature === null) {
								throw new NullPointerException("No participant feature of connector was found.")
							}
							//predefinedFeatureExtents.put(participantFeature, connectedAtoms)
							connector.endFeature.forEach[endFeature | 
								val endType = endFeature.type.get(0)
								val endAtom = connectedAtoms.findFirst[connectedAtom |
									connectedAtom.mostSpecificNonFeatureType == endType
								]
								predefinedFeatureExtents.put(endFeature, Set.of(endAtom))
							]
							pairing.value.get(0).context.explicitFeatures.add(connector)
							val a = execute(connector, pairing.value.get(0).context, predefinedFeatureExtents)
							a.explicitFeatures.remove(
								a.explicitFeatures.findFirst["participant".equals(it.effectiveName)]
							)
                		}
                	}
                }
        }

}
