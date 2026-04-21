package hu.bme.mit.sysml.oosem.model.project.implementations;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.LibraryPackage;
import org.omg.sysml.lang.sysml.MetadataUsage;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.model.project.implementations.OOSEMProjectImpl.OOSEMProjectImplData;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.ProjectUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

public class ProjectProcessor {

	public ProjectProcessor(IProject project, OOSEMProjectImplData data, IProgressMonitor monitor) {
		this.project = project;
		this.data = data;
		this.monitor = monitor;
	}
	
	public void process() {
		loadBlocks();
		registerSpecializations();
		registerFeatures();
	}
	
	private void loadBlocks() {
		var filePaths = ProjectUtils.traverseProjectForPaths(project);
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, filePaths.size());
		
		ResourceSet resourceSet = new ResourceSetImpl();
		for(var fp : filePaths) {
			subMonitor.setTaskName("Processing: " + fp);
			processFile(resourceSet, fp);
			subMonitor.worked(1);
		}
	}
	
	private void registerSpecializations() {
		for(var block : data.blockCatalog.keySet()) {
			if(block instanceof Type type) {
				var childBlock = data.blockCatalog.get(type);
				type.supertypes(true).stream()
					.map(p -> data.blockCatalog.get(p))
					.filter(p -> p!= null)
					.forEach(p -> {
						childBlock.registerParent(p);
						p.registerChildren(childBlock);
					});
			}
		}
		
		data.specificationsWithDesigns = new BlockFamilyStructures(data, OOSEMBlockType.SPECIFICATION, data.designs);
		data.designsWithIntegrations = new BlockFamilyStructures(data, OOSEMBlockType.DESIGN, data.integrations);
	}
	
	private void registerFeatures() {
		processFeaturesForPhase(data.specifications);
		processFeaturesForPhase(data.designs);
		processFeaturesForPhase(data.integrations);
	}
	
	private void processFeaturesForPhase(Set<OOSEMBlock> blocksInPhase) {
		var remaining = new HashSet<OOSEMBlock>(blocksInPhase);
		
		while(!remaining.isEmpty()) {
			var blocksToProcess = remaining.stream()
				.filter(p -> {
					return p.getParents().stream().noneMatch(remaining::contains);
				})
				.collect(Collectors.toSet());
			
			if(blocksToProcess.isEmpty()) {
				throw new RuntimeException("Cycle detected in the model.");
			}
			
			remaining.removeAll(blocksToProcess);
			
			for(var block : blocksToProcess) {
				registerFeaturesForBlock(block);
			}
		}
	}
	
	private void registerFeaturesForBlock(OOSEMBlock block) {
		for(var parent : block.getParents()) {
			parent.getProperties().stream().forEach(p -> block.registerInheritedProperty(p));
			parent.getSubsystems().stream().forEach(p -> block.registerInheritedSubsystem(p));
			parent.getUntrackedFeatures().stream().forEach(p -> block.registerInheritedUntrackedFeature(p));
		}
		
		for(var member : ((Namespace)block.getObject()).getOwnedMember()) {
			if(member instanceof MetadataUsage) continue;
			var redefinedFeatures = FeatureUtil.getRedefinedFeaturesOf((Feature)member);
			switch(redefinedFeatures.size()) {
				case 0:
					processNewFeatureOfBlock(block, member);
					break;
				case 1:
					processRedefinedFeatureOfBlock(block, redefinedFeatures.get(0), member);
					break;
				default:
					System.err.println("Multiple redefined features found for feature " + member + " in block " + block.getObject() + ".\n\tFound features: " + redefinedFeatures);
			}
		}
	}
	
	private void processNewFeatureOfBlock(OOSEMBlock block, EObject feature) {
		if(data.featureCatalog.get(feature) != null)
			throw new RuntimeException("Unexpected occurence of feature: " + feature);
		
		var supertypes = OOSEMUtils.getOOSEMDefinitionsToUsage((Type)feature);
		OOSEMBlock type = null;
		if(supertypes != null && !supertypes.isEmpty()) {
			type = data.blockCatalog.get(supertypes.getFirst());
		}
		
		var oosemFeature = new OOSEMFeature(feature, block, type);
		data.featureCatalog.put(feature, oosemFeature);
		
		if(oosemFeature.getOOSEMBlockType() == OOSEMBlockType.SPECIFICATION) {
			switch(block.getOOSEMBlockType()) {
				case SPECIFICATION:
					block.registerProperty(oosemFeature);
					break;
				case DESIGN:
					block.registerSubsystem(oosemFeature);
					break;
				default:
					block.registerUntrackedFeature(oosemFeature);
			}
		} else { // Features can only be introduced as specifications.
			block.registerUntrackedFeature(oosemFeature);
		}
	}
	
	private void processRedefinedFeatureOfBlock(OOSEMBlock block, EObject oldFeature, EObject newFeature) {
		if(data.featureCatalog.get(newFeature) != null)
			throw new RuntimeException("Unexpected occurence of feature: " + newFeature);
		if(data.featureCatalog.get(oldFeature) == null)
			throw new RuntimeException("Redefined feature missing form catalog: " + oldFeature);
		
		var supertypes = OOSEMUtils.getOOSEMDefinitionsToUsage((Type)newFeature);
		OOSEMBlock type = null;
		if(supertypes != null && !supertypes.isEmpty()) {
			type = data.blockCatalog.get(supertypes.getFirst());
		}
		
		var oosemFeature = new OOSEMFeature(newFeature, block, type, data.featureCatalog.get(oldFeature));
		data.featureCatalog.put(newFeature, oosemFeature);
		
		block.redefineFeature(data.featureCatalog.get(oldFeature), oosemFeature);
	}
	
	private void processFile(ResourceSet resourceSet, String file) {
		var root = getRoot(resourceSet, file);
		if(root instanceof Namespace n)
			processNode(n);
	}
	
	private EObject getRoot(ResourceSet resourceSet, String path) {
		URI relativeUri = URI.createPlatformResourceURI(path, true);
		
		Resource resource = resourceSet.getResource(relativeUri, true);
		EObject object = resource.getContents().get(0);
		return object;
	}
	
	private void processNode(Namespace node) {
		var block = data.blockCatalog.get(node);
		
		if(block != null) { return; }
		
		if(OOSEMUtils.getOOSEMBlockType(node) != OOSEMBlockType.NONE && node instanceof OccurrenceDefinition) {
			block = new OOSEMBlock(node);
			data.blockCatalog.put(node, block);
			sortBlock(block);
		}
				
		for(var m : node.getOwnedMember()) {
			if(m instanceof LibraryPackage p && p.getQualifiedName().equals("OOSEM")) { continue; }
				
			if(m instanceof Namespace ns) {
				processNode(ns);
			}
		}
	}
	
	private void sortBlock(OOSEMBlock block) {
		switch(block.getOOSEMBlockType()) {
			case SPECIFICATION:
				data.specifications.add(block);
				break;
			case DESIGN:
				data.designs.add(block);
				break;
			case INTEGRATION:
				data.integrations.add(block);
				break;
			default:
				throw new RuntimeException("Unexpected OOSEM block type.");
		}
	}
	
	private final IProject project;
	private final OOSEMProjectImplData data;
	private final IProgressMonitor monitor;
}