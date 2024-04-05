package hu.bme.mit.kerml.atomizer.util;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.Element;
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
	        		if(type.getDeclaredName().equals("SpecificationBlock")) {
	        			spec = true;
	        		} else if(type.getDeclaredName().equals("DesignBlock")) {
	        			desi = true;
	        		} else if(type.getDeclaredName().equals("IntegrationBlock")) {
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
	
	public static List<EObject> getPossibleImplementationsOfSpecification(EObject o){
		if(o instanceof Type d && getOOSEMBlockType(d) == OOSEMBlockType.SPECIFICATION) {
			var defs = getOOSEMDefinitionsToUsage(d);
			if(defs.size() > 0) {
				var def = defs.get(0);
				if(def instanceof OccurrenceDefinition spec) {
					
					
					//TODO: Rendesen
					/* var speci = spec.spe getOwnedSpecialization();
					 * return speci.stream()
							.map(p-> {System.out.println("Step1: " + p);return p;})
							.map(p -> p.getSpecific())
							.map(p-> {System.out.println("Step2: " + p);return p;})
							.filter(OOSEMUtils::filterNamelessElements)
							.filter(p -> getOOSEMBlockType(p) == OOSEMBlockType.DESIGN || getOOSEMBlockType(p) == OOSEMBlockType.INTEGRATION)
							.map(p-> {System.out.println("Step3: " + p);return p;})
							.collect(Collectors.toList());*/
				}
			}
		}
		return null;
	}
	
	public static String getTextOfType(Type t) {
		String res = t.getDeclaredName();
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
		return !(e.getDeclaredName() == null || e.getDeclaredName().isEmpty());
	}
	
	public static List<Type> getOOSEMDefinitionsToUsage(Type od) {
		
		var type = getOOSEMBlockType(od);
		if(type != OOSEMBlockType.NONE) {
			return od.allSupertypes().stream().filter(OOSEMUtils::filterNamelessElements).filter(p -> ((p instanceof OccurrenceDefinition) ? getOOSEMBlockType(p) == type : false)).collect(Collectors.toList());
		} else {
			return null;
		}
	}
}
