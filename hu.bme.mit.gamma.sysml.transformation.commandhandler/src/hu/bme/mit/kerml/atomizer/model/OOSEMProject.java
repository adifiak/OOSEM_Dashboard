package hu.bme.mit.kerml.atomizer.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.kerml.atomizer.util.OOSEMUtils;
import hu.bme.mit.kerml.atomizer.util.OOSEMUtils.OOSEMBlockType;

import org.eclipse.emf.ecore.EObject;

public class OOSEMProject {
	public OOSEMProject(Set<EObject> specifications, Set<EObject> designs, Set<EObject> integrations) {
		this.specifications = specifications;
		this.designs = designs;
		this.integrations = integrations;
	}

	public Set<EObject> getSpecifications() {
		return specifications;
	}

	public Set<EObject> getDesigns() {
		return designs;
	}

	public Set<EObject> getIntegrations() {
		return integrations;
	}

	public BlockFamilyStructures getSpecificationsWithTheirDesigns() {
		return getBlockOfTypeWithChilds(OOSEMBlockType.SPECIFICATION);
	}

	public BlockFamilyStructures getDesignsWithTheirIntegrations() {
		return getBlockOfTypeWithChilds(OOSEMBlockType.DESIGN);
	}

	private BlockFamilyStructures getBlockOfTypeWithChilds(OOSEMBlockType parentType) {
		switch (parentType) {
		case SPECIFICATION:
			return collectBlocksAndTheirChilds(parentType, designs);
		case DESIGN:
			return collectBlocksAndTheirChilds(parentType, integrations);
		default:
			System.err.println("Unsupported parent type: " + parentType);
			return null;
		}
	}

	private BlockFamilyStructures collectBlocksAndTheirChilds(OOSEMBlockType parentType, Set<EObject> childsSet) {
		var families = new HashMap<EObject, Set<EObject>>();
		var orphans = new HashSet<EObject>();
		
		for (var s : childsSet) {
			if (s instanceof OccurrenceDefinition o) {
				var oosemParents = getParentBlocksWithType(parentType, o);
				if (oosemParents.size() > 1) {
					System.err.println("Several parents found to child. (Multistep refinement is not supported yet.)");
					System.err.println(oosemParents);
					// TODO ...
				} else if (oosemParents.size() == 0) {
					orphans.add(o);
				} else {
					var parent = oosemParents.get(0);
					if (families.containsKey(parent)) {
						var childs = families.get(parent);
						childs.add(o);
					} else {
						var childs = new HashSet<EObject>();
						childs.add(o);
						families.put(parent, childs);
					}
				}
			} else {
				System.err.println("Non-OccurrenceDef. element in getBlockOfTypeWithChilds()");
			}
		}
		return new BlockFamilyStructures(families, orphans);
	}

	private List<Type> getParentBlocksWithType(OOSEMBlockType parentType, OccurrenceDefinition o) {
		return o.allSupertypes().stream()
				.filter(t -> (t.getDeclaredName() != null && !t.getDeclaredName().isEmpty()
						&& !t.getDeclaredName().equals("SpecificationBlock")
						&& !t.getDeclaredName().equals("DesignBlock")
						&& !t.getDeclaredName().equals("IntegrationBlock"))
						&& OOSEMUtils.getOOSEMBlockType(t) == parentType)
				.collect(Collectors.toList());
	}
	
	public class BlockFamilyStructures {
		private final Map<EObject, Set<EObject>> blocksWithFamily;
		private final Set<EObject> orphanBlocks;
		
		BlockFamilyStructures(Map<EObject, Set<EObject>> blocksWithFamily, Set<EObject> orphanBlocks) {
			this.blocksWithFamily = blocksWithFamily;
			this.orphanBlocks = orphanBlocks;
		}
		
		public Map<EObject, Set<EObject>> getBlocksWithFamily(){
			return blocksWithFamily;
		}
		
		public Set<EObject> getOrphanedBlocks(){
			return orphanBlocks;
		}
	}

	private Set<EObject> specifications;
	private Set<EObject> designs;
	private Set<EObject> integrations;
}
