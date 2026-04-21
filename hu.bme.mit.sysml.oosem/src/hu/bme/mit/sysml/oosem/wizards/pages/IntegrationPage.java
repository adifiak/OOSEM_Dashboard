package hu.bme.mit.sysml.oosem.wizards.pages;

import hu.bme.mit.sysml.oosem.generators.BlockGenerationData;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration.RefinementWorkflow;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

public class IntegrationPage extends RefinementPage {
	public IntegrationPage(BlockGenerationData data) {
		super(
				data,
				"Integration page",
				"Helps in generating the skeleton of an integration block based on the underlying design block. Keeping the text field for the new names empty will keep the old name of the feature.",
				OOSEMBlock::getSubsystems,
				(OOSEMBlock block) -> {
					return block.getOOSEMBlockType() == OOSEMBlockType.DESIGN || block.getOOSEMBlockType() == OOSEMBlockType.INTEGRATION;
				},
				BlockGenerationData::registerSubsystemRefinement,
				RefinementWorkflow.CHOOSE_EXISTING);
	}
}
