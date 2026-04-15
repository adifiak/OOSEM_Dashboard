package hu.bme.mit.sysml.oosem.wizards.pages;

import hu.bme.mit.sysml.oosem.generators.BlockGenerationData;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration.RefinementWorkflow;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;

public class PropertyPage extends RefinementPage{
	public PropertyPage(BlockGenerationData data) {
		super(
				data,
				"Property page",
				"Helps in updating the properties along their eclosing block. Keeping the text field for the new names empty will keep the old name of the feature.",
				OOSEMBlock::getProperties,
				(OOSEMBlock block) -> {
					return block.getOOSEMBlockType() == OOSEMUtils.getTypeForNextPhase(data.getSubject().getOOSEMBlockType());
				},
				BlockGenerationData::registerPropertyRefinement,
				RefinementWorkflow.GENERATE_FRAME);
	}
}
