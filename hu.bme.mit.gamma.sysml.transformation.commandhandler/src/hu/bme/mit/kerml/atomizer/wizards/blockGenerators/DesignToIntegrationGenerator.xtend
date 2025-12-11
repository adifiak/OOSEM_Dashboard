package hu.bme.mit.kerml.atomizer.wizards.blockGenerators

import java.io.FileWriter
import java.io.IOException
import org.omg.sysml.lang.sysml.Type
import org.eclipse.emf.ecore.EObject
import hu.bme.mit.kerml.atomizer.util.OOSEMUtils

class DesignToIntegrationGenerator {
	static def void generate(BasicBlockGenerationData data, IntegrationData data2) {
		
		val content = '''
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
                		#«getMetadata(p.implementation)» «GeneratorUtils.getSysMLType(p.implementation as Type)»«IF data2.featureNames.get(p.specification) !== null» «data2.featureNames.get(p.specification)» «ENDIF»:>> «(p.specification as Type).name» : «(p.implementation as Type).name»;
                		«ENDIF»
                	«ENDFOR»
                    //TODO: Auto-generated block skeleton
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
	
	static def String getMetadata(EObject o){
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