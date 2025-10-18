package hu.bme.mit.kerml.atomizer.wizards.blockGenerators

import java.io.FileWriter
import java.io.IOException

class SpecificationToDesignGenerator {
	static def void generate(BasicBlockGenerationData data) {
		
		val content = '''
            package «data.blockName» {
            
                private import OOSEM::OOSEM_Metadata::*;
                private import «data.subjectSpecification.qualifiedName»;

                #design «GeneratorUtils.getSysMLType(data.subjectSpecification)» «data.blockName» :> «data.subjectSpecification.name» {
            		//TODO: Auto generated block skeleton
            	}
            }
        '''
        
        try {
        	val writer = new FileWriter(data.path)
            writer.write(content)
            writer.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
	}
}