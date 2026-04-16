package hu.bme.mit.sysml.oosem.views;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.omg.sysml.lang.sysml.Namespace;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;

public class OOSEMModelContentProvider implements ITreeContentProvider {
	public boolean hasChildren(Object element) {
		if (element instanceof Set s) {
			return !s.isEmpty();
		} else if (element instanceof OOSEMBlock b) {
			return !(b.getProperties().isEmpty() && b.getSubsystems().isEmpty() && b.getUntrackedFeatures().isEmpty());
		} else if (element instanceof OOSEMFeature) {
			return false;
		} if (element instanceof OOSEMElementGroup g) {
			return !g.getContent().isEmpty();
		} if (element instanceof Namespace n) {
			return !n.getOwnedMember().stream().filter(OOSEMUtils::filterNamelessElements).collect(Collectors.toList()).isEmpty();
		} else {
			return false;
		}
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Set s) {
			return s.toArray();
		} else if (parentElement instanceof OOSEMBlock b) {
			Object[] childs = Stream.of(
			        b.getProperties().isEmpty() ? null : new OOSEMElementGroup("Properties", b.getProperties()),
			        b.getSubsystems().isEmpty() ? null : new OOSEMElementGroup("Subsystems", b.getSubsystems()),
			        b.getUntrackedFeatures().isEmpty() ? null : new OOSEMElementGroup("UntrackedFeatures", b.getUntrackedFeatures())
			    )
			    .filter(Objects::nonNull)
			    .toArray();
			return childs;
		} else if (parentElement instanceof OOSEMFeature) {
			return new Object[0]; // For future expansion.
		} if (parentElement instanceof OOSEMElementGroup g) {
			return g.getContent().toArray();
		} if (parentElement instanceof Namespace n) {
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
