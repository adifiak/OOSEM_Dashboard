package hu.bme.mit.sysml.oosem.views.listeners;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Listener;

import hu.bme.mit.sysml.oosem.jobs.RefreshViewJob;
import hu.bme.mit.sysml.oosem.views.OOSEMProjectViewer;

public class RefreshButtonListener {
	public RefreshButtonListener(OOSEMProjectViewer view, Button refreshButton, Combo projectSelectionCombo) {
		this.view = view;
		this.refreshButton = refreshButton;
		this.projectSelectionCombo = projectSelectionCombo;
	}
	
	public Listener getListener() {
		return e -> {
			String selected = projectSelectionCombo.getItem(projectSelectionCombo.getSelectionIndex());
			view.setLoadedProject(selected);
			RefreshViewJob.createRevreshJob(view).schedule();
			refreshButton.setText("Refresh");
		};
	}
	
	private OOSEMProjectViewer view;
	private Button refreshButton;
	private Combo projectSelectionCombo;
}
