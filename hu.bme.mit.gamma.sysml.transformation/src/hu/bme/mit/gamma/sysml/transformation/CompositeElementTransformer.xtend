package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.property.util.PropertyUtil

abstract class CompositeElementTransformer extends AbstractTransformer {
	
	protected final CompositeTraceability traceability
	
	protected final extension PropertyUtil propertyUtil = PropertyUtil.INSTANCE
	
	new(CompositeTraceability traceability) {
		this.traceability = traceability
	}
	
}