package hu.bme.mit.sysml.oosem.generators

import org.omg.sysml.lang.sysml.OccurrenceDefinition
import org.omg.sysml.lang.sysml.PartDefinition
import org.omg.sysml.lang.sysml.ItemDefinition
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.OccurrenceUsage
import org.omg.sysml.lang.sysml.ItemUsage
import org.omg.sysml.lang.sysml.PartUsage
import org.omg.sysml.lang.sysml.PortUsage
import org.eclipse.emf.ecore.EObject
import hu.bme.mit.sysml.oosem.util.OOSEMUtils
import org.omg.sysml.lang.sysml.Usage
import org.omg.sysml.lang.sysml.Definition

class GeneratorUtils {
	def static dispatch String getSysMLType(OccurrenceDefinition o){
		return "occurrence"
	}
	
	def static dispatch String getSysMLType(ItemDefinition o){
		return "item"
	}
	
	def static dispatch String getSysMLType(PartDefinition o){
		return "part"
	}
	
	def static dispatch String getSysMLType(PortDefinition o){
		return "port"
	}
	
	def static dispatch String getSysMLType(Definition o){
		return ""
	}
	
	
	
	def static dispatch String getSysMLType(OccurrenceUsage o){
		return "occurrence"
	}
	
	def static dispatch String getSysMLType(ItemUsage o){
		return "item"
	}
	
	def static dispatch String getSysMLType(PartUsage o){
		return "part"
	}
	
	def static dispatch String getSysMLType(PortUsage o){
		return "port"
	}
	
	def static dispatch String getSysMLType(Usage o){
		if(o.definition === null || o.definition.empty) { return ""; }
		return getSysMLType(o.definition.first)
	}
	
	
	
	static def String getOOSEMMetadata(EObject o){
		switch(OOSEMUtils.getOOSEMBlockType(o)){
			case SPECIFICATION:
				return "specification"
			case DESIGN:
				return "design"
			case INTEGRATION:
				return "integration"
			default:
				return ""
		}
	}
}
