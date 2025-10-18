package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import java.util.Arrays;
import org.omg.sysml.lang.sysml.ItemDefinition;
import org.omg.sysml.lang.sysml.ItemUsage;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.OccurrenceUsage;
import org.omg.sysml.lang.sysml.PartDefinition;
import org.omg.sysml.lang.sysml.PartUsage;
import org.omg.sysml.lang.sysml.PortDefinition;
import org.omg.sysml.lang.sysml.PortUsage;
import org.omg.sysml.lang.sysml.Type;

@SuppressWarnings("all")
public class GeneratorUtils {
  protected static String _getSysMLType(final OccurrenceDefinition o) {
    return "occurrence";
  }

  protected static String _getSysMLType(final ItemDefinition o) {
    return "item";
  }

  protected static String _getSysMLType(final PartDefinition o) {
    return "part";
  }

  protected static String _getSysMLType(final PortDefinition o) {
    return "port";
  }

  protected static String _getSysMLType(final OccurrenceUsage o) {
    return "occurrence";
  }

  protected static String _getSysMLType(final ItemUsage o) {
    return "item";
  }

  protected static String _getSysMLType(final PartUsage o) {
    return "part";
  }

  protected static String _getSysMLType(final PortUsage o) {
    return "port";
  }

  public static String getSysMLType(final Type o) {
    if (o instanceof PartDefinition) {
      return _getSysMLType((PartDefinition)o);
    } else if (o instanceof PartUsage) {
      return _getSysMLType((PartUsage)o);
    } else if (o instanceof ItemDefinition) {
      return _getSysMLType((ItemDefinition)o);
    } else if (o instanceof ItemUsage) {
      return _getSysMLType((ItemUsage)o);
    } else if (o instanceof PortDefinition) {
      return _getSysMLType((PortDefinition)o);
    } else if (o instanceof PortUsage) {
      return _getSysMLType((PortUsage)o);
    } else if (o instanceof OccurrenceDefinition) {
      return _getSysMLType((OccurrenceDefinition)o);
    } else if (o instanceof OccurrenceUsage) {
      return _getSysMLType((OccurrenceUsage)o);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(o).toString());
    }
  }
}
