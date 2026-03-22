package hu.bme.mit.sysml.oosem.views;

import java.util.Set;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMElement;

public class OOSEMElementGroup {
	public OOSEMElementGroup(String name, Set<? extends OOSEMElement> content) {
		this.name = name;
		this.content = content;
		
		for(var c : content) {
			if(!hasError && !c.getValidationErrors().isEmpty()) {
				hasError = true;
			}
			if(!hasWarning && !c.getValidationWarnings().isEmpty()) {
				hasWarning = true;
			}
		}
	}
	
	public String getName() {return name;}
	public Set<? extends OOSEMElement> getContent() {return content;}
	public boolean hasError() {return hasError;}
	public boolean hasWarning() {return hasWarning;}
	
	private final String name;
	private final Set<? extends OOSEMElement> content;
	private boolean hasError = false;
	private boolean hasWarning = false;
}
