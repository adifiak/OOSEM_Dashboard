package hu.bme.mit.sysml.oosem.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import hu.bme.mit.sysml.oosem.views.OOSEMProjectViewer;

public class RefreshViewJob {
	public static Job createRevreshJob(OOSEMProjectViewer view) {
		Job job = new Job("Refreshing data") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Loading data...", IProgressMonitor.UNKNOWN);

				try {
					view.refresh(monitor);
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
		return job;
	}
}
