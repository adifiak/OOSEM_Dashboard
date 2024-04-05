package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import org.eclipse.jface.wizard.Wizard;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;

public class SpecificationToDesignWizard extends Wizard{
	
	private BasicBlockGenerationPage page;
	private BasicBlockGenerationData data;
	
	public SpecificationToDesignWizard(OccurrenceDefinition o) {
		data = new BasicBlockGenerationData();
		data.subjectSpecification = o;
	}
	
	@Override
    public void addPages() {
        page = new BasicBlockGenerationPage(data, "Design_");
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        // Fill output model from page controls
        //page.applyValuesToModel();

        try {
        	page.refreshDataFromUI();
        	SpecificationToDesignGenerator.generate(data);
            return true;
        } catch (Exception e) {
            //MessageDialog.openError(getShell(), "Error", e.getMessage());
            return false;
        }
    }
}
