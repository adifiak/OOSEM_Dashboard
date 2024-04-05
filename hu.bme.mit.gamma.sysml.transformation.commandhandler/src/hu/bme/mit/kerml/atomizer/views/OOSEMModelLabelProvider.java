package hu.bme.mit.kerml.atomizer.views;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.LabelProvider;
import org.omg.sysml.lang.sysml.OccurrenceUsage;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;

public class OOSEMModelLabelProvider extends LabelProvider {
	@SuppressWarnings("rawtypes")
	public String getText(Object element) {
		if (element instanceof Set) {
			return "System models";
		} else if (element instanceof Type t) {
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
				return res;
			}
		}
		return "Unknown label for: " + element.getClass();
	}
}
