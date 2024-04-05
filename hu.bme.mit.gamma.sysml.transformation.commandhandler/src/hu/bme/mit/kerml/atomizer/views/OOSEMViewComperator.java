package hu.bme.mit.kerml.atomizer.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.omg.sysml.lang.sysml.Type;

public class OOSEMViewComperator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if(o1 instanceof Type t1 && o2 instanceof Type t2) {
			return t1.getDeclaredName().compareTo(t2.getDeclaredName());
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}
}
