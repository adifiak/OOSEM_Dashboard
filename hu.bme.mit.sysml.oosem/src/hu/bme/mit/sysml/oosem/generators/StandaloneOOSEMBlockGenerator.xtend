package hu.bme.mit.sysml.oosem.generators

import org.omg.sysml.lang.sysml.Type
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData
import hu.bme.mit.sysml.oosem.generators.IGenerator
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType

class StandaloneOOSEMBlockGenerator implements IGenerator {
	override String generate(BlockGenerationData data) {
		return '''
			package «data.blockName» {
			    private import OOSEM::OOSEM_Metadata::*;
			    private import «(data.subject.object as Type).qualifiedName»;
			    «generateImport(data.propertyRefinementConfigs)»
			    «generateImport(data.subsystemRefinementConfigs)»
			    
			    «generateOOSEMType(data.tagetType)» «GeneratorUtils.getSysMLType(data.subject.object as Type)» def «data.blockName» :> «(data.subject.object as Type).name» {
			        «generateFeatures(data.propertyRefinementConfigs)»
			        «generateFeatures(data.subsystemRefinementConfigs)»
			        
			        //TODO: Auto-generated block skeleton
			    }
			}
		'''
	}
	
	def String generateImport(BlockGenerationData.RefinementData data) {
		return '''
			«FOR c : data.configurations»
				«IF c.type !== null »
					private import «(c.type.object as Type).qualifiedName»;
				«ENDIF»
			«ENDFOR»
		'''
	}
	
	def String generateOOSEMType(OOSEMBlockType type) {
		switch(type) {
			case SPECIFICATION: {
				return "#specification"
			}
			case DESIGN: {
				return "#design"
			}
			case INTEGRATION: {
				return "#integration"
			}
			default: {
				return ""
			}
		}
		
	}
	
	def String generateFeatures(BlockGenerationData.RefinementData data) {
		return '''
			«FOR c : data.configurations»
				«IF c.type === null »
					//#<OOSEMMetadata> «GeneratorUtils.getSysMLType(c.refinedFeature.object as Type)» <NewName> :>> «c.refinedFeature.name» : <NewType>;
				«ELSE»
					#«GeneratorUtils.getOOSEMMetadata(c.type.object)» «GeneratorUtils.getSysMLType(c.type.object as Type)» «IF !c.name.isEmpty»«c.name» «ENDIF»:>> «c.refinedFeature.name» : «c.type.name»;
				«ENDIF»
			«ENDFOR»
		'''
	}
}