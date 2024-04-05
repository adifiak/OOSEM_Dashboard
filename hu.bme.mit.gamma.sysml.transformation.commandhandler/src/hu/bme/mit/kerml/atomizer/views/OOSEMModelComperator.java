package hu.bme.mit.kerml.atomizer.views;

import java.util.Comparator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.omg.sysml.lang.sysml.Type;

public class OOSEMModelComperator implements Comparator<EObject>{

	@Override
	public int compare(EObject o1, EObject o2) {
		if(o1 instanceof Type t1 && o2 instanceof Type t2) {
			return t1.getDeclaredName().compareTo(t2.getDeclaredName());
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}}