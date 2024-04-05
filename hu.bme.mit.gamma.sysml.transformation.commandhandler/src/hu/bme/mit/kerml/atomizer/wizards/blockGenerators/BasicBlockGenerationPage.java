package hu.bme.mit.kerml.atomizer.wizards.blockGenerators;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class BasicBlockGenerationPage extends WizardPage {
	
	public BasicBlockGenerationPage(BasicBlockGenerationData data, String defaultBlocknamePrefix) {
		super("Specification to Design Block Wizard");
		setTitle("Specification to Design Block Page");
        setDescription("Helps in generating the skeleton of a design block based on the underlying specification block.");
        this.data = data;
        this.defaultBlocknamePrefix = defaultBlocknamePrefix;
	}
	
	@Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Definition block name:");
        blockNameText = new Text(container, SWT.BORDER);
        blockNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        blockNameText.setText(defaultBlocknamePrefix + data.subjectSpecification.getName());
        
        new Label(container, SWT.NONE).setText("Output file:");
        Composite browseComp = new Composite(container, SWT.NONE);
        browseComp.setLayout(new GridLayout(2, false));
        browseComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        outputPathText = new Text(browseComp, SWT.BORDER);
        outputPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button browseBtn = new Button(browseComp, SWT.PUSH);
        browseBtn.setText("Browse...");
        browseBtn.addListener(SWT.Selection, e -> {
            FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
            dialog.setFilterExtensions(new String[]{"*.sysml", "*.*"});
            String path = dialog.open();
            if (path != null) outputPathText.setText(path);
        });

        setControl(container);
        setPageComplete(true);
    }
	
	public void refreshDataFromUI() {
		data.blockName = blockNameText.getText();
		data.path = outputPathText.getText();
	}
	
	private Text blockNameText;
	private Text outputPathText;
	private Composite container;
	
	private BasicBlockGenerationData data;
	private String defaultBlocknamePrefix;

}
