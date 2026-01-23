package hu.bme.mit.sysml.oosem.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.omg.sysml.lang.sysml.Element;

import hu.bme.mit.sysml.oosem.model.OOSEMModelLoader;
import hu.bme.mit.sysml.oosem.model.OOSEMProject;
import hu.bme.mit.sysml.oosem.model.OOSEMModelLoader.BlockFamilyStructures;
import hu.bme.mit.sysml.oosem.views.listeners.ContextMenuListener;
import hu.bme.mit.sysml.oosem.views.listeners.OOSEMTreeViewerListener;
import hu.bme.mit.sysml.oosem.views.listeners.ProjectBuildFinishedListener;
import hu.bme.mit.sysml.oosem.views.listeners.RefreshButtonListener;
import hu.bme.mit.sysml.oosem.views.listeners.ShowValidationResultsMouseTracctListener;
import hu.bme.mit.sysml.oosem.views.listeners.ContextMenuListener.*;;

public class OOSEMModelTreeView {
	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		createMenuBar(parent);

		viewBody = new Composite(parent, SWT.NONE);
		viewBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout(3, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		viewBody.setLayout(layout);
	}
	
	public void refresh(IProgressMonitor monitor) {
		if(projectBuildFinishedListener != null) projectBuildFinishedListener.unregister();
		
		Display.getDefault().syncExec(() -> {
			for (var child : viewBody.getChildren()) {
				child.dispose();
			}
			initViewBase(viewBody);
		});

		oosemProject = OOSEMModelLoader.LoadModelFromOOSEMProject(loadedProject, monitor);
		var specificationBlocks = oosemProject.getSpecifications();
		var designBlocks = oosemProject.getSpecificationsWithTheirDesigns();
		var integrationBlocks = oosemProject.getDesignsWithTheirIntegrations();

		Display.getDefault().syncExec(() -> {
			createTreeViewers(specificationBlocks, designBlocks, integrationBlocks);
			calculateScrolledCompositeSizes();
		});
		
		projectBuildFinishedListener = new ProjectBuildFinishedListener(this, oosemProject.getProject());
	}
	
	public void setLoadedProject(String lp) { loadedProject = lp; }
	
	public void setFocus() {/* treeViewer1.getControl().setFocus();*/}

	private void createMenuBar(Composite parent) {
		menuBar = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.spacing = 10;
		menuBar.setLayout(rowLayout);

		projectSelectionCombo = new Combo(menuBar, SWT.DROP_DOWN | SWT.READ_ONLY);
		refreshButton = new Button(menuBar, SWT.PUSH | SWT.FILL);
		
		projectSelectionCombo.add(comboPlaceholder);
		Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()).stream()
				.filter(p -> p.isOpen())
				.filter(p -> !Arrays.asList("oosem", "sysml", "kerml", "sysml.library").contains(p.getName()))
				.map(IProject::getName).forEach(p -> projectSelectionCombo.add(p));
		projectSelectionCombo.select(0);
		projectSelectionCombo.setLayoutData(new RowData(240, 30));

		refreshButton.setText("Load");
		refreshButton.setEnabled(false);
		refreshButton.addListener(SWT.Selection, new RefreshButtonListener(this, refreshButton, projectSelectionCombo).getListener());
		refreshButton.setLayoutData(new RowData(90, 30));

