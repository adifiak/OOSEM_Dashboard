package hu.bme.mit.sysml.oosem.views;

import java.util.Comparator;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;

public class OOSEMModelComparator implements Comparator<OOSEMBlock>{

	@Override
	public int compare(OOSEMBlock o1, OOSEMBlock o2) {
		if(o1.getObject() instanceof Type t1 && o2.getObject() instanceof Type t2) {
			return t1.getDeclaredName().compareTo(t2.getDeclaredName());
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}}