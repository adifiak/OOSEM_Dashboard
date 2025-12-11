package hu.bme.mit.kerml.atomizer.jobs;

import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
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
  private static /* ExtentManager */Object em /* Skipped initializer because of errors */;

  public static /* Atom */Object execute(final Type target, final /* Atom */Object context) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method execute(Type, Atom, Map<Feature, Set<Atom>>) from the type Atomizer refers to the missing type Atom");
  }

  public static /* Atom */Object execute(final Type target, final /* Atom */Object context, final /* Map<Feature, Set<Atom>> */Object predefinedFeatureExtents) {
    throw new Error("Unresolved compilation problems:"
      + "\nAtom cannot be resolved to a type."
      + "\nThe method getExtent(Type, Atom) is undefined"
      + "\nAtom cannot be resolved."
      + "\nThe method getExtent(Feature, Object) is undefined"
      + "\nThe method getExtent(Feature, Object) is undefined"
      + "\nThe method findSuitableAtoms(Feature, Object) is undefined"
      + "\nThe method getExtent(Feature, Object) is undefined"
      + "\nThe method getExtent(Feature, Object) is undefined"
      + "\nThe field Atomizer.em refers to the missing type ExtentManager"
      + "\nThe method execute(Type, Atom) from the type Atomizer refers to the missing type Atom"
      + "\nadd cannot be resolved"
      + "\ntoString cannot be resolved"
      + "\ntoString cannot be resolved"
      + "\naddFeature cannot be resolved"
      + "\naddAll cannot be resolved"
      + "\naddFeature cannot be resolved"
      + "\ncount cannot be resolved"
      + "\n..< cannot be resolved"
      + "\ncanAdd cannot be resolved"
      + "\nadd cannot be resolved");
  }

  public static CharSequence toKermlModel() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Atom is undefined"
      + "\nallAtoms cannot be resolved"
      + "\nmostSpecificNonFeatureType cannot be resolved"
      + "\nname cannot be resolved"
      + "\nmostSpecificNonFeatureType cannot be resolved"
      + "\neffectiveName cannot be resolved"
      + "\nhasExplicitFeatures cannot be resolved"
      + "\n! cannot be resolved"
      + "\nexplicitFeatures cannot be resolved"
      + "\neffectiveFeatures cannot be resolved"
      + "\nget cannot be resolved"
      + "\ntype cannot be resolved"
      + "\neffectiveName cannot be resolved"
      + "\natoms cannot be resolved"
      + "\nsize cannot be resolved"
      + "\natoms cannot be resolved"
      + "\nmostSpecificNonFeatureType cannot be resolved"
      + "\nname cannot be resolved"
      + "\ntoLowerCase cannot be resolved"
      + "\nname cannot be resolved"
      + "\neffectiveName cannot be resolved");
  }

  private static int upper(final Feature feature) {
    Expression ub = FeatureUtil.getMultiplicityRangeOf(feature.getMultiplicity()).getUpperBound();
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
    Expression lb = FeatureUtil.getMultiplicityRangeOf(feature.getMultiplicity()).getLowerBound();
    if ((lb == null)) {
      lb = FeatureUtil.getMultiplicityRangeOf(feature.getMultiplicity()).getUpperBound();
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
    throw new Error("Unresolved compilation problems:"
      + "\nExtent cannot be resolved to a type."
      + "\nThe method or field findUnpairedExtent is undefined"
      + "\nThe method execute(Type, Atom) from the type Atomizer refers to the missing type Atom"
      + "\ncount cannot be resolved"
      + "\n!= cannot be resolved"
      + "\ncount cannot be resolved"
      + "\ncount cannot be resolved"
      + "\n< cannot be resolved"
      + "\ntype cannot be resolved"
      + "\ncontext cannot be resolved");
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
    throw new Error("Unresolved compilation problems:"
      + "\nExtent cannot be resolved to a type."
      + "\nThe method or field findAllPairings is undefined"
      + "\nThe method or field effectiveName is undefined for the type Object"
      + "\nThe method execute(Type, Atom, Map<Feature, Set<Atom>>) from the type Atomizer refers to the missing type Atom"
      + "\nentrySet cannot be resolved"
      + "\ncount cannot be resolved"
      + "\natoms cannot be resolved"
      + "\nget cannot be resolved"
      + "\natoms cannot be resolved"
      + "\nget cannot be resolved"
      + "\ncount cannot be resolved"
      + "\natoms cannot be resolved"
      + "\nget cannot be resolved"
      + "\natoms cannot be resolved"
      + "\nget cannot be resolved"
      + "\nmostSpecificNonFeatureType cannot be resolved"
      + "\n== cannot be resolved"
      + "\ncontext cannot be resolved"
      + "\nexplicitFeatures cannot be resolved"
      + "\nadd cannot be resolved"
      + "\ncontext cannot be resolved"
      + "\nexplicitFeatures cannot be resolved"
      + "\nremove cannot be resolved"
      + "\nexplicitFeatures cannot be resolved"
      + "\nfindFirst cannot be resolved");
  }
}
