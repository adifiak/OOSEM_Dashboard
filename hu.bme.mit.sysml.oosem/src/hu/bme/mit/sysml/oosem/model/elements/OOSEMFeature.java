package hu.bme.mit.sysml.oosem.model.elements;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.OccurrenceUsage;
import org.omg.sysml.util.FeatureUtil;

public class OOSEMFeature extends OOSEMElement{

	public OOSEMFeature(EObject o, OOSEMBlock definedIn) {
		this(o, definedIn, null);
	}
	
	public OOSEMFeature(EObject o, OOSEMBlock definedIn, OOSEMFeature refinedFeature) {
		super(o);
		this.definedIn = definedIn;
		this.refinedFeature = refinedFeature;
		this.copiedFeature = null;
	}
	
	public OOSEMFeature(OOSEMFeature copied) {
		super(copied.getObject());
		this.definedIn = copied.definedIn;
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
	
	public String getTypeName() {
		if (object instanceof OccurrenceUsage o) {
			var types = FeatureUtil.getAllTypesOf(o);
			if (types.size() > 0) {
				var type = types.get(0);
				var typeName = type.getDeclaredName();
				if (typeName != null && !typeName.isEmpty() && !typeName.equals("Part"))
					return typeName;
			}
		}
		return "";
	}
	
	private final OOSEMFeature copiedFeature;
	private final OOSEMFeature refinedFeature;
	private final OOSEMBlock definedIn;
}
