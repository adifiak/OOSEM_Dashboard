package hu.bme.mit.kerml.atomizer.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.kerml.atomizer.model.OOSEMProject;
import hu.bme.mit.kerml.atomizer.util.OOSEMModelLoader;
import hu.bme.mit.kerml.atomizer.util.OOSEMModelLoader.BlockFamilyStructures;
import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;
import hu.bme.mit.kerml.atomizer.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.kerml.atomizer.util.OpenInFileUtils;
import hu.bme.mit.kerml.atomizer.wizards.blockGenerators.DesignToIntegrationWizard;
import hu.bme.mit.kerml.atomizer.wizards.blockGenerators.SpecificationToDesignWizard;

public class OOSEMModelTreeView {
	private final String comboPlaceholder = "Choose project to visualize...";

	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		menuBar = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.spacing = 10;
		menuBar.setLayout(rowLayout);

		// Create the Combo (drop-down list)
		projectSelectionCombo = new Combo(menuBar, SWT.DROP_DOWN | SWT.READ_ONLY);
		projectSelectionCombo.add(comboPlaceholder);
		Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()).stream().filter(p -> p.isOpen())
				.map(IProject::getName).forEach(p -> projectSelectionCombo.add(p));
		projectSelectionCombo.select(0);

		// Add a listener to react to selection changes
		/*
		 * projectSelectionCombo.addListener(SWT.Selection, event -> { int index =
		 * projectSelectionCombo.getSelectionIndex(); String selected =
		 * projectSelectionCombo.getItem(index); System.out.println("Selected: " +
		 * selected); // TODO: do something when the selection changes });
		 */
		projectSelectionCombo.setLayoutData(new RowData(240, 30));

		Button refreshButton = new Button(menuBar, SWT.PUSH | SWT.FILL);
		refreshButton.setText("Refresh");
		refreshButton.addListener(SWT.Selection, e -> {
			String selected = projectSelectionCombo.getItem(projectSelectionCombo.getSelectionIndex());
			if (!selected.equals(comboPlaceholder)) {
				Job job = new Job("Refreshing data") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Loading data...", IProgressMonitor.UNKNOWN);

						try {
							refresh(selected);
							return Status.OK_STATUS; // success
						} catch (Exception e) {
							return new Status(IStatus.ERROR, "OOSEMTreeViewer", "Something failed", e);
						} finally {
							monitor.done();
						}
					}
				};
				job.setUser(true);
				job.setPriority(Job.LONG);
				job.setSystem(false);
				job.schedule();
			} else {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error",
						"Please select a project to visualize.");
			}
		});
		refreshButton.setLayoutData(new RowData(90, 30));

		viewBody = new Composite(parent, SWT.NONE);
		viewBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout(3, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		viewBody.setLayout(layout);
	}

	public void refresh(String projectName) {
		Display.getDefault().syncExec(() -> {
			for (var child : viewBody.getChildren()) {
				child.dispose();
			}
			initViewBase(viewBody);
		});

		oosemProject = OOSEMModelLoader.LoadModelFromOOSEMProject(projectName);
		var specificationBlocks = oosemProject.getSpecifications();
		var designBlocks = oosemProject.getSpecificationsWithTheirDesigns();
		var integrationBlocks = oosemProject.getDesignsWithTheirIntegrations();

		Display.getDefault().syncExec(() -> {
			createTreeViewers(specificationBlocks, designBlocks, integrationBlocks);
			calculateScrolledCompositeSizes();
		});
	}

	private Composite menuBar;
	private Composite viewBody;

	private Combo projectSelectionCombo;

	private ScrolledComposite specificationsSC;
	private ScrolledComposite designsSC;
	private ScrolledComposite integrationsSC;

	private Composite specificationContainer;
	private Composite designContainer;
	private Composite integrationContainer;

	private OOSEMProject oosemProject;

	private void createTreeViewers(Set<EObject> specificationBlocks, BlockFamilyStructures designBlocks,
			BlockFamilyStructures integrationBlocks) {
		// Specifications Tree viewer
		createSimpleOOSEMBlockView(specificationsSC, specificationContainer, "Specification Blocks",
				specificationBlocks, true);
		// Designs view
		createOOSEMViewWithSuperTypes(designsSC, designContainer, designBlocks, "Designs of ");
		// Integrations view
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

		parentsOrdered.sort(new OOSEMModelComperator());

		for (var parentBlock : parentsOrdered) {
			String parentName = (parentBlock instanceof Element e) ? parentNamePrefix + e.getDeclaredName()
					: "NAME NOT FOUND";
			var roots = parentsAndChilds.get(parentBlock);
			createViewBlock(scrolledComposite, container, parentName, roots, true);
		}

		var orphanBlocks = blockFamilyStructures.getOrphanedBlocks();
		if (!orphanBlocks.isEmpty()) {
			createViewBlock(scrolledComposite, container, "❌ Orphan blocks", orphanBlocks, true);
		}

	}

	private void createViewBlock(ScrolledComposite scrolledComposite, Composite container, String parentName,
			Set<EObject> roots, boolean addIntegrationWizard) {
		Composite block = new Composite(container, SWT.NONE);
		block.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		block.setLayout(new GridLayout(1, false));

		Label title = new Label(block, SWT.NONE);
		title.setText(parentName + ":");

		TreeViewer treeViewer = new TreeViewer(block, SWT.BORDER);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		treeViewer.addTreeListener(new OOSEMTreeViewerListener(scrolledComposite, container, treeViewer));

		treeViewer.setContentProvider(new OOSEMModelContentProvider());
		treeViewer.setLabelProvider(new OOSEMModelLabelProvider(oosemProject.getValidationErrors()));
		treeViewer.setComparator(new OOSEMViewComperator());
		treeViewer.setInput((Object[]) roots.toArray());

		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);

		// Dynamically populate menu each time it is shown
		menuMgr.addMenuListener(manager -> {
			ITreeSelection selection = treeViewer.getStructuredSelection();
			Object obj = selection.getFirstElement();
			if (obj == null)
				return;

			if (obj instanceof EObject eobj) {
				manager.add(new Action("Open in Editor") {
					public void run() {
						OpenInFileUtils.openEditorForEObject(eobj);
					}
				});
			}
			
			if (addIntegrationWizard) {
				if (obj instanceof OccurrenceDefinition o && OOSEMUtils.getOOSEMBlockType(o) == OOSEMBlockType.DESIGN) {
					manager.add(new Action("Generate Integration Block") {
						public void run() {
							WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(),
									new DesignToIntegrationWizard(o, oosemProject));
							dialog.open();
						}
					});
				}
			}
		});

		var tree = treeViewer.getTree();

		treeViewer.getTree().setMenu(menuMgr.createContextMenu(treeViewer.getTree()));

		tree.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseHover(MouseEvent e) {

				TreeItem item = tree.getItem(new org.eclipse.swt.graphics.Point(e.x, e.y));
				var data = item.getData();
				if (data != null && data instanceof Type t) {
					var errors = oosemProject.getValidationErrors().get(data);
					if (item != null && errors != null) {
						var toolTip = "Errors for " + t.getName() + ":";
						for (var err : errors) {
							toolTip = toolTip + "\n - " + err;
						}
						tree.setToolTipText(toolTip);
					} else {
						tree.setToolTipText(null);
					}
				}
			}
		});
	}

	private void createSimpleOOSEMBlockView(ScrolledComposite scrolledComposite, Composite container, String labelText,
			Set<EObject> blocks, boolean addDesignWizard) {
		Composite block = new Composite(container, SWT.NONE);
		block.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		block.setLayout(new GridLayout(1, false));

		if (labelText != null && !labelText.isEmpty()) {
			Label title = new Label(block, SWT.NONE);
			title.setText(labelText);
		}

		TreeViewer treeViewer = new TreeViewer(block, SWT.BORDER);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		treeViewer.addTreeListener(new OOSEMTreeViewerListener(scrolledComposite, container, treeViewer));

		treeViewer.setContentProvider(new OOSEMModelContentProvider());
		treeViewer.setLabelProvider(new OOSEMModelLabelProvider(oosemProject.getValidationErrors()));
		treeViewer.setComparator(new OOSEMViewComperator());
		treeViewer.setInput((Object[]) blocks.toArray());

		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);

		// Dynamically populate menu each time it is shown
		menuMgr.addMenuListener(manager -> {
			ITreeSelection selection = treeViewer.getStructuredSelection();
			Object obj = selection.getFirstElement();
			if (obj == null)
				return;

			if (obj instanceof EObject eobj) {
				manager.add(new Action("Open in Editor") {
					public void run() {
						OpenInFileUtils.openEditorForEObject(eobj);
					}
				});
			}

			if (addDesignWizard) {
				if (obj instanceof OccurrenceDefinition o
						&& OOSEMUtils.getOOSEMBlockType(o) == OOSEMBlockType.SPECIFICATION) {
					manager.add(new Action("Generate Design Block") {
						public void run() {
							WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(),
									new SpecificationToDesignWizard(o));
							dialog.open();
						}
					});
				}
			}
		});

		// Attach menu to the tree
		treeViewer.getTree().setMenu(menuMgr.createContextMenu(treeViewer.getTree()));

	}

	// @Override
	public void setFocus() {
		// treeViewer1.getControl().setFocus();
	}

	private class OOSEMTreeViewerListener implements ITreeViewerListener {
		public OOSEMTreeViewerListener(ScrolledComposite scrolledComposite, Composite container,
				TreeViewer treeViewer) {
			this.scrolledComposite = scrolledComposite;
			this.container = container;
			this.treeViewer = treeViewer;
		}

		@Override
		public void treeExpanded(TreeExpansionEvent e) {
			asyncRelayout();
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent e) {
			asyncRelayout();
		}

		private void asyncRelayout() {
			treeViewer.getTree().getDisplay().asyncExec(() -> {
				if (!treeViewer.getTree().isDisposed()) {
					treeViewer.getTree().getParent().layout(true, true);
				}
			});
			scrolledComposite.getDisplay().asyncExec(() -> {
				if (!scrolledComposite.getDisplay().isDisposed()) {
					scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					scrolledComposite.layout(true, true);
				}
			});
		}

		private ScrolledComposite scrolledComposite;
		private Composite container;
		private TreeViewer treeViewer;
	}
}
