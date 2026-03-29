package hu.bme.mit.sysml.oosem.generators

import org.omg.sysml.lang.sysml.Type
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData
import hu.bme.mit.sysml.oosem.generators.IGenerator

class DesignToIntegrationGenerator implements IGenerator {
	override String generate(BlockGenerationData data) {
		return '''
			package «data.blockName» {
			    private import OOSEM::OOSEM_Metadata::*;
			    private import «(data.subject.object as Type).qualifiedName»;
			    «FOR c : data.propertyRefinementConfigs.configurations»
			    	«IF c.type !== null »
			    		private import «(c.type.object as Type).qualifiedName»;
			    	«ENDIF»
			    «ENDFOR»
			    «FOR c : data.subsystemRefinementConfigs.configurations»
			    	«IF c.type !== null »
			    		private import «(c.type.object as Type).qualifiedName»;
			    	«ENDIF»
			    «ENDFOR»
			
			    #integration «GeneratorUtils.getSysMLType(data.subject.object as Type)» def «data.blockName» :> «(data.subject.object as Type).name» {
			    	«FOR c : data.propertyRefinementConfigs.configurations»
			    		«IF c.type === null »
			    			//#<OOSEMMetadata> «GeneratorUtils.getSysMLType(c.refinedFeature.object as Type)» <NewName> :>> «c.refinedFeature.name» : <NewType>;
			    		«ELSE»
			    			#«GeneratorUtils.getMetadata(c.type.object)» «GeneratorUtils.getSysMLType(c.type.object as Type)» «IF !c.name.isEmpty»«c.name» «ENDIF»:>> «c.refinedFeature.name» : «c.type.name»;
			    		«ENDIF»
			    	«ENDFOR»
			    	
					«FOR c : data.subsystemRefinementConfigs.configurations»
						«IF c.type === null »
							//#<OOSEMMetadata> «GeneratorUtils.getSysMLType(c.refinedFeature.object as Type)» <NewName> :>> «c.refinedFeature.name» : <NewType>;
						«ELSE»
							#«GeneratorUtils.getMetadata(c.type.object)» «GeneratorUtils.getSysMLType(c.type.object as Type)» «IF !c.name.isEmpty»«c.name» «ENDIF»:>> «c.refinedFeature.name» : «c.type.name»;
						«ENDIF»
					«ENDFOR»
					//TODO: Auto-generated block skeleton
			    }
			}
		'''
	}
}