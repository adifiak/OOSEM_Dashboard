package hu.bme.mit.sysml.oosem.model.project.implementations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.implementations.OOSEMProjectImpl.OOSEMProjectImplData;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

public class BlockFamilyStructures {
	BlockFamilyStructures(OOSEMProjectImplData data, OOSEMBlockType parentType, Set<OOSEMBlock> childsSet) {
		for (var block : childsSet) {
			if(!(block.getObject() instanceof OccurrenceDefinition)) { continue; }
			
			OccurrenceDefinition o = (OccurrenceDefinition) block.getObject();
			var oosemParents = OOSEMUtils.getAncestorBlocksByOOSEMType(parentType, o);
			
			if (oosemParents.size() == 0) {
				orphanBlocks.add(block);
				continue;
			}
			
			processParents(data, block, oosemParents);
		}
	}
	
	public Map<OOSEMBlock, Set<OOSEMBlock>> getBlocksWithFamily(){
		return blocksWithFamily;
	}
	
	public Set<OOSEMBlock> getOrphanedBlocks(){
		return orphanBlocks;
	}
	
	private final Map<OOSEMBlock, Set<OOSEMBlock>> blocksWithFamily = new HashMap<OOSEMBlock, Set<OOSEMBlock>>();
	private final Set<OOSEMBlock> orphanBlocks = new HashSet<OOSEMBlock>();
	
	private void processParents(OOSEMProjectImplData data, OOSEMBlock block, List<Type> oosemParents) {
		for(var parent : oosemParents) {
			var parentBlock = data.blockCatalog.get(parent);
			if(parentBlock == null) { continue; }
			
			var childs = blocksWithFamily.get(parentBlock);
			if(childs == null) {
				childs = new HashSet<OOSEMBlock>();
				blocksWithFamily.put(parentBlock, childs);
			}
			
			childs.add(block);
		}
	}
}