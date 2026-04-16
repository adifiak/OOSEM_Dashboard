package hu.bme.mit.sysml.oosem.wizards.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import hu.bme.mit.sysml.oosem.generators.BlockGenerationData;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration.RefinementWorkflow;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.views.OOSEMModelLabelProvider;

public abstract class RefinementPage extends BlockGenerationPage{
	
	public RefinementPage(BlockGenerationData data, String title, String description, Function<OOSEMBlock, Set<OOSEMFeature>> getSetToRefine, Predicate<OOSEMBlock> refinementTypeFiletringPredicate, BiConsumer<BlockGenerationData, RefinementConfiguration> registerOperation, RefinementWorkflow defaultWorkflow) {
		super(title);
		setTitle(title);
		setDescription(description);
		this.data = data;
		this.getSetToRefine = getSetToRefine;
		this.refinementTypeFiletringPredicate = refinementTypeFiletringPredicate;
		this.registerOperation = registerOperation;
		this.defaultWorkflow = defaultWorkflow;
	}
	
	public void createControl(Composite parent) {
		var scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
        container = new Composite(scrolledComposite, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        
        Set<OOSEMFeature> features = getSetToRefine.apply(data.getSubject());
        
        for (var feat : features) {
        	featurePanels.add(new FeaturePanel(container, feat, refinementTypeFiletringPredicate));
        }
        
        scrolledComposite.setContent(container);
        scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        setControl(scrolledComposite);
        
        validatePage();
    }
	
	public void refreshDataFromUI() {
		featurePanels.stream().forEach(p -> {
			var config = p.getConfiguration();
			registerOperation.accept(data, config);
		});
	}
	
	private List<FeaturePanel> featurePanels = new ArrayList<>();
	private Composite container;
	
	private final BlockGenerationData data;
	private final RefinementWorkflow defaultWorkflow;
	
	private final Function<OOSEMBlock, Set<OOSEMFeature>> getSetToRefine;
	private final BiConsumer<BlockGenerationData, RefinementData.RefinementConfiguration> registerOperation;
	private final Predicate<OOSEMBlock> refinementTypeFiletringPredicate;
	
	protected void validatePage() {
		boolean foundError = false;
		
		for(var fp : featurePanels) {
			if(!fp.validate()) {
				foundError = true;
			}
		}
		
		if(foundError) {
			setMessage("⚠️ At least one panel contains invalid information.");
	        setPageComplete(false);
		} else {
			setMessage(null);
	        setPageComplete(true);
		}
	}
	
	private class FeaturePanel {

		FeaturePanel(Composite parent, OOSEMFeature feature, Predicate<OOSEMBlock> refinementTypeFiletringPredicate) {
			this.feature = feature;
			container = new Composite(parent, SWT.BORDER);
			container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			container.setLayout(new GridLayout(2, true));
			
			Image warningImage = FieldDecorationRegistry.getDefault()
					.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING)
                    .getImage();
			
			initWorkflowControls();
			initRedefineFeatureNameControls();
            initChooseExistingControls(warningImage);
            initStubGenerationControls(warningImage);
            
            workflowCombo.setSelection(new StructuredSelection(defaultWorkflow)); // Select default workflow, here to not mess with uninitialized elements
            container.layout(true, true);
		}
		
		public RefinementConfiguration getConfiguration() {
			var selectedType = (OOSEMBlock) ((StructuredSelection) typeCombo.getSelection()).getFirstElement();
			
			return new RefinementConfiguration(feature, selectedType, redefinedNameText.getText(), (RefinementWorkflow) workflowCombo.getStructuredSelection().getFirstElement(), frameTypeNameText.getText());
		}
		
		public boolean validate() {
			RefinementWorkflow selectedWorkflow = (RefinementWorkflow) workflowCombo.getStructuredSelection().getFirstElement();
			
			switch(selectedWorkflow) {
				case CHOOSE_EXISTING: return validateChooseExisting();
				case GENERATE_STUB: return validateGenerateStub();
				default: return validateDefaultWorkflow();
			}
		}
		
		private final OOSEMFeature feature;
		
		private final Composite container;
		private ComboViewer workflowCombo;
		private Label typeComboLabel;
		private ComboViewer typeCombo;
		private ControlDecoration typeComboError;
		private Label redefinedNameTextLabel;
		private Text redefinedNameText;
		private Label frameTypeNameLabel;
		private Text frameTypeNameText;
		private ControlDecoration frameTypeNameTextError;
		
		private void initWorkflowControls() {
			new Label(container, SWT.NONE).setText("Select workflow for " + feature.getName() + ":");
			workflowCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			workflowCombo.setContentProvider(ArrayContentProvider.getInstance());
	        
			workflowCombo.setLabelProvider(new LabelProvider() {
	            @Override
	            public String getText(Object element) {
	                RefinementWorkflow value = (RefinementWorkflow) element;
	                switch (value) {
	                    case SKIP: return "Skip";
	                    case CHOOSE_EXISTING: return "Choose Existing";
	                    case GENERATE_STUB: return "Generate Stub";
	                    default: return value.name();
	                }
	            }
	        });
			workflowCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			workflowCombo.setInput(RefinementWorkflow.values());
			
			workflowCombo.addSelectionChangedListener(workflowSelectionListener());
		}
		
		private void initRedefineFeatureNameControls() {
			redefinedNameTextLabel = new Label(container, SWT.NONE);
			redefinedNameTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			redefinedNameTextLabel.setText("New name for " + feature.getName() + ":");
			
            redefinedNameText = new Text(container, SWT.BORDER);
            redefinedNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		private void initChooseExistingControls(Image warningImage) {
			typeComboLabel = new Label(container, SWT.NONE);
            typeComboLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            typeComboLabel.setText("Implementations of " + feature.getName() + ":");
            
            typeCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            typeCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            typeCombo.setContentProvider(ArrayContentProvider.getInstance());
            typeCombo.setLabelProvider(new OOSEMModelLabelProvider());
            typeCombo.addSelectionChangedListener(event -> {validatePage();});
            List<OOSEMBlock> options = new ArrayList<>(feature.getType().getAllChilds().stream()
            		.filter(refinementTypeFiletringPredicate)
            		.collect(Collectors.toList()));
            typeCombo.setInput(options);
            
            typeComboError = new ControlDecoration(typeCombo.getCombo(), SWT.LEFT | SWT.TOP);
            typeComboError.setImage(warningImage);
            typeComboError.setDescriptionText("Please select an implementation.");
            typeComboError.hide();
		}
		
		private void initStubGenerationControls(Image warningImage) {
			frameTypeNameLabel = new Label(container, SWT.NONE);
            frameTypeNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            frameTypeNameLabel.setText("Name for new type:");
            
            frameTypeNameText = new Text(container, SWT.BORDER);
            frameTypeNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            frameTypeNameTextError = new ControlDecoration(frameTypeNameText, SWT.LEFT | SWT.TOP);
            frameTypeNameTextError.setImage(warningImage);
            frameTypeNameTextError.setDescriptionText("Please specify a name.");
            frameTypeNameTextError.hide();
            
            frameTypeNameText.addModifyListener(event -> validatePage());
		}
		
		private boolean validateDefaultWorkflow() {
			frameTypeNameTextError.hide();
			typeComboError.hide();
			return true;
		}

		private boolean validateGenerateStub() {
			typeComboError.hide();
			if(!frameTypeNameText.getText().isEmpty()) {
				frameTypeNameTextError.hide();
				return true;
			} else {
				frameTypeNameTextError.show();
				return false;
			}
		}

		private boolean validateChooseExisting() {
			frameTypeNameTextError.hide();
			if(typeCombo.getStructuredSelection().getFirstElement() != null) {
				typeComboError.hide();
				return true;
			} else {
				typeComboError.show();
				return false;
			}
		}

		private ISelectionChangedListener workflowSelectionListener() {
			return event -> {
				RefinementWorkflow selectedWorkflow = (RefinementWorkflow) workflowCombo.getStructuredSelection().getFirstElement();
				
				switch(selectedWorkflow) {
					case CHOOSE_EXISTING:
						setPanelControlVisibilities(true, true, false);
						break;
					case GENERATE_STUB:
						setPanelControlVisibilities(false, true, true);
						break;
					default:
						setPanelControlVisibilities(false, false, false);
						break;
				}
				container.layout();
				validatePage();
			};
		}
		
		private void setPanelControlVisibilities(boolean showTypeSelection, boolean showRedefinedName, boolean showFrameTypeName) {
			setControlVisibility(typeComboLabel, showTypeSelection);
			setControlVisibility(typeCombo.getControl(), showTypeSelection);

			setControlVisibility(redefinedNameTextLabel, showRedefinedName);
			setControlVisibility(redefinedNameText, showRedefinedName);

			setControlVisibility(frameTypeNameLabel, showFrameTypeName);
			setControlVisibility(frameTypeNameText, showFrameTypeName);
		}
		
		private void setControlVisibility(Control control, boolean visible) {
			Object layoutData = control.getLayoutData();
		    if (layoutData instanceof GridData) {
		        ((GridData) layoutData).exclude = !visible;
		    }
		    control.setVisible(visible);
		}
	}

}
