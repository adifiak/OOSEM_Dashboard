package hu.bme.mit.sysml.oosem.model.elements;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

public class OOSEMFeature extends OOSEMElement{

	public OOSEMFeature(EObject o, OOSEMBlock definedIn, OOSEMBlock type) {
		this(o, definedIn, type, null);
	}
	
	public OOSEMFeature(EObject o, OOSEMBlock definedIn, OOSEMBlock type, OOSEMFeature refinedFeature) {
		super(o);
		this.definedIn = definedIn;
		this.type = type;
		this.refinedFeature = refinedFeature;
		this.copiedFeature = null;
	}
	
	public OOSEMFeature(OOSEMFeature copied) {
		super(copied.getObject());
		this.definedIn = copied.definedIn;
		this.type = copied.type;
		this.refinedFeature = copied.refinedFeature;
		if(copied.copiedFeature != null ) {
			this.copiedFeature = copied.copiedFeature;
		} else {
			this.copiedFeature = copied;
		}
	}
	
	public OOSEMFeature getRefinedFeature() {
		return refinedFeature;
	}
	
	public OOSEMBlock getDefinedIn() {
		return definedIn;
	}
	
	public OOSEMFeature getCopiedFeature() {
		return copiedFeature;
	}
	
	public boolean isInherited() {
		return copiedFeature != null;
	}
	
	public OOSEMBlock getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(copiedFeature, definedIn, refinedFeature, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OOSEMFeature other = (OOSEMFeature) obj;
		return Objects.equals(copiedFeature, other.copiedFeature) && Objects.equals(definedIn, other.definedIn)
				&& Objects.equals(refinedFeature, other.refinedFeature) && Objects.equals(type, other.type);
	}



	private final OOSEMFeature copiedFeature;
	private final OOSEMFeature refinedFeature;
	private final OOSEMBlock definedIn;
	private final OOSEMBlock type;
}
