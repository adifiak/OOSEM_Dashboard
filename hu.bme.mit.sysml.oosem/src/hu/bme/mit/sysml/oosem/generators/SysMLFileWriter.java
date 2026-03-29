package hu.bme.mit.sysml.oosem.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public class SysMLFileWriter {
	public static void writeFile(BlockGenerationData data, String content) {
		try {
        	var path = data.getPath() + "/" + data.getBlockName()+ ".sysml";
        	var file = new File(path);
			file.createNewFile(); // if file already exists will do nothing
			var writer = new FileWriter(path);
            writer.write(content);
            writer.close();
            if(buildProjectAutomaticaly) {
            	data.getProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
            	data.getProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
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
