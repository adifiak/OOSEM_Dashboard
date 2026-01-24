package hu.bme.mit.sysml.oosem.wizards.blockGenerators

import org.omg.sysml.lang.sysml.Type
import org.eclipse.emf.ecore.EObject
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.BasicBlockGenerationData
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.IntegrationData
import hu.bme.mit.sysml.oosem.util.OOSEMUtils
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.GeneratorUtils

class DesignToIntegrationGenerator {
	static def String generate(BasicBlockGenerationData data, IntegrationData data2) {
		return '''
			package «data.blockName» {
			    private import OOSEM::OOSEM_Metadata::*;
			    private import «data.subjectSpecification.qualifiedName»;
			    «FOR p : data2.configs»
			    	«IF p.implementation !== null »
			    		private import «(p.implementation as Type).qualifiedName»;
			    	«ENDIF»
			    «ENDFOR»
			
			    #integration «GeneratorUtils.getSysMLType(data.subjectSpecification)» def «data.blockName» :> «data.subjectSpecification.name» {
					«FOR p : data2.configs»
						«IF p.implementation === null »
							//#<OOSEMMetadata> «GeneratorUtils.getSysMLType(p.specification as Type)» <NewName> :>> «(p.specification as Type).name» : <NewType>;
						«ELSE»
							#«GeneratorUtils.getMetadata(p.implementation)» «GeneratorUtils.getSysMLType(p.implementation as Type)» «IF data2.featureNames.get(p.specification) !== null && !data2.featureNames.get(p.specification).isEmpty»«data2.featureNames.get(p.specification)» «ENDIF»:>> «(p.specification as Type).name» : «(p.implementation as Type).name»;
						«ENDIF»
					«ENDFOR»
					//TODO: Auto-generated block skeleton
			    }
			}
		'''
	}
}
