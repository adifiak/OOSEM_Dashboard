package hu.bme.mit.sysml.oosem.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.omg.sysml.lang.sysml.Type;

public class OOSEMViewComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if(o1 instanceof Type t1 && o2 instanceof Type t2) {
			return t1.effectiveName().compareTo(t2.effectiveName());
		} if(o1 instanceof OOSEMElementGroup g1 && o2 instanceof OOSEMElementGroup g2) {
			return g1.getName().compareTo(g2.getName());
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}
}
