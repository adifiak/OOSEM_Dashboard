package hu.bme.mit.sysml.oosem.wizards.pages;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import hu.bme.mit.sysml.oosem.generators.BlockGenerationData;

public class BasicBlockGenerationPage extends BlockGenerationPage {
	
	public BasicBlockGenerationPage(BlockGenerationData data, String defaultBlocknamePrefix) {
		super("Specification to Design Block Wizard");
		setTitle("Specification to Design Block Page");
        setDescription("Helps in generating the skeleton of a design block based on the underlying specification block.");
        this.data = data;
        data.setBlockName(defaultBlocknamePrefix + data.getBlockName());
	}
	
	@Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Definition block name:");
        blockNameText = new Text(container, SWT.BORDER);
        blockNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        blockNameText.setText(data.getBlockName());
        blockNameText.addListener(SWT.Modify, event -> {
            if (event.doit)
            	validateLocation();
        });
        
        new Label(container, SWT.NONE).setText("Output file:");
        Composite browseComp = new Composite(container, SWT.NONE);
        browseComp.setLayout(new GridLayout(2, false));
        browseComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        outputPathText = new Text(browseComp, SWT.BORDER);
        outputPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        outputPathText.setText(data.getPath());
        outputPathText.addListener(SWT.Modify, event -> {
            if (event.doit)
            	validateLocation();
        });

        Button browseBtn = new Button(browseComp, SWT.PUSH);
        browseBtn.setText("Browse...");
        browseBtn.addListener(SWT.Selection, e -> {
        	DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
        	dialog.setFilterPath(data.getPath());
            String path = dialog.open();
            if (path != null) outputPathText.setText(path);
        });
        
        refreshDataFromUI();
        validateLocation();

        setControl(container);
        setPageComplete(true);
    }
	
	public void refreshDataFromUI() {
		data.setBlockName(blockNameText.getText());
		data.setPath(outputPathText.getText());
	}
	
	private void validateLocation() {
		var path = outputPathText.getText() + "/" + blockNameText.getText() + ".sysml";
    	if(new File(path).exists()) {
    		setMessage("File already exists at location", IMessageProvider.ERROR);
    	} else {
    		setMessage(null);
    	}
	}
	
	private Text blockNameText;
	private Text outputPathText;
	private Composite container;
	
	private BlockGenerationData data;
}
