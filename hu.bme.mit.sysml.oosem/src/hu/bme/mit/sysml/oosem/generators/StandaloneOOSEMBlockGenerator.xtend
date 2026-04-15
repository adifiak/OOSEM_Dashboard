package hu.bme.mit.sysml.oosem.generators

import org.omg.sysml.lang.sysml.Type
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData
import hu.bme.mit.sysml.oosem.generators.IGenerator
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration.RefinementWorkflow
import hu.bme.mit.sysml.oosem.util.OOSEMUtils
import hu.bme.mit.sysml.oosem.generators.BlockGenerationData.RefinementData.RefinementConfiguration

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
			    «generateBlockFramesForFeatures(data.propertyRefinementConfigs)»
			    «generateBlockFramesForFeatures(data.subsystemRefinementConfigs)»
			}
		'''
	}
	
	def String generateImport(BlockGenerationData.RefinementData data) {
		return '''
			«FOR c : data.configurations»
				«IF c.workflow == RefinementWorkflow.CHOOSE_EXISTING»
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
				«IF c.workflow == RefinementWorkflow.CHOOSE_EXISTING »
					#«GeneratorUtils.getOOSEMMetadata(c.type.object as Type)» «GeneratorUtils.getSysMLType(c.refinedFeature.object as Type)» «IF c.name !== null && !c.name.isEmpty»«c.name» «ENDIF»:>> «c.refinedFeature.name» : «IF c.requiresIntegration »«c.type.name»«ELSE»«c.newTypeName»«ENDIF»;
				«ELSE»
					«IF c.workflow == RefinementWorkflow.GENERATE_FRAME »
						«generateOOSEMType(OOSEMUtils.getTypeForNextPhase(c.refinedFeature.OOSEMBlockType))» «GeneratorUtils.getSysMLType(c.refinedFeature.object as Type)» «IF c.name !== null && !c.name.isEmpty»«c.name» «ENDIF»:>> «c.refinedFeature.name» : «c.newTypeName»;
					«ENDIF»
				«ENDIF»
			«ENDFOR»
		'''
	}
	

	
	def String generateBlockFramesForFeatures(BlockGenerationData.RefinementData data) {
		return '''
			«FOR c : data.configurations»
				«IF c.workflow == RefinementWorkflow.GENERATE_FRAME»
				
				«generateOOSEMType(OOSEMUtils.getTypeForNextPhase(c.refinedFeature.OOSEMBlockType))» «GeneratorUtils.getSysMLType(c.refinedFeature.object as Type)» def «c.newTypeName» :> «c.refinedFeature.name» {
				    //TODO: Auto-generated block skeleton
				}
				«ENDIF»
			«ENDFOR»
		'''
	}
}