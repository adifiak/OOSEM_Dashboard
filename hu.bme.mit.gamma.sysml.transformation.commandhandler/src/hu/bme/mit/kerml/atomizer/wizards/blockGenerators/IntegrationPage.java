package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;

public class IntegrationPage extends WizardPage {
	public IntegrationPage(IntegrationData data) {
		super("Design to Integration Block Wizard");
		setTitle("Design to Integration Block Page");
        setDescription("Helps in generating the skeleton of an integration block based on the underlying design block.");
        this.data = data;
	}
	
	@Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        
        specs = OOSEMUtils.getSpecificationsInDesignBlock(data.subjectDesign);
        for (var spec : specs) {
                new Label(container, SWT.NONE).setText("Implementations of " + OOSEMUtils.getTextOfType((Type)spec) + ":");
                Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
                List<String> options = new ArrayList<>();
                options.add(comboPlaceholder);
                options.add("TODO...");
                
                System.out.println(OOSEMUtils.getPossibleImplementationsOfSpecification(spec));
                
                combo.setItems(options.stream().toArray(String[]::new));
                combo.select(0);
                combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                dynamicCombos.add(combo);
        	
        }
        
        setControl(container);
        setPageComplete(true);
    }
	
	public void refreshDataFromUI() {

	}
	
	private Composite container;
	
	private IntegrationData data;
	
	private List<Combo> dynamicCombos = new ArrayList<>();
	private List<EObject> specs;
	
	private final String comboPlaceholder = "Implementation to integrate...";
}
