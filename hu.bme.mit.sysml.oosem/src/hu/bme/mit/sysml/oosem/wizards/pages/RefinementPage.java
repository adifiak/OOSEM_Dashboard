package hu.bme.mit.sysml.oosem.wizards.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import hu.bme.mit.sysml.oosem.generators.BlockGenerationData;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData;
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.views.OOSEMModelLabelProvider;

public abstract class RefinementPage extends BlockGenerationPage{
	
	public RefinementPage(BlockGenerationData data, String title, String description, Function<OOSEMBlock, Set<OOSEMFeature>> getSetToRefine, Predicate<OOSEMBlock> refinementTypeFiletringPredicate, BiConsumer<BlockGenerationData, RefinementConfiguration> registerOperation) {
		super(title);
		setTitle(title);
		setDescription(description);
		this.data = data;
		this.getSetToRefine = getSetToRefine;
		this.refinementTypeFiletringPredicate = refinementTypeFiletringPredicate;
		this.registerOperation = registerOperation;
	}
	
	public void createControl(Composite parent) {
        //container = new ScrolledComposite(parent, SWT.V_SCROLL);
		var scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
        container = new Composite(scrolledComposite, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        
        Set<OOSEMFeature> features = getSetToRefine.apply(data.getSubject());
        
        for (var feat : features) {
        	new FeaturePanel(container, feat, refinementTypeFiletringPredicate);
        }
        
        scrolledComposite.setContent(container);
        scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        setControl(scrolledComposite);
        setPageComplete(true);
    }
	
	public void refreshDataFromUI() {
		featurePanels.stream().forEach(p -> {
			var config = p.getConfiguration();
			registerOperation.accept(data, config);
		});
	}
	
	public class FeaturePanel{

		FeaturePanel(Composite parent, OOSEMFeature feature, Predicate<OOSEMBlock> refinementTypeFiletringPredicate) {
			container = new Composite(parent, SWT.BORDER);
			container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			container.setLayout(new GridLayout(2, true));
			
			new Label(container, SWT.NONE).setText("Implementations of " + feature.getName() + ":");
			typeCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            
            List<OOSEMBlock> options = new ArrayList<>(feature.getType().getAllChilds().stream().filter(refinementTypeFiletringPredicate).collect(Collectors.toList()));
            //var emptyItem = new OOSEMIntegrationConfig(feature);
            //options.add(emptyItem);
            
            typeCombo.setContentProvider(ArrayContentProvider.getInstance());
            typeCombo.setLabelProvider(new OOSEMModelLabelProvider());
            typeCombo.setInput(options);
            //var selection = new StructuredSelection(emptyItem);
            //typeCombo.setSelection(selection);
            typeCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            new Label(container, SWT.NONE).setText("New name for " + feature.getName() + ":");
            redefinedNameText = new Text(container, SWT.BORDER);
            redefinedNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			this.feature = feature;
			featurePanels.add(this);
		}
		
		public RefinementConfiguration getConfiguration() {
			var selectedType = (OOSEMBlock) ((StructuredSelection) typeCombo.getSelection()).getFirstElement();
			
			return new RefinementConfiguration(feature, selectedType, redefinedNameText.getText());
		}
		
		private final OOSEMFeature feature;
		
		private final Composite container;
		private final ComboViewer typeCombo;
		private final Text redefinedNameText;
	}
	
	private List<FeaturePanel> featurePanels = new ArrayList<>();
	private Composite container;
	
	private final BlockGenerationData data;
	
	private final Function<OOSEMBlock, Set<OOSEMFeature>> getSetToRefine;
	private final BiConsumer<BlockGenerationData, RefinementData.RefinementConfiguration> registerOperation;
	private final Predicate<OOSEMBlock> refinementTypeFiletringPredicate;

}
