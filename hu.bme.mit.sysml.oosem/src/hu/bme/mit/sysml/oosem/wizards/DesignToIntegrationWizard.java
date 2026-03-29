package hu.bme.mit.sysml.oosem.wizards;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.sysml.oosem.wizards.pages.BasicBlockGenerationPage;
import hu.bme.mit.sysml.oosem.wizards.pages.IntegrationPage;
import hu.bme.mit.sysml.oosem.wizards.pages.PropertyPage;
import hu.bme.mit.sysml.oosem.generators.StandaloneOOSEMBlockGenerator;

public class DesignToIntegrationWizard extends BlockGenerationWizard {
	
	public DesignToIntegrationWizard(OOSEMProject project, OOSEMBlock block) {
		super(project, block, OOSEMBlockType.INTEGRATION, new StandaloneOOSEMBlockGenerator());
		pages.add(new BasicBlockGenerationPage(data, "I_"));
		pages.add(new PropertyPage(data));
		pages.add(new IntegrationPage(data));
	}
	
}
