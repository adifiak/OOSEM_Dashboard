package hu.bme.mit.kerml.atomizer;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;

import hu.bme.mit.kerml.atomizer.jobs.FileScannerJob;
import hu.bme.mit.kerml.atomizer.jobs.JobReporter;

public class CommandHandler extends AbstractHandler {
	// Set this to true if some of the model has not been loaded properly (will be very slow)
	public static final boolean RESOLVE_ALL = false;
	
	protected final Logger logger = Logger.getLogger("GammaLogger");

	@Override
	public Object execute(ExecutionEvent event) {
		Job job = new FileScannerJob("Scanning SysML files for fun...",
				new JobReporter(logger), event);
		job.setUser(true);
		job.setPriority(Job.LONG);
		job.schedule();
		return null;
	}
	
}
