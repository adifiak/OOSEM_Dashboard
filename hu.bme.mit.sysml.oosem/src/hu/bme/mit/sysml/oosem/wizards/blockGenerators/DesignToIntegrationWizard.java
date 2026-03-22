package hu.bme.mit.sysml.oosem.wizards.blockGenerators;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;

import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OpenInFileUtils;

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
        	var content = DesignToIntegrationGenerator.generate(data, data2);
        	SysMLFileWriter.writeFile(data, content, data2.project);

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
