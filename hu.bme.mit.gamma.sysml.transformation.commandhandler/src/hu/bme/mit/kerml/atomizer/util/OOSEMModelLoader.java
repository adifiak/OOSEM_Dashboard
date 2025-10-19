package hu.bme.mit.kerml.atomizer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.kerml.atomizer.model.OOSEMProject;
import hu.bme.mit.kerml.atomizer.util.OOSEMUtils.*;

public class OOSEMModelLoader {
	public static OOSEMProject LoadModelFromOOSEMProject(String projectName) {
		Set<EObject> specifications = new HashSet<EObject>();
		Set<EObject> designs = new HashSet<EObject>();
		Set<EObject> integrations = new HashSet<EObject>();
		
		var filePaths = getPathsForProject(projectName);
		
		for(var fp : filePaths) {
			processFile(specifications, designs, integrations, fp);
		}
		
		var specsWithDesigns = collectBlocksAndTheirChilds(OOSEMBlockType.SPECIFICATION, designs);
		var designsWithIntegrations = collectBlocksAndTheirChilds(OOSEMBlockType.DESIGN, integrations);
		
		var validationErrors = new HashMap<EObject, List<String>>();
		var validationWarnings = new HashMap<EObject, List<String>>();
		
		validateIntegration(validationErrors, validationWarnings, designsWithIntegrations.blocksWithFamily);

		return new OOSEMProject(specifications, designs, integrations, specsWithDesigns, designsWithIntegrations, validationErrors, validationWarnings);
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

	private static BlockFamilyStructures collectBlocksAndTheirChilds(OOSEMBlockType parentType, Set<EObject> childsSet) {
		var families = new HashMap<EObject, Set<EObject>>();
		var orphans = new HashSet<EObject>();
		
		for (var s : childsSet) {
			if (s instanceof OccurrenceDefinition o) {
				var oosemParents = getParentBlocksWithType(parentType, o);
				if (oosemParents.size() > 1) {
					System.err.println("Several parents found to child. (Multistep refinement is not supported yet.)");
					System.err.println(oosemParents);
					// TODO ...
				} else if (oosemParents.size() == 0) {
					orphans.add(o);
				} else {
					var parent = oosemParents.get(0);
					if (families.containsKey(parent)) {
						var childs = families.get(parent);
						childs.add(o);
					} else {
						var childs = new HashSet<EObject>();
						childs.add(o);
						families.put(parent, childs);
					}
				}
			} else {
				System.err.println("Non-OccurrenceDef. element in getBlockOfTypeWithChilds()");
			}
		}
		return new BlockFamilyStructures(families, orphans);
	}
	
	private static List<Type> getParentBlocksWithType(OOSEMBlockType parentType, OccurrenceDefinition o) {
		return o.allSupertypes().stream()
				.filter(t -> (t.getDeclaredName() != null && !t.getDeclaredName().isEmpty()
						&& !t.getDeclaredName().equals("SpecificationBlock")
						&& !t.getDeclaredName().equals("DesignBlock")
						&& !t.getDeclaredName().equals("IntegrationBlock"))
						&& OOSEMUtils.getOOSEMBlockType(t) == parentType)
				.collect(Collectors.toList());
	}
	
	private static void validateIntegration(Map<EObject, List<String>> validationErrors, Map<EObject, List<String>> validationWarnings, Map<EObject, Set<EObject>> designsWithIntegrations) {
		var nonOrphanIntegrations = new HashSet<EObject>();
		designsWithIntegrations.values().stream().forEach(p -> nonOrphanIntegrations.addAll(p));
		for(var i : nonOrphanIntegrations) {
			if(i instanceof OccurrenceDefinition o) {
				var ownedSpecifications = o.getOwnedMember().stream()
						.filter(OOSEMUtils::filterSpecification)
						.collect(Collectors.toList());
				for(var s : ownedSpecifications) {
					registerValidatorOutput(validationErrors, s, "Integrations of specificationBlocks is not permited.");
				}
			}
		}
		
		for(var d : designsWithIntegrations.keySet()) {
			var specs =  ((Type) d).getOwnedMember().stream()
					.filter(OOSEMUtils::filterSpecification)
					.collect(Collectors.toSet());
			var integrations = designsWithIntegrations.get(d);
			for (var integration : integrations) {
				var integratedBlocks = ((Type) integration).getOwnedMember().stream()
						.filter(OOSEMUtils::filterDesignsAndInegrations)
						.collect(Collectors.toList());
				
				var unintegratedSpecifications = new ArrayList<>(specs);
				
				for(var integratedBlock : integratedBlocks) {
					var redefinedFeatures = FeatureUtil.getAllRedefinedFeaturesOf((Feature)integratedBlock);
					redefinedFeatures.remove(integratedBlock);
					redefinedFeatures = redefinedFeatures.stream().filter(OOSEMUtils::filterSpecification).collect(Collectors.toSet());
					
					unintegratedSpecifications.removeAll(redefinedFeatures);
					
					if(!checkIfIntegrationIsRequired(redefinedFeatures, specs))
						registerValidatorOutput(validationErrors, integratedBlock, "Unrequired integration of block.");
				}
				
				if(!unintegratedSpecifications.isEmpty()) {
					var msg = "Unintegrated specifications:";
					var first = true;
					for(var u : unintegratedSpecifications) {
						if(!first) { msg = msg + ",";first = false;}
						msg = msg + " " + u.getName();
					}
					registerValidatorOutput(validationWarnings, integration, msg);
				}
			}
		}
	}
	
	private static boolean checkIfIntegrationIsRequired(Set<Feature> redefinedFeatures, Set<Element> specs) {
		for(var redefinedFeature : redefinedFeatures) {
			if(specs.contains(redefinedFeature)) return true;
		}
		return false;
	}
	
	private static void registerValidatorOutput(Map<EObject, List<String>> validationOutputContainer, EObject o, String msg) {
		var errorList = validationOutputContainer.get(o);
		if(errorList == null) {
			List<String> errors = new ArrayList<>();
			errors.add(msg);
			validationOutputContainer.put(o, errors);
		} else {
			errorList.add(msg);
		}
	}
	
	public static class BlockFamilyStructures {
		private final Map<EObject, Set<EObject>> blocksWithFamily;
		private final Set<EObject> orphanBlocks;
		
		public BlockFamilyStructures(Map<EObject, Set<EObject>> blocksWithFamily, Set<EObject> orphanBlocks) {
			this.blocksWithFamily = blocksWithFamily;
			this.orphanBlocks = orphanBlocks;
		}
		
		public Map<EObject, Set<EObject>> getBlocksWithFamily(){
			return blocksWithFamily;
		}
		
		public Set<EObject> getOrphanedBlocks(){
			return orphanBlocks;
		}
	}
	
}
