package hu.bme.mit.sysml.oosem.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ProjectUtils {
	public static IProject getProjectWithName(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		return root.getProject(projectName);
	}
	
	public static List<String> traverseProjectForPaths(IProject project) {
		List<String> paths = new ArrayList<>();
		try {
			project.accept(new IResourceVisitor() {
			    @Override
			    public boolean visit(IResource resource) {
			        if (resource.getType() == IResource.FILE) {
			            IFile file = (IFile) resource;
			            if(file.getFileExtension().equals("sysml")) {
			            	paths.add(file.getFullPath().toString());
			            }
			        }
			        return true; // still visit children
			    }
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return paths;
	}
}
