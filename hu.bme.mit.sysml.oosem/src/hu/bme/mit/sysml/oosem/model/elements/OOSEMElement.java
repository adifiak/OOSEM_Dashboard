package hu.bme.mit.sysml.oosem.model.elements;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.Element;

import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

public abstract class OOSEMElement {
	public OOSEMElement(EObject o) {
		object = o;
		blockType = OOSEMUtils.getOOSEMBlockType(object);
	}
	
	public EObject getObject() {return object;}
	
	public OOSEMBlockType getOOSEMBlockType() {return blockType;}
	
	public Set<String> getValidationErrors() {
		return validationErrors;
	}
	
	public Set<String> getValidationWarnings() {
		return validationWarnings;
	}
	
	public boolean passedValidation() {
		return validationErrors.isEmpty() && validationWarnings.isEmpty();
	}
	
	public String getName() {
		if(object instanceof Element e) {
			return e.effectiveName();
		}
		return "";
	}
	
	public void registerError(String msg) {
		validationErrors.add(msg);
	}
	
	public void registerWarning(String msg) {
		validationWarnings.add(msg);
	}
	
	private OOSEMBlockType blockType;
	protected final EObject object;
	
	private final Set<String> validationErrors = new HashSet<String>();
	private final Set<String> validationWarnings = new HashSet<String>();
}
