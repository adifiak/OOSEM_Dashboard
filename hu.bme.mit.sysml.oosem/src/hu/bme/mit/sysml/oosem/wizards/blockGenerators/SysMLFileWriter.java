package hu.bme.mit.sysml.oosem.wizards.blockGenerators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;

public class SysMLFileWriter {
	public static void writeFile(BasicBlockGenerationData data, String content, OOSEMProject project) {
		try {
        	var path = data.path + "/" + data.blockName + ".sysml";
        	var file = new File(path);
			file.createNewFile(); // if file already exists will do nothing
			var writer = new FileWriter(path);
            writer.write(content);
            writer.close();
            if(buildProjectAutomaticaly) {
            	project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
            	project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static void setBuildProjectAutomaticaly(boolean val) {buildProjectAutomaticaly = val;}
	
	private static boolean buildProjectAutomaticaly = true;
}
