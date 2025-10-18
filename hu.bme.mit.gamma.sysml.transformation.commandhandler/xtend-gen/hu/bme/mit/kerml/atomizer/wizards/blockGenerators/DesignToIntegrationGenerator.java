package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;
import java.io.FileWriter;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.omg.sysml.lang.sysml.Type;

@SuppressWarnings("all")
public class DesignToIntegrationGenerator {
  public static void generate(final BasicBlockGenerationData data, final IntegrationData data2) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    _builder.append(data.blockName);
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("private import OOSEM::OOSEM_Metadata::*;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("private import ");
    String _qualifiedName = data.subjectSpecification.getQualifiedName();
    _builder.append(_qualifiedName, "    ");
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("#integration ");
    String _sysMLType = GeneratorUtils.getSysMLType(data.subjectSpecification);
    _builder.append(_sysMLType, "    ");
    _builder.append(" ");
    _builder.append(data.blockName, "    ");
    _builder.append(" :> ");
    String _name = data.subjectSpecification.getName();
    _builder.append(_name, "    ");
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    {
      for(final IntegrationPage.OOSEMIntegrationConfig p : data2.configs) {
        {
          EObject _implementation = p.getImplementation();
          boolean _tripleEquals = (_implementation == null);
          if (_tripleEquals) {
            _builder.append("    ");
            _builder.append("//#<OOSEMMetadata> ");
            EObject _specification = p.getSpecification();
            String _sysMLType_1 = GeneratorUtils.getSysMLType(((Type) _specification));
            _builder.append(_sysMLType_1, "    ");
            _builder.append(" <NewName> :>> ");
            EObject _specification_1 = p.getSpecification();
            String _name_1 = ((Type) _specification_1).getName();
            _builder.append(_name_1, "    ");
            _builder.append(" : <NewType>;");
            _builder.newLineIfNotEmpty();
          } else {
            _builder.append("    ");
            _builder.append("#");
            String _metadata = DesignToIntegrationGenerator.getMetadata(p.getImplementation());
            _builder.append(_metadata, "    ");
            _builder.append(" ");
            EObject _implementation_1 = p.getImplementation();
            String _sysMLType_2 = GeneratorUtils.getSysMLType(((Type) _implementation_1));
            _builder.append(_sysMLType_2, "    ");
            _builder.append(" ");
            {
              String _get = data2.featureNames.get(p.getSpecification());
              boolean _tripleNotEquals = (_get != null);
              if (_tripleNotEquals) {
                String _get_1 = data2.featureNames.get(p.getSpecification());
                _builder.append(_get_1, "    ");
                _builder.append(" ");
              }
            }
            _builder.append(":>> ");
            EObject _specification_2 = p.getSpecification();
            String _name_2 = ((Type) _specification_2).getName();
            _builder.append(_name_2, "    ");
            _builder.append(" : ");
            EObject _implementation_2 = p.getImplementation();
            String _name_3 = ((Type) _implementation_2).getName();
            _builder.append(_name_3, "    ");
            _builder.append(";");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    _builder.append("    ");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("//TODO: Auto generated block skeleton");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String content = _builder.toString();
    try {
      final FileWriter writer = new FileWriter(data.path);
      writer.write(content);
      writer.close();
    } catch (final Throwable _t) {
      if (_t instanceof IOException) {
        final IOException e = (IOException)_t;
        e.printStackTrace();
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  public static String getMetadata(final EObject o) {
    OOSEMUtils.OOSEMBlockType _oOSEMBlockType = OOSEMUtils.getOOSEMBlockType(o);
    if (_oOSEMBlockType != null) {
      switch (_oOSEMBlockType) {
        case SPECIFICATION:
          return "specification";
        case DESIGN:
          return "design";
        case INTEGRATION:
          return "integration";
        default:
          return "";
      }
    } else {
      return "";
    }
  }
}
