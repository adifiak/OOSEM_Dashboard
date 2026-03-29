package hu.bme.mit.sysml.oosem;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;

import hu.bme.mit.sysml.oosem.jobs.FileScannerJob;
import hu.bme.mit.sysml.oosem.jobs.JobReporter;

public class CommandHandler extends AbstractHandler {
	// Set this to true if some of the model has not been loaded properly (will be very slow)
	public static final boolean RESOLVE_ALL = false;
	
	protected final Logger logger = Logger.getLogger("OOSEMAsistantLogger");

	@Override
	public Object execute(ExecutionEvent event) {
		Job job = new FileScannerJob("Scanning SysML files...",
				new JobReporter(logger), event);
		job.setUser(true);
		job.setPriority(Job.LONG);
		job.schedule();
		return null;
	}
	
}
