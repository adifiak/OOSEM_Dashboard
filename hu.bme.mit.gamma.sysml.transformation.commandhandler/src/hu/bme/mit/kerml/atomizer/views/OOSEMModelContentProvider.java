package hu.bme.mit.kerml.atomizer.views;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Namespace;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;

public class OOSEMModelContentProvider implements ITreeContentProvider {
	@SuppressWarnings("rawtypes")
	public boolean hasChildren(Object element) {
		if (element instanceof Set s) {
			return !s.isEmpty();
		} else if (element instanceof Namespace n) {
			return !n.getOwnedMember().stream().filter(OOSEMUtils::filterNamelessElements).collect(Collectors.toList()).isEmpty();
		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Set s) {
			return s.toArray();
		} else if (parentElement instanceof Namespace n) {
			return n.getOwnedMember().stream().filter(OOSEMUtils::filterNamelessElements).collect(Collectors.toList()).toArray();
		} else {
			return new Object[0];
		}
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		} else {
			return new Object[0];
		}
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
