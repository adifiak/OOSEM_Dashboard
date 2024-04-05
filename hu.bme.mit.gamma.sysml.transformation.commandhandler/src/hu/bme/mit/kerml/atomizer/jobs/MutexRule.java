package hu.bme.mit.kerml.atomizer.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class MutexRule implements ISchedulingRule {
	
	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}

	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}
	
}