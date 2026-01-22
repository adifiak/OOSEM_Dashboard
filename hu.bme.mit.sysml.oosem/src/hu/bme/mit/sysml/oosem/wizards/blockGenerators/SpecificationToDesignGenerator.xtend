package hu.bme.mit.sysml.oosem.wizards.blockGenerators

import hu.bme.mit.sysml.oosem.wizards.blockGenerators.BasicBlockGenerationData
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.GeneratorUtils

class SpecificationToDesignGenerator {
	static def String generate(BasicBlockGenerationData data) {
		return '''
			package «data.blockName» {
			    private import OOSEM::OOSEM_Metadata::*;
			    private import «data.subjectSpecification.qualifiedName»;
			
			    #design «GeneratorUtils.getSysMLType(data.subjectSpecification)» def «data.blockName» :> «data.subjectSpecification.name» {
			    	//TODO: Auto-generated block skeleton
			    }
			}
		'''
	}
}
