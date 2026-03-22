package hu.bme.mit.sysml.oosem.model.project.implementations;

import java.util.HashSet;
import java.util.Set;

import org.omg.sysml.lang.sysml.Element;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.model.project.implementations.OOSEMProjectImpl.OOSEMProjectImplData;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

public class ProjectValidator {

	
	public ProjectValidator(OOSEMProjectImplData data) {
		this.data = data;
	}
	
	public void validate() {
		validateSpecification();
		validateDesign();
		validateIntegration();
	}
	
	private final OOSEMProjectImplData data;
	
	private void validateSpecification() {
		data.specifications.stream().forEach(block -> {
				validateSpecificationBlockProperties(block);
				validateSpecificationBlockSubsystems(block);
				validateSpecificationBlockUntrackedFeatures(block);
		});
	}

	private void validateSpecificationBlockProperties(OOSEMBlock block) {
		block.getProperties().stream()
			.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.SPECIFICATION;})
			.forEach(feature -> {
				feature.registerError("Specification blocks can only have specification properties.");
				block.registerError("Error(s) present in children.");
		});
	}
	
	private void validateSpecificationBlockSubsystems(OOSEMBlock block) {
		if(!block.getSubsystems().isEmpty()) {
			block.registerError("Specification blocks can't have subsystems.");
		}
	}
	
	private void validateSpecificationBlockUntrackedFeatures(OOSEMBlock block) {
		block.getUntrackedFeatures().stream()
			.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.NONE;})
			.forEach(feature -> {
				feature.registerError("Specification blocks can only contain specification properties and untracked (non-OOSEM) features.");
				block.registerError("Error(s) present in children.");
			});
	}
	
	private void validateDesign() {
		registerOrphanBlocks(data.specificationsWithDesigns.getOrphanedBlocks());
		
		var nonOrphanDesigns = new HashSet<OOSEMBlock>();
		data.specificationsWithDesigns.getBlocksWithFamily().values().stream().forEach(p -> nonOrphanDesigns.addAll(p));
		
		nonOrphanDesigns.stream().forEach(block -> {
			validateDesignBlockProperties(block);
			validateDesignBlockSubsystems(block);
			validateDesignBlockUntrackedFeatures(block);
		});
	}

	private void validateDesignBlockProperties(OOSEMBlock block) {
		block.getProperties().stream()
		.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.DESIGN;})
		.forEach(feature -> {
			if(feature.getOOSEMBlockType() == OOSEMBlockType.SPECIFICATION) {
				feature.registerWarning("Properties should be updated to match the parent's OOSEM phase.");
				block.registerWarning("Warning(s) present in children.");
			} else if(feature.getOOSEMBlockType() == OOSEMBlockType.INTEGRATION) {
				feature.registerError("Design blocks can only have design properties.");
				block.registerError("Error(s) present in children.");
			}
		});
	}

	private void validateDesignBlockSubsystems(OOSEMBlock block) {
		block.getSubsystems().stream()
		.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.SPECIFICATION;})
		.forEach(feature -> {
			feature.registerError("Subsystems of a design block can only be specifications.");
			block.registerError("Error(s) present in children.");
		});
	}

	private void validateDesignBlockUntrackedFeatures(OOSEMBlock block) {
		block.getUntrackedFeatures().stream()
		.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.NONE;})
		.forEach(feature -> {
			feature.registerError("Design blocks can only contain design properties, specification subsystems and untracked (non-OOSEM) features.");
			block.registerError("Error(s) present in children.");
		});
	}
	
	private void validateIntegration() {
		registerOrphanBlocks(data.designsWithIntegrations.getOrphanedBlocks());
		
		var nonOrphanIntegrations = new HashSet<OOSEMBlock>();
		data.designsWithIntegrations.getBlocksWithFamily().values().stream().forEach(p -> nonOrphanIntegrations.addAll(p));
		
		nonOrphanIntegrations.stream()
		.forEach(block -> {
			validateIntegrationBlockProperties(block);
			validateIntegrationBlockSubsystems(block);
			validateIntegrationBlockUntrackedFeatures(block);
		});
	}

	private void validateIntegrationBlockProperties(OOSEMBlock block) {
		block.getProperties().stream()
		.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.INTEGRATION;})
		.forEach(feature -> {
			if(feature.getOOSEMBlockType() == OOSEMBlockType.DESIGN) {
				feature.registerWarning("Properties should be updated to match the parent's OOSEM phase.");
				block.registerWarning("Warning(s) present in children.");
			} else {
				feature.registerError("Integration blocks can only have integration properties.");
				block.registerError("Error(s) present in children.");
			}
		});
	}

	private void validateIntegrationBlockSubsystems(OOSEMBlock block) {
		var unintegratedSpecifications = new HashSet<OOSEMFeature>();
		block.getSubsystems().stream()
		.filter(f -> {return f.getOOSEMBlockType() == OOSEMBlockType.SPECIFICATION;})
		.forEach(feature -> {
			if(feature.getDefinedIn() == block) {
				feature.registerError("Integrations of specifications is not permited.");
				block.registerError("Error(s) present in children.");
			} else {
				unintegratedSpecifications.add(feature);
			}
		});
		registerUnintegratedSpecifications(block, unintegratedSpecifications);
	}

	private void validateIntegrationBlockUntrackedFeatures(OOSEMBlock block) {
		block.getUntrackedFeatures().stream()
		.filter(f -> {return f.getOOSEMBlockType() != OOSEMBlockType.NONE;})
		.forEach(feature -> {
			feature.registerError("Integration blocks can only contain integration properties, design or integration subsystems and untracked (non-OOSEM) features.");
			block.registerError("Error(s) present in children.");
		});
	}

	private void registerOrphanBlocks(Set<OOSEMBlock> orphans) {
		for(var o : orphans)
			o.registerError("Orphan block: Does not specialize block from previous phase.");
	}

	private void registerUnintegratedSpecifications(OOSEMBlock integration, Set<OOSEMFeature> unintegratedSpecifications) {
		if(!unintegratedSpecifications.isEmpty()) {
			var msg = "Unintegrated specifications:";
			var first = true;
			for(var u : unintegratedSpecifications) {
				if(!first) { msg = msg + ",";first = false;}
				msg = msg + " " + ((Element)u.getObject()).getName();
				u.registerWarning("Unintegreted subsystem.");
			}
			integration.registerWarning(msg);
		}
	}
}
