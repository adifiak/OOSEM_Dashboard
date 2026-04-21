package hu.bme.mit.sysml.oosem.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import hu.bme.mit.sysml.oosem.model.project.implementations.OOSEMProjectImpl;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.ProjectUtils;
import hu.bme.mit.sysml.oosem.views.listeners.ProjectBuildFinishedListener;
import hu.bme.mit.sysml.oosem.views.listeners.RefreshButtonListener;

public class OOSEMModelTreeView {
	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		createMenuBar(parent);

		viewFolder = new TabFolder(parent, SWT.NONE);
		viewFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		views.add(new OOSEMTreeView(viewFolder));
	}
	
	public void refresh(IProgressMonitor monitor) {
		if(projectBuildFinishedListener != null) projectBuildFinishedListener.unregister();
		
		var project = ProjectUtils.getProjectWithName(loadedProject);
		oosemProject = new OOSEMProjectImpl(project, monitor);
				
		for(var view : views) {
			view.setProject(oosemProject);
		}
		
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
				.filter(p -> !Arrays.asList("oosem", "platform", "sysml", "kerml", "sysml.library").contains(p.getName()))
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
	
	private Composite menuBar;
	private TabFolder viewFolder;

	private Combo projectSelectionCombo;
	private Button refreshButton;
	private final String comboPlaceholder = "Choose project to visualize...";

	private OOSEMProject oosemProject;
	private String loadedProject = "";
	
	private ProjectBuildFinishedListener projectBuildFinishedListener;
	
	private List<OOSEMView> views = new ArrayList<>();
	
	
}