package hu.bme.mit.sysml.oosem.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import hu.bme.mit.sysml.oosem.generators.BlockGenerationData;
import hu.bme.mit.sysml.oosem.generators.SysMLFileWriter;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.sysml.oosem.wizards.pages.BlockGenerationPage;
import hu.bme.mit.sysml.oosem.generators.IGenerator;

public abstract class BlockGenerationWizard extends Wizard {
	public BlockGenerationWizard(OOSEMProject project, OOSEMBlock block, OOSEMBlockType targetType, IGenerator generator) {
		this.data = new BlockGenerationData(project, block, targetType);
		this.generator = generator;
	}
	
	@Override
    public void addPages() {
		for(var p : pages) {
			addPage(p);
		}
    }

    @Override
    public boolean performFinish() {
        try {
        	for(var p : pages) {
        		p.refreshDataFromUI();
    		}

        	var content = generator.generate(data);
        	SysMLFileWriter.writeFile(data, content);

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
	
	protected final List<BlockGenerationPage> pages = new ArrayList<>();
	
	protected final BlockGenerationData data;
	
	private final IGenerator generator;
}
