package hu.bme.mit.kerml.atomizer.jobs;

import java.util.Arrays;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.omg.sysml.lang.sysml.ConjugatedPortDefinition;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.PartDefinition;
import org.omg.sysml.lang.sysml.PartUsage;
import org.omg.sysml.lang.sysml.PortDefinition;
import org.omg.sysml.lang.sysml.PortUsage;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

@SuppressWarnings("all")
public class HierarchyVisualizer {
  private static int depth = 0;

  protected static void _processNode(final Namespace n) {
    if ((HierarchyVisualizer.depth == 0)) {
      System.out.println("@startmindmap");
      System.out.println("<style>\nroot {\n\tFontColor #?black:white\n}\n</style>");
      System.out.println("top to bottom direction\n");
      HierarchyVisualizer.namespaceProcessor(n);
      System.out.println("\nlegend");
      System.out.println("\t<&folder>Package");
      System.out.println("\t<&cog>Part");
      System.out.println("\t<&puzzle-piece>Port");
      System.out.println("endlegend");
      System.out.println("@endmindmap");
    } else {
      HierarchyVisualizer.drawNode(n);
      HierarchyVisualizer.namespaceProcessor(n);
    }
  }

  protected static void _processNode(final EObject object) {
    System.out.println(("\'Non-namespace object: " + object));
  }

  public static void namespaceProcessor(final Namespace n) {
    HierarchyVisualizer.depth++;
    EList<Element> _ownedMember = n.getOwnedMember();
    for (final Element m : _ownedMember) {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(m.getDeclaredName());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        HierarchyVisualizer.processNode(m);
      }
    }
    HierarchyVisualizer.depth--;
  }

  public static void nodeDrawer(final String color, final String icon, final String name, final String type) {
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, HierarchyVisualizer.depth, true);
    for (final Integer i : _doubleDotLessThan) {
      System.out.print("*");
    }
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(color);
    boolean _not = (!_isNullOrEmpty);
    if (_not) {
      System.out.print((("[#" + color) + "] "));
    }
    boolean _isNullOrEmpty_1 = StringExtensions.isNullOrEmpty(icon);
    boolean _not_1 = (!_isNullOrEmpty_1);
    if (_not_1) {
      System.out.print(((" <&" + icon) + "> "));
    }
    System.out.print(name);
    boolean _isNullOrEmpty_2 = StringExtensions.isNullOrEmpty(type);
    boolean _not_2 = (!_isNullOrEmpty_2);
    if (_not_2) {
      System.out.print((" : " + type));
    }
    System.out.print("\n");
  }

  protected static void _drawNode(final Namespace n) {
    HierarchyVisualizer.nodeDrawer("", "", n.getDeclaredName(), "");
  }

  protected static void _drawNode(final org.omg.sysml.lang.sysml.Package p) {
    HierarchyVisualizer.nodeDrawer("DarkViolet", "folder", p.getDeclaredName(), "");
  }

  protected static void _drawNode(final PartDefinition p) {
    HierarchyVisualizer.nodeDrawer("RoyalBlue", "cog", p.getDeclaredName(), "");
  }

  protected static void _drawNode(final PartUsage p) {
    String typeName = "";
    EList<Type> types = FeatureUtil.getAllTypesOf(((Feature) p));
    final EList<Type> _converted_types = (EList<Type>)types;
    int _length = ((Object[])Conversions.unwrapArray(_converted_types, Object.class)).length;
    boolean _greaterThan = (_length > 0);
    if (_greaterThan) {
      Type type = types.get(0);
      typeName = type.getDeclaredName();
      if (((!StringExtensions.isNullOrEmpty(typeName)) && typeName.equals("Part"))) {
        typeName = "";
      }
    }
    HierarchyVisualizer.nodeDrawer("Blue", "cog", p.getDeclaredName(), typeName);
  }

  protected static void _drawNode(final PortDefinition p) {
    HierarchyVisualizer.nodeDrawer("OliveDrab", "puzzle-piece", p.getDeclaredName(), "");
  }

  protected static void _drawNode(final PortUsage p) {
    String typeName = "";
    EList<Type> types = FeatureUtil.getAllTypesOf(((Feature) p));
    final EList<Type> _converted_types = (EList<Type>)types;
    int _length = ((Object[])Conversions.unwrapArray(_converted_types, Object.class)).length;
    boolean _greaterThan = (_length > 0);
    if (_greaterThan) {
      Type type = types.get(0);
      if ((type instanceof ConjugatedPortDefinition)) {
        String _declaredName = ((ConjugatedPortDefinition)type).getOriginalPortDefinition().getDeclaredName();
        String _plus = ("~" + _declaredName);
        typeName = _plus;
      } else {
        typeName = type.getDeclaredName();
      }
    }
    HierarchyVisualizer.nodeDrawer("Green", "puzzle-piece", p.getDeclaredName(), typeName);
  }

  public static void processNode(final EObject n) {
    if (n instanceof Namespace) {
      _processNode((Namespace)n);
      return;
    } else if (n != null) {
      _processNode(n);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(n).toString());
    }
  }

  public static void drawNode(final Namespace p) {
    if (p instanceof PartDefinition) {
      _drawNode((PartDefinition)p);
      return;
    } else if (p instanceof PartUsage) {
      _drawNode((PartUsage)p);
      return;
    } else if (p instanceof PortDefinition) {
      _drawNode((PortDefinition)p);
      return;
    } else if (p instanceof PortUsage) {
      _drawNode((PortUsage)p);
      return;
    } else if (p instanceof org.omg.sysml.lang.sysml.Package) {
      _drawNode((org.omg.sysml.lang.sysml.Package)p);
      return;
    } else if (p != null) {
      _drawNode(p);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(p).toString());
    }
  }
}
