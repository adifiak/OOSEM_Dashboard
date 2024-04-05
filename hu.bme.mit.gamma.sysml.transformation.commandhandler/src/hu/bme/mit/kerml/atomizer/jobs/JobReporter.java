package hu.bme.mit.kerml.atomizer.jobs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;

public class JobReporter {
	
	private final Logger logger;
	
	public JobReporter(Logger logger) {
		this.logger = logger;
	}
	
	public IStatus reportError(Exception exception, String prefix) {
		exception.printStackTrace();
		String message = prefix + exception.getMessage();
		logger.log(Level.SEVERE, message);
		return org.eclipse.core.runtime.Status.error(message, exception);
	}

	public void reportTaskStart(SubMonitor subMonitor, String message) {
		logger.log(Level.INFO, message);
		subMonitor.setTaskName(message);
	}

	public void reportTaskEnd(SubMonitor subMonitor, String message) {
		logger.log(Level.INFO, message);
	}
	
}