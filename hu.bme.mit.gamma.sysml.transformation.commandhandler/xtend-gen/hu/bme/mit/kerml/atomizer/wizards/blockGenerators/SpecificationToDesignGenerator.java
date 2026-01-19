package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import java.io.FileWriter;
import java.io.IOException;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class SpecificationToDesignGenerator {
  public static void generate(final BasicBlockGenerationData data) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    _builder.append(data.blockName);
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
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
    _builder.append("#design ");
    String _sysMLType = GeneratorUtils.getSysMLType(data.subjectSpecification);
    _builder.append(_sysMLType, "    ");
    _builder.append(" def ");
    _builder.append(data.blockName, "    ");
    _builder.append(" :> ");
    String _name = data.subjectSpecification.getName();
    _builder.append(_name, "    ");
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.append("        ");
    _builder.append("//TODO: Auto-generated block skeleton");
    _builder.newLine();
    _builder.append("    ");
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
}
