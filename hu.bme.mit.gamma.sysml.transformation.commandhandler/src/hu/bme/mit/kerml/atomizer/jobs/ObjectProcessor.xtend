package hu.bme.mit.kerml.atomizer.jobs

import org.eclipse.emf.ecore.EObject
import org.omg.sysml.lang.sysml.Classifier
import org.omg.sysml.lang.sysml.Namespace
import org.omg.sysml.lang.sysml.Package
import java.util.Collection
import org.omg.sysml.lang.sysml.PartDefinition
import org.omg.sysml.lang.sysml.PartUsage

class ObjectProcessor {
        static def dispatch void executeTarget(Namespace n, Collection<EObject> roots){
        	    System.out.println(":P namespace - " + n.declaredName)
        	
                for (m : n.member) {
                        executeTarget(m, roots)
                }
                System.out.println(":/ namespace - " + n.declaredName)
                
        }


        static def dispatch void executeTarget(Package p, Collection<EObject> roots) {
                // p: «p.name» «p.declaredName»
                System.out.println(":P package - " + p.declaredName)
                for (m : p.member) {
                        executeTarget(m, roots)
                }
                System.out.println(":/ package - " + p.declaredName)
                
        }
        
        static def dispatch void executeTarget(PartDefinition p, Collection<EObject> roots) {
                // p: «p.name» «p.declaredName»
                System.out.println(":P partdef - " + p.declaredName)
                for (m : p.member) {
                        executeTarget(m, roots)
                }
                System.out.println(":/ partdef - " + p.declaredName)
                
        }
        
        static def dispatch void executeTarget(PartUsage p, Collection<EObject> roots) {
                // p: «p.name» «p.declaredName»
                System.out.println(":P partusage - " + p.declaredName)
                for (m : p.member) {
                        executeTarget(m, roots)
                }
                System.out.println(":/ partusage - " + p.declaredName)
                
        }

        static def dispatch void executeTarget(Classifier classifier, Collection<EObject> roots) {
        	System.out.println(":C")
                /*if ("ToExecute".equals(classifier.name)) {
                        System.out.println("ToExecute matches name")
                        System.out.println("collecting associations")
                        //ExtentManager.instance.registerAssociations(Atomizer.collectAssociations(roots))
                        ExtentManager.instance.registerAssociations(new HashMap)
                        System.out.println("collecting connectors")
                        ExtentManager.instance.registerConnectors(Atomizer.collectConnectors(roots))
                        System.out.println("executing target")
                        Atomizer.execute(classifier, null)
                        System.out.println("satisfying pairings")
                        Atomizer.satisfyPairings()
                        System.out.println("concretizing pairings")
                        Atomizer.concretizePairings()
                        
                }*/
        }

        static def dispatch void executeTarget(EObject object, Collection<EObject> context) {
        	
                System.out.println("Can't handle EObject: " + object);
        }
}
