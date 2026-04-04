package hu.bme.mit.sysml.oosem.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.implementations.BlockFamilyStructures;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.UIUtils;
import hu.bme.mit.sysml.oosem.views.listeners.ContextMenuListener;
import hu.bme.mit.sysml.oosem.views.listeners.OOSEMTreeViewerListener;
import hu.bme.mit.sysml.oosem.views.listeners.ShowValidationResultsMouseTracctListener;
import hu.bme.mit.sysml.oosem.views.listeners.ContextMenuListener.MenuOptions;
import hu.bme.mit.sysml.oosem.views.listeners.ContextMenuListener.addOptionToContextMenu;

public class OOSEMTreeView extends OOSEMView {

	public OOSEMTreeView(TabFolder parent) {
		super(parent);
		tab.setText("OOSEMTreeView");
	}

	@Override
	public void refresh() {
		if(project == null) return;

		clearView();
		
		Display.getDefault().syncExec(() -> {
			specificationsSC = createConfiguredScrolledComposite(container);
			specificationContainer = createScrolledCompositeContainer(specificationsSC);
			
			designsSC = createConfiguredScrolledComposite(container);
			designContainer = createScrolledCompositeContainer(designsSC);
			
			integrationsSC = createConfiguredScrolledComposite(container);
			integrationContainer = createScrolledCompositeContainer(integrationsSC);
			
			createTreeViewers(project.getSpecifications(), project.getSpecificationsWithTheirDesigns(), project.getDesignsWithTheirIntegrations());
			calculateScrolledCompositeSizes();
		});
	}
	
	private ScrolledComposite specificationsSC;
	private ScrolledComposite designsSC;
	private ScrolledComposite integrationsSC;

	private Composite specificationContainer;
	private Composite designContainer;
	private Composite integrationContainer;
	
	private void createTreeViewers(Set<OOSEMBlock> specificationBlocks, BlockFamilyStructures designBlocks, BlockFamilyStructures integrationBlocks) {
		createViewBlock(specificationsSC, specificationContainer, new GridData(SWT.FILL, SWT.FILL, true, true), "Specification Blocks:",
				specificationBlocks, Arrays.asList(MenuOptions::addShowInEditorToMenu, MenuOptions::addDesignWizardToMenu));
		createOOSEMViewWithSuperTypes(designsSC, designContainer, designBlocks, "Designs of ");
		createOOSEMViewWithSuperTypes(integrationsSC, integrationContainer, integrationBlocks, "Integrations of ");
	}

	private void calculateScrolledCompositeSizes() {
		specificationsSC.setMinSize(specificationContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		designsSC.setMinSize(designContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		integrationsSC.setMinSize(integrationContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		container.layout(true, true);
	}

	private ScrolledComposite createConfiguredScrolledComposite(Composite parent) {
		var sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return sc;
	}

	private Composite createScrolledCompositeContainer(ScrolledComposite sc) {
		var cont = new Composite(sc, SWT.NONE);
		cont.setLayout(new GridLayout(1, false));
		sc.setContent(cont);
		return cont;
	}
	
	private void createOOSEMViewWithSuperTypes(ScrolledComposite scrolledComposite, Composite container,
			BlockFamilyStructures blockFamilyStructures, String parentNamePrefix) {
		var parentsAndChilds = blockFamilyStructures.getBlocksWithFamily();
		var parentsOrdered = new ArrayList<>(parentsAndChilds.keySet());
		parentsOrdered.sort(new OOSEMModelComparator());

		for (var parentBlock : parentsOrdered) {
			String parentName = generateViewBlockTitleText(parentNamePrefix, parentBlock);
			var roots = parentsAndChilds.get(parentBlock);
			var layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
			List<addOptionToContextMenu> menuOptions = Arrays.asList(MenuOptions::addShowInEditorToMenu, MenuOptions::addIntegrationWizardToMenu);
			createViewBlock(scrolledComposite, container, layoutData, parentName, roots, menuOptions);
		}

		var orphanBlocks = blockFamilyStructures.getOrphanedBlocks();
		if (!orphanBlocks.isEmpty()) {
			var layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
			List<addOptionToContextMenu> menuOptions = Arrays.asList(MenuOptions::addShowInEditorToMenu, MenuOptions::addIntegrationWizardToMenu);
			createViewBlock(scrolledComposite, container, layoutData, "Orphan blocks:", orphanBlocks, menuOptions);
		}
	}

	private String generateViewBlockTitleText(String parentNamePrefix, OOSEMBlock parentBlock) {
		String parentName ="NAME NOT FOUND:";
		if(parentBlock.getObject() instanceof Type e) {
			parentName = parentNamePrefix + e.getDeclaredName();
			
			var parentsFromSamePhase = OOSEMUtils.getParentsFromSamePhase(e);
			if(!parentsFromSamePhase.isEmpty()) {
				parentName = parentName + " specializes " + UIUtils.getFormatedBlockListText(parentsFromSamePhase);
			}
			
			parentName = parentName + ":";
		}
		return parentName;
	}

	private void createViewBlock(ScrolledComposite scrolledComposite, Composite container, Object layoutData, String labelText,
			Set<OOSEMBlock> roots, List<addOptionToContextMenu> contextMenuOptions) {
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
		treeViewer.setLabelProvider(new OOSEMModelLabelProvider());
		treeViewer.setComparator(new OOSEMViewComparator());
		treeViewer.setInput((Object[]) roots.toArray());

		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);

		menuMgr.addMenuListener(ContextMenuListener.getContextMenuListener(contextMenuOptions, treeViewer, project));
		tree.setMenu(menuMgr.createContextMenu(tree));
		tree.addMouseTrackListener(new ShowValidationResultsMouseTracctListener(tree, project));
	}
}
