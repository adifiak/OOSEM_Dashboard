package hu.bme.mit.kerml.atomizer.wizards.blockGenerators

import org.omg.sysml.lang.sysml.OccurrenceDefinition
import org.omg.sysml.lang.sysml.PartDefinition
import org.omg.sysml.lang.sysml.ItemDefinition
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.OccurrenceUsage
import org.omg.sysml.lang.sysml.ItemUsage
import org.omg.sysml.lang.sysml.PartUsage
import org.omg.sysml.lang.sysml.PortUsage

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
}