package hu.bme.mit.sysml.oosem.views;

import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.LabelProvider;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMElement;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.UIUtils;

public class OOSEMModelLabelProvider extends LabelProvider {
	public OOSEMModelLabelProvider() {}
	
	public String getText(Object element) {
		if(element instanceof OOSEMElement oosemElement) 
			return generateOOSEMElementLabel(oosemElement);
		if(element instanceof OOSEMElementGroup g)
			return generateOOSEMElementGroupLabel(g);
		if(element instanceof EObject eobject)
			return generateEObjectLabel(eobject);
		
		return "Unknown label for: " + element.getClass();
	}

	private String generateOOSEMElementLabel(OOSEMElement oosemElement) {
		var res = oosemElement.getName();
		res = getOOSEMPhaseDecorator(oosemElement) + res;
		
		if(oosemElement instanceof OOSEMBlock block)
			res = res + getInPhaseSpecializationDecorator(block);
		if(oosemElement instanceof OOSEMFeature feature) {
			res = res + getTypeDecorator(feature);
			res = res + getredefinitionDecorator(feature);
		}
		if(!oosemElement.getValidationErrors().isEmpty()) {
			res = res + " ❌";
		} else if(!oosemElement.getValidationWarnings().isEmpty()) {
			res = res + " ⚠️";
		}
		
		return res;
	}

	private String generateOOSEMElementGroupLabel(OOSEMElementGroup g) {
		var res = g.getName();
		
		if(g.hasError()) {
			res = res + " ❌";
		} else if(g.hasWarning()) {
			res = res + " ⚠️";
		}
		
		return res;
	}

	private String generateEObjectLabel(EObject eobject) {
		if (eobject instanceof Type t) {
			var res = OOSEMUtils.getDecoratedName(t);
			if(t instanceof Feature f) {
				var redefines = FeatureUtil.getRedefinedFeaturesOf(f);
				redefines = redefines.stream().filter(OOSEMUtils::filterSpecification).collect(Collectors.toList());
				if(!redefines.isEmpty()) {
					res = res + " redefines " + OOSEMUtils.getTextOfType(redefines.get(0));
				}
			}
			
			var parentsFromSamePhase = OOSEMUtils.getParentsFromSamePhase(t);
			if(!parentsFromSamePhase.isEmpty()) {
				res = res + " specializes " + UIUtils.getFormatedBlockListText(parentsFromSamePhase);
			}
			
			return res;
		} else {
			return "Could not generaty label for model element: " + eobject.toString();
		}
	}
	
	private String getOOSEMPhaseDecorator(OOSEMElement element) {
		switch(element.getOOSEMBlockType()) {
			case SPECIFICATION:
				return "🟣 "; //🔴
			case DESIGN:
				return "🟩 ";
			case INTEGRATION:
				return "🔷 ";
			default:
				return "";
		}
	}
	
	private String getInPhaseSpecializationDecorator(OOSEMBlock block) {
		var res = "";
		boolean first = true;
		for(var p : block.getParents()) {
			if(p.getOOSEMBlockType() == block.getOOSEMBlockType()) {
				if(first) {
					res = res + " specializes ";
					first = false;
				} else {
					res = res + ", ";
				}
				res = res + p.getName();
			}
		}
		return res;
	}
	
	private String getTypeDecorator(OOSEMFeature feature) {
		if (feature.getType() == null) return "";
		
		return " : " + feature.getType().getName();
	}
	
	private String getredefinitionDecorator(OOSEMFeature feature) {
		if(feature.getRefinedFeature() == null) return "";
		
		return " redefines " + feature.getRefinedFeature().getName();
	}
}
