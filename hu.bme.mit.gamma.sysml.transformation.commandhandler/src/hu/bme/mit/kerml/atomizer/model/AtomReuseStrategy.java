package hu.bme.mit.kerml.atomizer.model;

import java.util.ArrayList;
import java.util.Collection;

import org.omg.sysml.lang.sysml.Feature;

public interface AtomReuseStrategy {
	Collection<Atom> findSuitableAtoms(Feature feature, Atom context, ExtentManager extentManager);

	class NoReuseStrategy implements AtomReuseStrategy {

		@Override
		public ArrayList<Atom> findSuitableAtoms(Feature feature, Atom context, ExtentManager extentManager) {
			return new ArrayList<Atom>();
		}
	}

	class SpecificReuseStrategy implements AtomReuseStrategy { // TODO
		@Override
		public ArrayList<Atom> findSuitableAtoms(Feature feature, Atom context, ExtentManager extentManager) {
			return new ArrayList<Atom>();
		}
	}

	class AllReuseStrategy implements AtomReuseStrategy {
		@Override
		public ArrayList<Atom> findSuitableAtoms(Feature feature, Atom context, ExtentManager extentManager) {
			Collection<Extent> contextExtents = context.getEffectiveFeatures().values();
			Collection<Extent> nonContextExtents = extentManager.getExtents().values();

			Extent extent = extentManager.getExtent(feature, context);
			ArrayList<Atom> ret = new ArrayList<>();

			for (Extent e : contextExtents) {
				for (Atom a : e.getAtoms()) {
					if (extent.canAdd(a)) {
						ret.add(a);
					}
				}
			}

			for (Extent e : nonContextExtents) {
				for (Atom a : e.getAtoms()) {
					if (extent.canAdd(a)) {
						ret.add(a);
					}
				}
			}

			return ret;
		}
	}
}