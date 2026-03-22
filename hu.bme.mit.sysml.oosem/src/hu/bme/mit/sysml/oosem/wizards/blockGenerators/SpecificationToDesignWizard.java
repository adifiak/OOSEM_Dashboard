package hu.bme.mit.sysml.oosem.wizards.blockGenerators;

import org.eclipse.jface.wizard.Wizard;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;

import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OpenInFileUtils;

public class SpecificationToDesignWizard extends Wizard{
	
	private BasicBlockGenerationPage page;
	private BasicBlockGenerationData data;
	private OOSEMProject project;
	
	public SpecificationToDesignWizard(OccurrenceDefinition o, OOSEMProject project) {
		data = new BasicBlockGenerationData();
		data.subjectSpecification = o;
		data.path = OpenInFileUtils.getFileForEObject(o).getParent().getFullPath().toString();
		this.project = project;
	}
	
	@Override
    public void addPages() {
        page = new BasicBlockGenerationPage(data, "Design_");
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
        	page.refreshDataFromUI();
        	var content = SpecificationToDesignGenerator.generate(data);
        	SysMLFileWriter.writeFile(data, content, project);
            return true;
        } catch (Exception e) {
            //MessageDialog.openError(getShell(), "Error", e.getMessage());
            return false;
        }
    }
}
