package hu.bme.mit.sysml.oosem.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.sysml.oosem.model.OOSEMModelLoader.BlockFamilyStructures;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;

public class OOSEMModelValidator {
	static void validateOOSEMModel(Map<EObject, Set<String>> validationErrors, Map<EObject, Set<String>> validationWarnings, Set<EObject> specifications, BlockFamilyStructures designs, BlockFamilyStructures integrations) {
		validateSpecification(validationErrors, validationWarnings, specifications);
		
		validateDesign(validationErrors, validationWarnings, designs.getBlocksWithFamily());
		registerOrphanBlocks(validationErrors, designs.getOrphanedBlocks());
		
		validateIntegration(validationErrors, validationWarnings, integrations.getBlocksWithFamily());
		registerOrphanBlocks(validationErrors, integrations.getOrphanedBlocks());
	}
	
	private static void validateSpecification(Map<EObject, Set<String>> validationErrors, Map<EObject, Set<String>> validationWarnings, Set<EObject> specifications) {
		validateChildren(validationErrors, specifications, OOSEMUtils::filterDesignsAndInegrations, "Specifications can not contain designs or integrations.");
	}
	
	private static void validateDesign(Map<EObject, Set<String>> validationErrors, Map<EObject, Set<String>> validationWarnings, Map<EObject, Set<EObject>> specificationsWithDesigns) {
		var nonOrphanDesigns = new HashSet<EObject>();
		specificationsWithDesigns.values().stream().forEach(p -> nonOrphanDesigns.addAll(p));
		validateChildren(validationErrors, nonOrphanDesigns, OOSEMUtils::filterDesignsAndInegrations, "Designs can only contain specifications.");
	}
	
	private static void validateIntegration(Map<EObject, Set<String>> validationErrors, Map<EObject, Set<String>> validationWarnings, Map<EObject, Set<EObject>> designsWithIntegrations) {
		var nonOrphanIntegrations = new HashSet<EObject>();
		designsWithIntegrations.values().stream().forEach(p -> nonOrphanIntegrations.addAll(p));

		validateChildren(validationErrors, nonOrphanIntegrations, OOSEMUtils::filterSpecification, "Integrations of specificationBlocks is not permited.");
		
		for(var d : designsWithIntegrations.keySet()) {
			var integrations = designsWithIntegrations.get(d);
			for (var integration : integrations) {
				var integratedBlocks = ((Type) integration).getOwnedMember().stream()
						.filter(OOSEMUtils::filterDesignsAndInegrations)
						.collect(Collectors.toList());
				
				var unintegratedSpecifications = validateUnrequiredIntegrations(validationErrors, d, integration, integratedBlocks);
				registerUnintegratedSpecifications(validationWarnings, integration, unintegratedSpecifications);
			}
		}
	}
	
	private static void registerOrphanBlocks(Map<EObject, Set<String>> validationErrors, Set<EObject> orphans) {
		for(var o : orphans)
			registerValidatorOutput(validationErrors, o, "Orphan block: Does not specialize block from previous phase.");
	}

	private static void registerUnintegratedSpecifications(Map<EObject, Set<String>> validationWarnings,
			EObject integration, ArrayList<Element> unintegratedSpecifications) {
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

	private static ArrayList<Element> validateUnrequiredIntegrations(Map<EObject, Set<String>> validationErrors,EObject parent, EObject integration, List<Element> integratedBlocks) {
		var specs =  ((Type) parent).getOwnedMember().stream()
				.filter(OOSEMUtils::filterSpecification)
				.collect(Collectors.toSet());
		
		var unintegratedSpecifications = new ArrayList<>(specs);
		var errorInChildren = false;
		
		for(var integratedBlock : integratedBlocks) {
			var redefinedFeatures = FeatureUtil.getAllRedefinedFeaturesOf((Feature)integratedBlock);
			redefinedFeatures.remove(integratedBlock);
			redefinedFeatures = redefinedFeatures.stream().filter(OOSEMUtils::filterSpecification).collect(Collectors.toSet());
			
			unintegratedSpecifications.removeAll(redefinedFeatures);
			
			if(!checkIfIntegrationIsRequired(redefinedFeatures, specs)) {
				registerValidatorOutput(validationErrors, integratedBlock, "Unrequired integration of block.");
				errorInChildren = true;
			}
		}
		if(errorInChildren) {
			registerValidatorOutput(validationErrors, integration, "Error(s) present in children.");
		}
		return unintegratedSpecifications;
	}
	
	private static void validateChildren(Map<EObject, Set<String>> validationErrors, Set<EObject> blocks, Predicate<? super Element> filterLambda, String message) {
		for(var i : blocks) {
			if(i instanceof OccurrenceDefinition o) {
				var filteredChildren = o.getOwnedMember().stream()
						.filter(filterLambda)
						.collect(Collectors.toList());
				
				if(filteredChildren.isEmpty()) continue;
				
				for(var s : filteredChildren) {
					registerValidatorOutput(validationErrors, s, message);
				}
				registerValidatorOutput(validationErrors, o, "Error(s) present in children.");
			}
		}
	}
	
	private static boolean checkIfIntegrationIsRequired(Set<Feature> redefinedFeatures, Set<Element> specs) {
		for(var redefinedFeature : redefinedFeatures) {
			if(specs.contains(redefinedFeature)) return true;
		}
		return false;
	}
	
	private static void registerValidatorOutput(Map<EObject, Set<String>> validationOutputContainer, EObject o, String msg) {
		var errorSet = validationOutputContainer.get(o);
		if(errorSet == null) {
			Set<String> errors = new HashSet<>();
			errors.add(msg);
			validationOutputContainer.put(o, errors);
		} else {
			errorSet.add(msg);
		}
	}
}
