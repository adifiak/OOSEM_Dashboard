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
public class OOSEMVisualizer {
  private static int depth = 0;

  protected static void _processNode(final Namespace n) {
    if ((OOSEMVisualizer.depth == 0)) {
      System.out.println("@startmindmap");
      System.out.println("<style>\nroot {\n\tFontColor #?black:white\n}\n</style>");
      System.out.println("top to bottom direction\n");
      OOSEMVisualizer.namespaceProcessor(n);
      System.out.println("\nlegend");
      System.out.println("\t<&folder>Package");
      System.out.println("\t<&cog>Part");
      System.out.println("\t<&puzzle-piece>Port");
      System.out.println("endlegend");
      System.out.println("@endmindmap");
    } else {
      OOSEMVisualizer.drawNode(n);
      OOSEMVisualizer.namespaceProcessor(n);
    }
  }

  protected static void _processNode(final EObject object) {
    System.out.println(("\'Non-namespace object: " + object));
  }

  public static void namespaceProcessor(final Namespace n) {
    OOSEMVisualizer.depth++;
    EList<Element> _ownedMember = n.getOwnedMember();
    for (final Element m : _ownedMember) {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(m.getDeclaredName());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        boolean _equals = m.getDeclaredName().equals("OOSEM");
        boolean _not_1 = (!_equals);
        if (_not_1) {
          OOSEMVisualizer.processNode(m);
        }
      }
    }
    OOSEMVisualizer.depth--;
  }

  public static void nodeDrawer(final String color, final String icon, final String name, final String type) {
    ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, OOSEMVisualizer.depth, true);
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
    OOSEMVisualizer.nodeDrawer("", "", n.getDeclaredName(), "");
  }

  protected static void _drawNode(final org.omg.sysml.lang.sysml.Package p) {
    OOSEMVisualizer.nodeDrawer("White", "folder", p.getDeclaredName(), "");
  }

  protected static void _drawNode(final PartDefinition p) {
    String Color = OOSEMVisualizer.getBlockColor(p, Boolean.valueOf(true));
    OOSEMVisualizer.nodeDrawer(Color, "cog", p.getDeclaredName(), "");
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
    String Color = OOSEMVisualizer.getBlockColor(p, Boolean.valueOf(false));
    OOSEMVisualizer.nodeDrawer(Color, "cog", p.getDeclaredName(), typeName);
  }

  protected static void _drawNode(final PortDefinition p) {
    OOSEMVisualizer.nodeDrawer("White", "puzzle-piece", p.getDeclaredName(), "");
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
    OOSEMVisualizer.nodeDrawer("White", "puzzle-piece", p.getDeclaredName(), typeName);
  }

  public static String getBlockColor(final Type t, final Boolean dark) {
    EList<Type> types = t.allSupertypes();
    boolean spec = false;
    boolean desi = false;
    boolean inte = false;
    for (final Type type : types) {
      boolean _equals = type.getDeclaredName().equals("SpecificationBlock");
      if (_equals) {
        spec = true;
      } else {
        boolean _equals_1 = type.getDeclaredName().equals("DesignBlock");
        if (_equals_1) {
          desi = true;
        } else {
          boolean _equals_2 = type.getDeclaredName().equals("IntegrationBlock");
          if (_equals_2) {
            inte = true;
          }
        }
      }
    }
    if (inte) {
      String _xifexpression = null;
      if ((dark).booleanValue()) {
        _xifexpression = "Navy";
      } else {
        _xifexpression = "RoyalBlue";
      }
      return _xifexpression;
    } else {
      if (desi) {
        String _xifexpression_1 = null;
        if ((dark).booleanValue()) {
          _xifexpression_1 = "OliveDrab";
        } else {
          _xifexpression_1 = "YellowGreen";
        }
        return _xifexpression_1;
      } else {
        if (spec) {
          String _xifexpression_2 = null;
          if ((dark).booleanValue()) {
            _xifexpression_2 = "Crimson";
          } else {
            _xifexpression_2 = "LightCoral";
          }
          return _xifexpression_2;
        }
      }
    }
    return "White";
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
