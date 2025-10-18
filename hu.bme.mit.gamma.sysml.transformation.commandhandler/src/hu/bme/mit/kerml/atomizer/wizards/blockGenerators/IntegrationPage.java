package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;

public class IntegrationPage extends WizardPage {
	public IntegrationPage(IntegrationData data) {
		super("Design to Integration Block Wizard");
		setTitle("Design to Integration Block Page");
        setDescription("Helps in generating the skeleton of an integration block based on the underlying design block. Choosing 'Keep empty for now...' as an implementation will generate a comment with the template of a redefinition. Keeping the text field for the new names empty will keep the old name of the feature.");
        this.data = data;
	}
	
	@Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        
        specs = OOSEMUtils.getSpecificationsInDesignBlock(data.subjectDesign);
        for (var spec : specs) {
                new Label(container, SWT.NONE).setText("Implementations of " + OOSEMUtils.getTextOfType((Type)spec) + ":");
                ComboViewer combo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
                
                List<OOSEMIntegrationConfig> options = new ArrayList<>();
                var emptyItem = new OOSEMIntegrationConfig(spec);
                options.add(emptyItem);
                data.project.getPossibleImplementationsOfSpecification(spec).stream().forEach(p -> options.add(new OOSEMIntegrationConfig(spec, p)));;
                
                combo.setContentProvider(ArrayContentProvider.getInstance());
                combo.setLabelProvider(new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                    	var block = ((OOSEMIntegrationConfig) element).getImplementation();
                    	if(block != null) {
                    		return OOSEMUtils.getDecoratedName((Type)block);
                    	}
                    	return "Keep empty for now...";
                    }
                });
                combo.setInput(options);
                var selection = new StructuredSelection(emptyItem);
                combo.setSelection(selection);
                combo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                dynamicCombos.add(combo);
                
                new Label(container, SWT.NONE).setText("New name for" + OOSEMUtils.getTextOfType((Type)spec) + ":");
                var blockNameText = new Text(container, SWT.BORDER);
                blockNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                //blockNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                blockNameText.addSegmentListener(event -> data.featureNames.put(spec, blockNameText.getText()));
        }
        
        setControl(container);
        setPageComplete(true);
    }
	
	public void refreshDataFromUI() {
		data.configs = dynamicCombos.stream().map(p -> {
				StructuredSelection sel = (StructuredSelection) p.getSelection();
				OOSEMIntegrationConfig res = (OOSEMIntegrationConfig) sel.getFirstElement();
				return res;
			}).collect(Collectors.toList());
	}
	
	public class OOSEMIntegrationConfig {
		OOSEMIntegrationConfig(EObject specification){ this.spec = specification;}
		OOSEMIntegrationConfig(EObject specification, EObject block) { this(specification);this.implementation = block; }
		
		public EObject getImplementation() { return implementation;}
		public EObject getSpecification() { return spec;}
		
		private EObject implementation;
		private EObject spec;
	}
	
	private Composite container;
	
	private IntegrationData data;
	
	private List<ComboViewer> dynamicCombos = new ArrayList<>();
	private List<EObject> specs;
}
