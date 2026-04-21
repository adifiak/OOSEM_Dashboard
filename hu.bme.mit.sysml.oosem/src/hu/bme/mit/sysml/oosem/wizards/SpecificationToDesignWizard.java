package hu.bme.mit.sysml.oosem.wizards;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.sysml.oosem.wizards.pages.BasicBlockGenerationPage;
import hu.bme.mit.sysml.oosem.wizards.pages.PropertyPage;
import hu.bme.mit.sysml.oosem.generators.StandaloneOOSEMBlockGenerator;

public class SpecificationToDesignWizard extends BlockGenerationWizard {
	
	public SpecificationToDesignWizard(OOSEMProject project, OOSEMBlock block) {
		super(project, block, OOSEMBlockType.DESIGN, new StandaloneOOSEMBlockGenerator());
		pages.add(new BasicBlockGenerationPage(data, "D_"));
		pages.add(new PropertyPage(data));
	}
	
}
