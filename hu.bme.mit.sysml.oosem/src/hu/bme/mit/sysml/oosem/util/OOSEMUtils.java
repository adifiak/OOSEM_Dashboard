package hu.bme.mit.sysml.oosem.util;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.OccurrenceUsage;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

public class OOSEMUtils {
	public enum OOSEMBlockType {
		NONE, SPECIFICATION, DESIGN, INTEGRATION
	}

	public static OOSEMBlockType getOOSEMBlockType(EObject o) {
		if(o instanceof Type t) {
		var types = t.allSupertypes();
	        	
	        	boolean spec = false, desi = false, inte = false;
	        	
	        	for (var type : types) {
	        		if(type.effectiveName().equals("SpecificationBlock")) {
	        			spec = true;
	        		} else if(type.effectiveName().equals("DesignBlock")) {
	        			desi = true;
	        		} else if(type.effectiveName().equals("IntegrationBlock")) {
	        			inte = true;
	        		}
	        	}
	        	
	        	if(inte){
	        		return OOSEMBlockType.INTEGRATION;
	        	} else if(desi){
	        		return OOSEMBlockType.DESIGN;
	        	} else if(spec){
	        		return OOSEMBlockType.SPECIFICATION;
	        	}
		}
		return OOSEMBlockType.NONE;
	}
	
	public static List<EObject> getSpecificationsInDesignBlock(EObject o) {
		if(o instanceof OccurrenceDefinition d && getOOSEMBlockType(d) == OOSEMBlockType.DESIGN) {
			return d.getOwnedMember().stream().filter(b -> getOOSEMBlockType(b) == OOSEMBlockType.SPECIFICATION).collect(Collectors.toList());
		} else {
			return null;
		}
	}
	
	public static String getTextOfType(Type t) {
		String res = t.effectiveName();
		if (t instanceof OccurrenceUsage o) {
			var types = FeatureUtil.getAllTypesOf(o);
			if (types.size() > 0) {
				var type = types.get(0);
				var typeName = type.getDeclaredName();
				if (typeName != null && !typeName.isEmpty() && !typeName.equals("Part"))
					return res + " : " + typeName;
			}
		}
		return res;
	}
	
	public static boolean filterNamelessElements(Element e) {
		if(!(e.effectiveName() == null || e.effectiveName().isEmpty())) {
			return true;
		} else if(e instanceof Feature f){
			return !FeatureUtil.getRedefinedFeaturesOf(f).isEmpty();
		}
		return false;
		
	}
	
	public static List<Type> getOOSEMDefinitionsToUsage(Type od) {
		
		var type = getOOSEMBlockType(od);
		if(type != OOSEMBlockType.NONE) {
			return od.allSupertypes().stream().filter(OOSEMUtils::filterNamelessElements).filter(p -> ((p instanceof OccurrenceDefinition) ? getOOSEMBlockType(p) == type : false)).collect(Collectors.toList());
		} else {
			return null;
		}
	}
	
	public static String getDecoratedName(Type t) {
		var res = OOSEMUtils.getTextOfType(t);
		if (res != null && !res.isEmpty()) {
			var blockType = OOSEMUtils.getOOSEMBlockType(t);
			switch(blockType) {
				case SPECIFICATION:
					res = "🟣 " + res;//🔴
					break;
				case DESIGN:
					res = "🟢 " + res;
					break;
				case INTEGRATION:
					res = "🔵 " + res;
					break;
				default:
			}
		} else {
			res = "UNKNOWN";
		}
		return res;
	}
	
	public static boolean filterSpecification(EObject o) {
		return getOOSEMBlockType(o) == OOSEMBlockType.SPECIFICATION;
	}
	
	public static boolean filterDesignsAndInegrations(EObject o) {
		return getOOSEMBlockType(o) == OOSEMBlockType.DESIGN || getOOSEMBlockType(o) == OOSEMBlockType.INTEGRATION;
	}
}
