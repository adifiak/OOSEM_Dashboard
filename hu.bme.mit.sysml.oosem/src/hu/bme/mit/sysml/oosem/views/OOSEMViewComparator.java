package hu.bme.mit.sysml.oosem.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMElement;

public class OOSEMViewComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if(o1 instanceof OOSEMElement t1 && o2 instanceof OOSEMElement t2) {
			return t1.getName().compareTo(t2.getName());
		} if(o1 instanceof OOSEMElementGroup g1 && o2 instanceof OOSEMElementGroup g2) {
			return g1.getName().compareTo(g2.getName());
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}
}
