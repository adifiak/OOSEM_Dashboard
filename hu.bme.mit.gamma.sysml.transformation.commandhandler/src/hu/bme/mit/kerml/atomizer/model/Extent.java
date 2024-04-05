package hu.bme.mit.kerml.atomizer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.omg.sysml.lang.sysml.Type;
import hu.bme.mit.kerml.atomizer.model.Atom;

public class Extent {
	private Set<Atom> atoms = new LinkedHashSet<>();
	private Set<Extent> supersets = new HashSet<>();
	private Set<Extent> disjoints = new HashSet<>();
	private Set<Extent> paired = new HashSet<>();
	private Map<Extent, Set<Type>> pairings = new HashMap<>();

	private ExtentManager extentManager;
	private Atom context;
	private Type type;

	public Set<Extent> getDisjoints() {
		return disjoints;
	}

	public Set<Atom> getAtoms() {
		return atoms;
	}

	public Extent(ExtentManager extentManager, Atom context, Type type) {
		this.extentManager = extentManager;
		this.context = context;
		this.type = type;
	}

	public void add(Atom atom) {
		assert canAdd(atom);
		for (Extent superset : supersets) {
			superset.localAdd(atom);
		}
		localAdd(atom);
	}

	public boolean canAdd(Atom atom) {
		for (Extent superset : supersets) {
			if (!superset.canAdd(atom)) {
				return false;
			}
		}
		for (Extent disjoint : disjoints) {
			if (disjoint.atoms.contains(atom)) {
				return false;
			}
		}
		return true;
	}

	protected void localAdd(Atom atom) {
		atoms.add(atom);
	}

	public void addSuperset(Extent superExtent) {
		supersets.add(superExtent);
	}

	public String toString() {
		String r = "Extent of " + extentManager.getType(this).effectiveName();
		if (context != null) {
			r = r + " in context of " + context;
		}
		return r;
	}

	public boolean contains(Atom atom) {
		return atoms.contains(atom);
	}

	public void disjoin(Extent extent) {
		disjoints.add(extent);
		extent.disjoints.add(this);
	}

	public boolean isSubsetOf(Extent extent) {
		return supersets.contains(extent);
	}

	public boolean isSupersetOf(Extent extent) {
		return extent.isSubsetOf(this);
	}

	public String printState() {
		String atoms = "contained atoms:";
		StringJoiner a = new StringJoiner(", ");
		for (Atom atom : this.atoms) {
			a.add("\n\t" + atom.toString());
		}
		atoms += a.toString() + "\n";

		String supersets = "supersets: ";
		StringJoiner s = new StringJoiner(", ");
		for (Extent e : this.supersets) {
			s.add(e.toString());
		}
		supersets += s.toString() + "\n";

		String disjoints = "disjoined extents: ";
		StringJoiner d = new StringJoiner(", ");
		for (Extent dis : this.disjoints) {
			d.add(dis.toString());
		}
		disjoints += d.toString() + "\n";

		return atoms + supersets + disjoints;
	}

	public void pair(Extent extent, Type connectingElement) {
		if (this == extent) {
			System.out.println("pairing with self...");
		}
		System.out.println("Pairing " + this + " to " + extent + " due to " + connectingElement);
		if (extent.getType().effectiveName().equals("wheel")) { // TODO: EZT TÖRÖLNI
			throw new IllegalArgumentException("that was not supposed to happen");
		}
		paired.add(extent);
		getPairings().computeIfAbsent(extent, t -> new HashSet<>()).add(connectingElement);
		if (!extent.getPaired().contains(this)) {
			extent.pair(this, connectingElement);
		}
	}

	public int count() {
		return atoms.size();
	}

	public Set<Extent> getPaired() {
		return paired;
	}

	public Atom getContext() {
		return context;
	}

	public Type getType() {
		return this.type;
	}

	public Map<Extent, Set<Type>> getPairings() {
		return pairings;
	}

	public void addAll(Collection<Atom> atoms) {
		atoms.forEach(atom -> add(atom));
	}

}
