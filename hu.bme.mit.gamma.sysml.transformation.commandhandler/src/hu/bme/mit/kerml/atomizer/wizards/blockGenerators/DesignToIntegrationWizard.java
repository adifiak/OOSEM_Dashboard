package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import org.eclipse.jface.wizard.Wizard;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;

import hu.bme.mit.kerml.atomizer.model.OOSEMProject;

public class DesignToIntegrationWizard  extends Wizard{
	
	public DesignToIntegrationWizard(OccurrenceDefinition o, OOSEMProject project) {
		data = new BasicBlockGenerationData();
		data.subjectSpecification = o;
		data2 = new IntegrationData();
		data2.subjectDesign = o;
		data2.project = project;
	}
	
	@Override
    public void addPages() {
        page = new BasicBlockGenerationPage(data, "Integration_");
        addPage(page);
        page2 = new IntegrationPage(data2);
        addPage(page2);
    }

    @Override
    public boolean performFinish() {
        try {
        	page.refreshDataFromUI();
        	page2.refreshDataFromUI();
        	DesignToIntegrationGenerator.generate(data, data2);
            return true;
        } catch (Exception e) {
            //MessageDialog.openError(getShell(), "Error", e.getMessage());
            return false;
        }
    }
    
	private BasicBlockGenerationPage page;
	private BasicBlockGenerationData data;
	private IntegrationPage page2;
	private IntegrationData data2;
}