		projectSelectionCombo.addListener(SWT.Selection, event -> {
			int index = projectSelectionCombo.getSelectionIndex();
			String selected = projectSelectionCombo.getItem(index);
			refreshButton.setText(selected.equals(loadedProject) ? "Refresh" : "Load");
			refreshButton.setEnabled(!selected.equals(comboPlaceholder));
		});
	}

	private void createTreeViewers(Set<EObject> specificationBlocks, BlockFamilyStructures designBlocks, BlockFamilyStructures integrationBlocks) {
		createViewBlock(specificationsSC, specificationContainer, new GridData(SWT.FILL, SWT.FILL, true, true), "Specification Blocks:",
				specificationBlocks, Arrays.asList(MenuOptions::addShowInEditorToMenu, MenuOptions::addDesignWizardToMenu));
		createOOSEMViewWithSuperTypes(designsSC, designContainer, designBlocks, "Designs of ");
		createOOSEMViewWithSuperTypes(integrationsSC, integrationContainer, integrationBlocks, "Integrations of ");
	}

	private void calculateScrolledCompositeSizes() {
		specificationsSC.setContent(specificationContainer);
		specificationsSC.setMinSize(specificationContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		designsSC.setContent(designContainer);
		designsSC.setMinSize(designContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		integrationsSC.setContent(integrationContainer);
		integrationsSC.setMinSize(integrationContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		viewBody.layout(true, true);
	}

	private void initViewBase(Composite parent) {
		specificationsSC = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		designsSC = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		integrationsSC = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		specificationsSC.setExpandHorizontal(true);
		specificationsSC.setExpandVertical(true);
		designsSC.setExpandHorizontal(true);
		designsSC.setExpandVertical(true);
		integrationsSC.setExpandHorizontal(true);
		integrationsSC.setExpandVertical(true);

		specificationsSC.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		designsSC.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		integrationsSC.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		specificationContainer = new Composite(specificationsSC, SWT.NONE);
		designContainer = new Composite(designsSC, SWT.NONE);
		integrationContainer = new Composite(integrationsSC, SWT.NONE);

		specificationContainer.setLayout(new GridLayout(1, false));
		designContainer.setLayout(new GridLayout(1, false));
		integrationContainer.setLayout(new GridLayout(1, false));
	}

	private void createOOSEMViewWithSuperTypes(ScrolledComposite scrolledComposite, Composite container,
			BlockFamilyStructures blockFamilyStructures, String parentNamePrefix) {
		var parentsAndChilds = blockFamilyStructures.getBlocksWithFamily();
		var parentsOrdered = new ArrayList<>(parentsAndChilds.keySet());
		parentsOrdered.sort(new OOSEMModelComparator());

		for (var parentBlock : parentsOrdered) {
			String parentName = (parentBlock instanceof Element e) ? parentNamePrefix + e.getDeclaredName() + ":"
					: "NAME NOT FOUND:";
			var roots = parentsAndChilds.get(parentBlock);
			var layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
			List<addOptionToContextMenu> menuOptions = Arrays.asList(MenuOptions::addShowInEditorToMenu, MenuOptions::addIntegrationWizardToMenu);
			createViewBlock(scrolledComposite, container, layoutData, parentName, roots, menuOptions);
		}

		var orphanBlocks = blockFamilyStructures.getOrphanedBlocks();
		if (!orphanBlocks.isEmpty()) {
			var layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
			createViewBlock(scrolledComposite, container, layoutData, "❌ Orphan blocks:", orphanBlocks, Arrays.asList()); //Cannot build on orphan blocks.
		}
	}

	private void createViewBlock(ScrolledComposite scrolledComposite, Composite container, Object layoutData, String labelText,
			Set<EObject> roots, List<addOptionToContextMenu> contextMenuOptions) {
		Composite block = new Composite(container, SWT.NONE);
		block.setLayoutData(layoutData);
		block.setLayout(new GridLayout(1, false));

		if (labelText != null && !labelText.isEmpty()) {
			Label title = new Label(block, SWT.NONE);
			title.setText(labelText);
		}

		TreeViewer treeViewer = new TreeViewer(block, SWT.BORDER);
		var tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		treeViewer.addTreeListener(new OOSEMTreeViewerListener(scrolledComposite, container, treeViewer));

		treeViewer.setContentProvider(new OOSEMModelContentProvider());
		treeViewer.setLabelProvider(
				new OOSEMModelLabelProvider(oosemProject.getValidationErrors(), oosemProject.getValidationWarnings()));
		treeViewer.setComparator(new OOSEMViewComparator());
		treeViewer.setInput((Object[]) roots.toArray());

		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);

		menuMgr.addMenuListener(ContextMenuListener.getContextMenuListener(contextMenuOptions, treeViewer, oosemProject));
		tree.setMenu(menuMgr.createContextMenu(tree));
		tree.addMouseTrackListener(new ShowValidationResultsMouseTracctListener(tree, oosemProject));
	}
	
	private Composite menuBar;
	private Composite viewBody;

	private Combo projectSelectionCombo;
	private Button refreshButton;

	private ScrolledComposite specificationsSC;
	private ScrolledComposite designsSC;
	private ScrolledComposite integrationsSC;

	private Composite specificationContainer;
	private Composite designContainer;
	private Composite integrationContainer;

	private OOSEMProject oosemProject;
	private String loadedProject = "";
	
	private ProjectBuildFinishedListener projectBuildFinishedListener;
	
	private final String comboPlaceholder = "Choose project to visualize...";
}