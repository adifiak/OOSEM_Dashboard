package hu.bme.mit.kerml.atomizer.views;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.LabelProvider;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.OccurrenceUsage;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;

public class OOSEMModelLabelProvider extends LabelProvider {
	OOSEMModelLabelProvider(Map<EObject, List<String>> validationErrors, Map<EObject, List<String>> validationWarnings) {
		this.validationErrors = validationErrors;
		this.validationWarnings = validationWarnings;
	}
	
	@SuppressWarnings("rawtypes")
	public String getText(Object element) {
		if (element instanceof Set) {
			return "System models";
		} else if (element instanceof Type t) {
			var res = OOSEMUtils.getDecoratedName(t);
			if(t instanceof Feature f) {
				var redefines = FeatureUtil.getRedefinedFeaturesOf(f);
				redefines = redefines.stream().filter(OOSEMUtils::filterSpecification).collect(Collectors.toList());
				if(!redefines.isEmpty()) {
					res = res + " redefines " + OOSEMUtils.getTextOfType(redefines.get(0));
				}
			}
			if(validationErrors.get(t) != null) {
				res = res + " ❌";
			} else if(validationWarnings.get(t) != null) {
				res = res + " ⚠️";
			}
			return res;
		}
		return "Unknown label for: " + element.getClass();
	}
	
	private final Map<EObject, List<String>> validationErrors;
	private final Map<EObject, List<String>> validationWarnings;
}
