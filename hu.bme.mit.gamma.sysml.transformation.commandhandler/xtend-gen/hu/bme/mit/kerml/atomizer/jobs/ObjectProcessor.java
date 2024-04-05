package hu.bme.mit.kerml.atomizer.jobs;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.Classifier;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.PartDefinition;
import org.omg.sysml.lang.sysml.PartUsage;

@SuppressWarnings("all")
public class ObjectProcessor {
  protected static void _executeTarget(final Namespace n, final Collection<EObject> roots) {
    String _declaredName = n.getDeclaredName();
    String _plus = (":P namespace - " + _declaredName);
    System.out.println(_plus);
    EList<Element> _member = n.getMember();
    for (final Element m : _member) {
      ObjectProcessor.executeTarget(m, roots);
    }
    String _declaredName_1 = n.getDeclaredName();
    String _plus_1 = (":/ namespace - " + _declaredName_1);
    System.out.println(_plus_1);
  }

  protected static void _executeTarget(final org.omg.sysml.lang.sysml.Package p, final Collection<EObject> roots) {
    String _declaredName = p.getDeclaredName();
    String _plus = (":P package - " + _declaredName);
    System.out.println(_plus);
    EList<Element> _member = p.getMember();
    for (final Element m : _member) {
      ObjectProcessor.executeTarget(m, roots);
    }
    String _declaredName_1 = p.getDeclaredName();
    String _plus_1 = (":/ package - " + _declaredName_1);
    System.out.println(_plus_1);
  }

  protected static void _executeTarget(final PartDefinition p, final Collection<EObject> roots) {
    String _declaredName = p.getDeclaredName();
    String _plus = (":P partdef - " + _declaredName);
    System.out.println(_plus);
    EList<Element> _member = p.getMember();
    for (final Element m : _member) {
      ObjectProcessor.executeTarget(m, roots);
    }
    String _declaredName_1 = p.getDeclaredName();
    String _plus_1 = (":/ partdef - " + _declaredName_1);
    System.out.println(_plus_1);
  }

  protected static void _executeTarget(final PartUsage p, final Collection<EObject> roots) {
    String _declaredName = p.getDeclaredName();
    String _plus = (":P partusage - " + _declaredName);
    System.out.println(_plus);
    EList<Element> _member = p.getMember();
    for (final Element m : _member) {
      ObjectProcessor.executeTarget(m, roots);
    }
    String _declaredName_1 = p.getDeclaredName();
    String _plus_1 = (":/ partusage - " + _declaredName_1);
    System.out.println(_plus_1);
  }

  protected static void _executeTarget(final Classifier classifier, final Collection<EObject> roots) {
    System.out.println(":C");
  }

  protected static void _executeTarget(final EObject object, final Collection<EObject> context) {
    System.out.println(("Can\'t handle EObject: " + object));
  }

  public static void executeTarget(final EObject p, final Collection<EObject> roots) {
    if (p instanceof PartDefinition) {
      _executeTarget((PartDefinition)p, roots);
      return;
    } else if (p instanceof PartUsage) {
      _executeTarget((PartUsage)p, roots);
      return;
    } else if (p instanceof Classifier) {
      _executeTarget((Classifier)p, roots);
      return;
    } else if (p instanceof org.omg.sysml.lang.sysml.Package) {
      _executeTarget((org.omg.sysml.lang.sysml.Package)p, roots);
      return;
    } else if (p instanceof Namespace) {
      _executeTarget((Namespace)p, roots);
      return;
    } else if (p != null) {
      _executeTarget(p, roots);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(p, roots).toString());
    }
  }
}
