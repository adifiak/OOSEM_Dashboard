package hu.bme.mit.kerml.atomizer.jobs;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import hu.bme.mit.kerml.atomizer.model.Atom;
import hu.bme.mit.kerml.atomizer.model.Extent;
import hu.bme.mit.kerml.atomizer.model.ExtentManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.omg.sysml.lang.sysml.Association;
import org.omg.sysml.lang.sysml.Connector;
import org.omg.sysml.lang.sysml.Expression;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.LiteralInfinity;
import org.omg.sysml.lang.sysml.LiteralInteger;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

@SuppressWarnings("all")
public class Atomizer {
  @Extension
  private static ExtentManager em = ExtentManager.getInstance();

  public static Atom execute(final Type target, final Atom context) {
    return Atomizer.execute(target, context, Map.<Feature, Set<Atom>>of());
  }

  public static Atom execute(final Type target, final Atom context, final Map<Feature, Set<Atom>> predefinedFeatureExtents) {
    System.out.println(("execute called with type: " + target));
    final Extent extent = Atomizer.em.getExtent(target, context);
    String _xifexpression = null;
    if ((target instanceof Feature)) {
      _xifexpression = ((Feature) target).getType().get(0).getDeclaredName();
    } else {
      _xifexpression = target.getDeclaredName();
    }
    final Atom atom = new Atom(_xifexpression, Atomizer.em);
    extent.add(atom);
    String _string = atom.toString();
    String _plus = ("Looking for features of " + _string);
    System.out.println(_plus);
    EList<Feature> _feature = target.getFeature();
    for (final Feature feature : _feature) {
      String _string_1 = feature.toString();
      String _plus_1 = ("Found feature " + _string_1);
      String _plus_2 = (_plus_1 + " of ");
      String _string_2 = atom.toString();
      String _plus_3 = (_plus_2 + _string_2);
      System.out.println(_plus_3);
    }
    boolean _isEmpty = predefinedFeatureExtents.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      System.out.println("Setting predefined features");
      Set<Map.Entry<Feature, Set<Atom>>> _entrySet = predefinedFeatureExtents.entrySet();
      for (final Map.Entry<Feature, Set<Atom>> featureExtent : _entrySet) {
        {
          boolean _contains = target.getFeature().contains(featureExtent.getKey());
          boolean _not_1 = (!_contains);
          if (_not_1) {
            throw new IllegalArgumentException("The predefined feature is not a feature of this type");
          }
          atom.addFeature(featureExtent.getKey());
          Atomizer.em.getExtent(featureExtent.getKey(), atom).addAll(featureExtent.getValue());
        }
      }
    }
    System.out.println("Executing remaining features");
    EList<Feature> _feature_1 = target.getFeature();
    for (final Feature feature_1 : _feature_1) {
      if ((((!Objects.equal(feature_1.getDeclaredName(), "self")) && (!Objects.equal(feature_1.getDeclaredName(), "that"))) && (!(feature_1 instanceof Connector)))) {
        String _effectiveName = feature_1.effectiveName();
        String _plus_4 = ("Executing feature" + _effectiveName);
        String _plus_5 = (_plus_4 + " with multiplicity: \n\tlower: ");
        int _lower = Atomizer.lower(feature_1);
        String _plus_6 = (_plus_5 + Integer.valueOf(_lower));
        String _plus_7 = (_plus_6 + 
          "\n\tupper: ");
        int _upper = Atomizer.upper(feature_1);
        String _plus_8 = (_plus_7 + Integer.valueOf(_upper));
        System.out.println(_plus_8);
        atom.addFeature(feature_1);
        final int from = Atomizer.em.getExtent(feature_1, atom).count();
        int _lower_1 = Atomizer.lower(feature_1);
        ExclusiveRange _doubleDotLessThan = new ExclusiveRange(from, _lower_1, true);
        for (final Integer i : _doubleDotLessThan) {
          {
            boolean done = false;
            Collection<Atom> _findSuitableAtoms = Atomizer.em.findSuitableAtoms(feature_1, atom);
            for (final Atom a : _findSuitableAtoms) {
              if (((!done) && Atomizer.em.getExtent(feature_1, atom).canAdd(a))) {
                Atomizer.em.getExtent(feature_1, atom).add(a);
                done = true;
              }
            }
            if ((!done)) {
              System.out.println(("Instance #" + i));
              Atomizer.execute(feature_1, atom);
            }
          }
        }
      }
    }
    return atom;
  }

  public static CharSequence toKermlModel() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("--------------------------------------------------------------");
    _builder.newLine();
    {
      List<Atom> _allAtoms = Atom.getAllAtoms();
      for(final Atom i : _allAtoms) {
        _builder.append("#atom");
        _builder.newLine();
        {
          Type _mostSpecificNonFeatureType = i.getMostSpecificNonFeatureType();
          if ((_mostSpecificNonFeatureType instanceof Association)) {
            _builder.append("association");
          } else {
            _builder.append("classifier");
          }
        }
        _builder.append(" ");
        String _name = i.getName();
        _builder.append(_name);
        _builder.append(" specializes ");
        String _effectiveName = i.getMostSpecificNonFeatureType().effectiveName();
        _builder.append(_effectiveName);
        {
          boolean _hasExplicitFeatures = i.hasExplicitFeatures();
          boolean _not = (!_hasExplicitFeatures);
          if (_not) {
            _builder.append(";");
            _builder.newLineIfNotEmpty();
          } else {
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            {
              Set<Feature> _explicitFeatures = i.getExplicitFeatures();
              for(final Feature f : _explicitFeatures) {
                _builder.append("        ");
                Extent fa = i.getEffectiveFeatures().get(f);
                _builder.newLineIfNotEmpty();
                _builder.append("        ");
                {
                  Type _type = fa.getType();
                  if ((_type instanceof Connector)) {
                    _builder.append("connector");
                  } else {
                    _builder.append("feature");
                  }
                }
                _builder.append(" redefines ");
                String _effectiveName_1 = f.effectiveName();
                _builder.append(_effectiveName_1, "        ");
                _builder.append(" [");
                int _size = fa.getAtoms().size();
                _builder.append(_size, "        ");
                _builder.append("];");
                _builder.newLineIfNotEmpty();
                {
                  Set<Atom> _atoms = fa.getAtoms();
                  for(final Atom a : _atoms) {
                    _builder.append("        ");
                    {
                      Type _mostSpecificNonFeatureType_1 = i.getMostSpecificNonFeatureType();
                      if ((_mostSpecificNonFeatureType_1 instanceof Association)) {
                        _builder.append("end feature ");
                      } else {
                        _builder.append("feature ");
                      }
                    }
                    String _lowerCase = a.getName().toLowerCase();
                    _builder.append(_lowerCase, "        ");
                    _builder.append(" : ");
                    String _name_1 = a.getName();
                    _builder.append(_name_1, "        ");
                    _builder.append(" [1] :> ");
                    String _effectiveName_2 = f.effectiveName();
                    _builder.append(_effectiveName_2, "        ");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                  }
                }
              }
            }
            _builder.append("}");
            _builder.newLine();
          }
        }
      }
    }
    return _builder;
  }

  private static int upper(final Feature feature) {
    Expression ub = FeatureUtil.getMultiplicityRangeOf(FeatureUtil.getMultiplicityOf(feature)).getUpperBound();
    if ((ub instanceof LiteralInteger)) {
      return ((LiteralInteger) ub).getValue();
    } else {
      if ((ub instanceof LiteralInfinity)) {
        return Integer.MAX_VALUE;
      } else {
        throw new RuntimeException("Upper multiplicity was not LiteralInteger or LiteralInfinity.");
      }
    }
  }

  private static int lower(final Feature feature) {
    Expression lb = FeatureUtil.getMultiplicityRangeOf(FeatureUtil.getMultiplicityOf(feature)).getLowerBound();
    if ((lb == null)) {
      lb = FeatureUtil.getMultiplicityRangeOf(FeatureUtil.getMultiplicityOf(feature)).getUpperBound();
    }
    if ((lb instanceof LiteralInteger)) {
      return ((LiteralInteger) lb).getValue();
    } else {
      if ((lb instanceof LiteralInfinity)) {
        return 0;
      } else {
        return (-1);
      }
    }
  }

  public static HashMap<Feature, Set<Connector>> collectConnectors(final Collection<EObject> root) {
    final Function1<EObject, List<Connector>> _function = (EObject it) -> {
      return IteratorExtensions.<Connector>toList(Iterators.<Connector>filter(it.eAllContents(), Connector.class));
    };
    List<Connector> connectors = IterableExtensions.<Connector>toList(IterableExtensions.<EObject, Connector>flatMap(root, _function));
    final HashMap<Feature, Set<Connector>> connMap = new HashMap<Feature, Set<Connector>>();
    final Consumer<Connector> _function_1 = (Connector conn) -> {
      final Function1<Feature, Boolean> _function_2 = (Feature it) -> {
        int _lower = Atomizer.lower(it);
        return Boolean.valueOf((_lower == 1));
      };
      boolean _forall = IterableExtensions.<Feature>forall(conn.getConnectorEnd(), _function_2);
      if (_forall) {
        final Consumer<Feature> _function_3 = (Feature end) -> {
          final Function1<Feature, Boolean> _function_4 = (Feature it) -> {
            return Boolean.valueOf((it != end));
          };
          Atomizer.putSetMapC(connMap, 
            FeatureUtil.getReferencedFeatureOf(
              IterableExtensions.<Feature>findFirst(conn.getConnectorEnd(), _function_4)), conn);
        };
        conn.getConnectorEnd().forEach(_function_3);
      }
    };
    connectors.forEach(_function_1);
    return connMap;
  }

  public static HashMap<Type, Set<Association>> collectAssociations(final Collection<EObject> root) {
    final Function1<EObject, List<Association>> _function = (EObject it) -> {
      return IteratorExtensions.<Association>toList(Iterators.<Association>filter(it.eAllContents(), Association.class));
    };
    final List<Association> associations = IterableExtensions.<Association>toList(IterableExtensions.<EObject, Association>flatMap(root, _function));
    final HashMap<Type, Set<Association>> assocMap = new HashMap<Type, Set<Association>>();
    final Consumer<Association> _function_1 = (Association assoc) -> {
      final Function1<Feature, Boolean> _function_2 = (Feature it) -> {
        int _lower = Atomizer.lower(it);
        return Boolean.valueOf((_lower == 1));
      };
      boolean _forall = IterableExtensions.<Feature>forall(assoc.getAssociationEnd(), _function_2);
      if (_forall) {
        final Consumer<Feature> _function_3 = (Feature end) -> {
          Atomizer.putSetMapA(assocMap, FeatureUtil.getReferencedFeatureOf(end), assoc);
        };
        assoc.getAssociationEnd().forEach(_function_3);
      }
    };
    associations.forEach(_function_1);
    return assocMap;
  }

  public static void satisfyPairings() {
    System.out.println("satisfyPairings");
    List<Extent> unpaired = null;
    while (((unpaired = Atomizer.em.findUnpairedExtent()) != null)) {
      {
        System.out.println("found unsatisfied pairing");
        final Function1<Extent, Integer> _function = (Extent it) -> {
          return Integer.valueOf(it.count());
        };
        final Extent e = IterableExtensions.<Extent, Integer>minBy(unpaired, _function);
        final Function1<Extent, Boolean> _function_1 = (Extent it) -> {
          return Boolean.valueOf((!Objects.equal(it, e)));
        };
        final int to = IterableExtensions.<Extent>findFirst(unpaired, _function_1).count();
        System.out.println(((("executing " + e) + " up to ") + Integer.valueOf(to)));
        while ((e.count() < to)) {
          Atomizer.execute(e.getType(), e.getContext());
        }
      }
    }
  }

  private static Object putSetMapA(final HashMap<Type, Set<Association>> m, final Type k, final Association v) {
    Object _xifexpression = null;
    Set<Association> _get = m.get(k);
    boolean _tripleEquals = (_get == null);
    if (_tripleEquals) {
      Set<Association> _xblockexpression = null;
      {
        final HashSet<Association> h = CollectionLiterals.<Association>newHashSet();
        h.add(v);
        _xblockexpression = m.put(k, h);
      }
      _xifexpression = _xblockexpression;
    } else {
      _xifexpression = Boolean.valueOf(m.get(k).add(v));
    }
    return _xifexpression;
  }

  private static Object putSetMapC(final HashMap<Feature, Set<Connector>> m, final Feature k, final Connector v) {
    Object _xifexpression = null;
    Set<Connector> _get = m.get(k);
    boolean _tripleEquals = (_get == null);
    if (_tripleEquals) {
      Set<Connector> _xblockexpression = null;
      {
        final HashSet<Connector> h = CollectionLiterals.<Connector>newHashSet();
        h.add(v);
        _xblockexpression = m.put(k, h);
      }
      _xifexpression = _xblockexpression;
    } else {
      _xifexpression = Boolean.valueOf(m.get(k).add(v));
    }
    return _xifexpression;
  }

  public static void concretizePairings() {
    final Map<Type, Set<Extent>> allPairings = Atomizer.em.findAllPairings();
    Set<Map.Entry<Type, Set<Extent>>> _entrySet = allPairings.entrySet();
    for (final Map.Entry<Type, Set<Extent>> pairing : _entrySet) {
      Type _key = pairing.getKey();
      if ((_key instanceof Connector)) {
        Type _key_1 = pairing.getKey();
        final Connector connector = ((Connector) _key_1);
        System.out.println("stuff to pair:");
        int _count = (((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[0]).count();
        ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _count, true);
        for (final Integer i : _doubleDotLessThan) {
          Object _get = ((Object[])Conversions.unwrapArray((((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[0]).getAtoms(), Object.class))[(i).intValue()];
          String _plus = ("\t" + _get);
          String _plus_1 = (_plus + " to ");
          Object _get_1 = ((Object[])Conversions.unwrapArray((((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[1]).getAtoms(), Object.class))[(i).intValue()];
          String _plus_2 = (_plus_1 + _get_1);
          System.out.println(_plus_2);
        }
        final HashMap<Feature, Set<Atom>> predefinedFeatureExtents = new HashMap<Feature, Set<Atom>>();
        int _count_1 = (((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[0]).count();
        ExclusiveRange _doubleDotLessThan_1 = new ExclusiveRange(0, _count_1, true);
        for (final Integer i_1 : _doubleDotLessThan_1) {
          {
            final Set<Atom> connectedAtoms = Set.<Atom>of(((Atom[])Conversions.unwrapArray((((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[0]).getAtoms(), Atom.class))[(i_1).intValue()], ((Atom[])Conversions.unwrapArray((((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[1]).getAtoms(), Atom.class))[(i_1).intValue()]);
            final Function1<Feature, Boolean> _function = (Feature it) -> {
              return Boolean.valueOf("participant".equals(it.getDeclaredName()));
            };
            final Feature participantFeature = IterableExtensions.<Feature>findFirst(connector.getFeature(), _function);
            if ((participantFeature == null)) {
              throw new NullPointerException("No participant feature of connector was found.");
            }
            final Consumer<Feature> _function_1 = (Feature endFeature) -> {
              final Type endType = endFeature.getType().get(0);
              final Function1<Atom, Boolean> _function_2 = (Atom connectedAtom) -> {
                Type _mostSpecificNonFeatureType = connectedAtom.getMostSpecificNonFeatureType();
                return Boolean.valueOf(Objects.equal(_mostSpecificNonFeatureType, endType));
              };
              final Atom endAtom = IterableExtensions.<Atom>findFirst(connectedAtoms, _function_2);
              predefinedFeatureExtents.put(endFeature, Set.<Atom>of(endAtom));
            };
            connector.getEndFeature().forEach(_function_1);
            (((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[0]).getContext().getExplicitFeatures().add(connector);
            final Atom a = Atomizer.execute(connector, (((Extent[])Conversions.unwrapArray(pairing.getValue(), Extent.class))[0]).getContext(), predefinedFeatureExtents);
            final Function1<Feature, Boolean> _function_2 = (Feature it) -> {
              return Boolean.valueOf("participant".equals(it.effectiveName()));
            };
            a.getExplicitFeatures().remove(
              IterableExtensions.<Feature>findFirst(a.getExplicitFeatures(), _function_2));
          }
        }
      }
    }
  }
}
