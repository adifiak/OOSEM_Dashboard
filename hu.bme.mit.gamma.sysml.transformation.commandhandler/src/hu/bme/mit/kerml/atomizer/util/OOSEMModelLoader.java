package hu.bme.mit.kerml.atomizer.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.kerml.atomizer.model.OOSEMProject;

public class OOSEMModelLoader {
	public static OOSEMProject LoadModelFromOOSEMProject(String projectName) {
		Set<EObject> specifications = new HashSet<EObject>();
		Set<EObject> designs = new HashSet<EObject>();
		Set<EObject> integrations = new HashSet<EObject>();
		
		var filePaths = getPathsForProject(projectName);
		
		for(var fp : filePaths) {
			processFile(specifications, designs, integrations, fp);
		}
		
		return new OOSEMProject(specifications, designs, integrations);
	}
	
	private static void processFile(Set<EObject> specifications, Set<EObject> designs, Set<EObject> integrations, String file) {
		var root = getRoot(file);
		processNode(specifications, designs, integrations, root);
	}
	
	private static void processNode(Set<EObject> specifications, Set<EObject> designs, Set<EObject> integrations, EObject node) {
		if(node instanceof Namespace n) {
			if(sortNode(specifications, designs, integrations, node)) {
				for(var m : n.getOwnedMember()) {
					if(m instanceof org.omg.sysml.lang.sysml.LibraryPackage p) {
						if(p.getQualifiedName().equals("OOSEM")) continue;
					}
					processNode(specifications, designs, integrations, m);
				}
			}
		}
	}
	
	//TODO: Might optimize that if you couldn't add it to a set (but tried), then you can stop traversing since you have been here
	private static boolean sortNode(Set<EObject> specifications, Set<EObject> designs, Set<EObject> integrations, EObject node) {
		if(node instanceof OccurrenceDefinition n) {
			var types = n.allSupertypes();
        	Boolean spec = false; var desi = false; var inte = false;
        	List<String> exclusionList = List.of("SpecificationBlock", "DesignBlock", "IntegrationBlock", "specificationBlocks", "designBlocks", "integrationBlocks","components");
        	
        	for (var type : types) {
        		if (type instanceof Element e){
        			var name = e.getDeclaredName();
        			var nodeName = n.getDeclaredName();
        			
        			if(name != null) {
        				if(nodeName == null || exclusionList.contains(nodeName)) {
        					continue;
        				}
        				
        				if(name.equals("SpecificationBlock")) {
                			spec = true;
                		} else if(name.equals("DesignBlock")) {
                			desi = true;
                		} else if(name.equals("IntegrationBlock")) {
                			inte = true;
                		}
        			}
        		}
        	}
        	
        	if(inte){
        		return integrations.add(node);
        	} else if(desi){
        		return designs.add(node);
        	} else if(spec){
        		return specifications.add(node);
        	}
		}
		return true;
	}
	
	private static EObject getRoot(String path) {
		URI relativeUri = URI.createPlatformResourceURI(path, true);
		
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.getResource(relativeUri, true);
		EObject object = resource.getContents().get(0);
		//TODO: Find out the meaning of PARSE_ALL
		/*
		resource = object.eResource();
		resourceSet = resource.getResourceSet();
		EcoreUtil.resolveAll(resourceSet);
		*/
		return object;
	}
	
	private static List<String> getPathsForProject(String projectName) { //"OOSEMTestProject"
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		IProject project = root.getProject(projectName);
		
		return traverseProjectForPaths(project);
	}
	
	private static List<String> traverseProjectForPaths(IProject project) {
		List<String> paths = new ArrayList<>();
		try {
			project.accept(new IResourceVisitor() {
			    @Override
			    public boolean visit(IResource resource) {
			        if (resource.getType() == IResource.FILE) {
			            IFile file = (IFile) resource;
			            if(file.getFileExtension().equals("sysml")) {
			            	System.out.println("File: " + file.getFullPath());
			            	paths.add(file.getFullPath().toString());
				            //roots.add(ModelLoader.getRoot(file.getFullPath().toString()));
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
