package hu.bme.mit.sysml.oosem.views.listeners;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Listener;

import hu.bme.mit.sysml.oosem.views.OOSEMModelTreeView;

public class RefreshButtonListener {
	public RefreshButtonListener(OOSEMModelTreeView view, Button refreshButton, Combo projectSelectionCombo) {
		this.view = view;
		this.refreshButton = refreshButton;
		this.projectSelectionCombo = projectSelectionCombo;
	}
	
	public Listener getListener() {
		return e -> {
			String selected = projectSelectionCombo.getItem(projectSelectionCombo.getSelectionIndex());
			Job job = new Job("Refreshing data") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Loading data...", IProgressMonitor.UNKNOWN);

					try {
						view.refresh(selected);
						return Status.OK_STATUS; // success
					} catch (Exception e) {
						return new Status(IStatus.ERROR, "OOSEMAssistant", "Something failed", e);
					} finally {
						monitor.done();
					}
				}
			};
			job.setUser(true);
			job.setPriority(Job.LONG);
			job.setSystem(false);
			job.schedule();
			refreshButton.setText("Refresh");
			view.setLoadedProject(selected);
		};
	}
	
	private OOSEMModelTreeView view;
	private Button refreshButton;
	private Combo projectSelectionCombo;
}
