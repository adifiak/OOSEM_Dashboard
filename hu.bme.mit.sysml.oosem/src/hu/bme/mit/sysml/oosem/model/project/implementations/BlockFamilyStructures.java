package hu.bme.mit.sysml.oosem.model.project.implementations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.omg.sysml.lang.sysml.OccurrenceDefinition;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.implementations.OOSEMProjectImpl.OOSEMProjectImplData;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

public class BlockFamilyStructures {
	private final Map<OOSEMBlock, Set<OOSEMBlock>> blocksWithFamily = new HashMap<OOSEMBlock, Set<OOSEMBlock>>();
	private final Set<OOSEMBlock> orphanBlocks = new HashSet<OOSEMBlock>();
	
	BlockFamilyStructures(OOSEMProjectImplData data, OOSEMBlockType parentType, Set<OOSEMBlock> childsSet) {
		for (var block : childsSet) {
			if (block.getObject() instanceof OccurrenceDefinition o) {
				var oosemParents = OOSEMUtils.getAncestorBlocksByOOSEMType(parentType, o);
				if (oosemParents.size() == 0) {
					orphanBlocks.add(block);
				} else {
					for(var parent : oosemParents) {
						var parentBlock = data.blockCatalog.get(parent);
						if(parentBlock != null) {
							if (blocksWithFamily.containsKey(parentBlock)) {
								var childs = blocksWithFamily.get(parentBlock);
								childs.add(block);
							} else {
								var childs = new HashSet<OOSEMBlock>();
								childs.add(block);
								blocksWithFamily.put(parentBlock, childs);
							}
						}
					}
				}
			} else {
				System.err.println("Non-OccurrenceDef. element in getBlockOfTypeWithChilds()");
			}
		}
	}
	
	public Map<OOSEMBlock, Set<OOSEMBlock>> getBlocksWithFamily(){
		return blocksWithFamily;
	}
	
	public Set<OOSEMBlock> getOrphanedBlocks(){
		return orphanBlocks;
	}
}