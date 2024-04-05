package hu.bme.mit.kerml.atomizer.jobs

import org.eclipse.emf.ecore.EObject
import org.omg.sysml.lang.sysml.Namespace
import org.omg.sysml.lang.sysml.PartUsage
import org.omg.sysml.lang.sysml.PartDefinition
import org.omg.sysml.lang.sysml.Package
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.Feature
import org.omg.sysml.util.FeatureUtil
import org.omg.sysml.lang.sysml.ConjugatedPortDefinition

class HierarchyVisualizer {
		static int depth = 0

        static def dispatch void processNode(Namespace n) {        		
        	if(depth == 0) { // Create file header and footer at the default container namespace
        		System.out.println("@startmindmap")
        		
        		System.out.println("<style>\nroot {\n\tFontColor #?black:white\n}\n</style>")
        		System.out.println("top to bottom direction\n")
        		
        		namespaceProcessor(n)
        		
        		System.out.println("\nlegend")
        		System.out.println("\t<&folder>Package")
        		System.out.println("\t<&cog>Part")
        		System.out.println("\t<&puzzle-piece>Port")
        		System.out.println("endlegend")
        		
        		System.out.println("@endmindmap")
        	} else { // Visualize normal nodes
        		drawNode(n)
        		namespaceProcessor(n)
        	}
        }
        
        //static def dispatch void processNode(FeatureImpl fi) {}

        static def dispatch void processNode(EObject object) {
        	System.out.println("'Non-namespace object: " + object)
        }
        
        static def void namespaceProcessor(Namespace n){
        	depth++
        	for (m : n.ownedMember) {
        		if(!m.declaredName.nullOrEmpty)
                	processNode(m)
            }
            depth--
        }
        
        static def void nodeDrawer(String color, String icon, String name, String type){
        	// Depth indicators
        	for(i : 0 ..< depth) {
        		System.out.print("*")
        	}
        	
        	// Color
        	if(!color.nullOrEmpty)
        		System.out.print("[#" + color + "] ")
        	
        	// Icon
        	if(!icon.nullOrEmpty)
        		System.out.print(" <&" + icon + "> ")
        	
        	// Name
        	System.out.print(name)
        	
        	// Type
        	if(!type.nullOrEmpty)
        		System.out.print(" : " + type)
        	
        	// Terminator
        	System.out.print("\n")
        }
        
        static def dispatch void drawNode(Namespace n) {
        	nodeDrawer("", "", n.declaredName, "")
        }
        
        static def dispatch void drawNode(Package p) {
            nodeDrawer("DarkViolet", "folder", p.declaredName, "")
        }
        
        static def dispatch void drawNode(PartDefinition p) {
            nodeDrawer("RoyalBlue", "cog", p.declaredName, "")
        }
        
        static def dispatch void drawNode(PartUsage p) {
        	var typeName = ""
        	var types = FeatureUtil.getAllTypesOf(p as Feature)
        	if(types.length > 0) {
        		var type = types.get(0)
        		typeName = type.declaredName
        		if(!typeName.nullOrEmpty && typeName.equals("Part"))
        			typeName = ""
        	}
        	nodeDrawer("Blue", "cog", p.declaredName, typeName)
        }
        
        static def dispatch void drawNode(PortDefinition p) {
            nodeDrawer("OliveDrab", "puzzle-piece", p.declaredName, "")
        }
        
        static def dispatch void drawNode(PortUsage p) {
        	var typeName = ""
        	var types = FeatureUtil.getAllTypesOf(p as Feature)
	        if(types.length > 0) {
	        	var type = types.get(0)
	        	if(type instanceof ConjugatedPortDefinition){
	        		typeName = "~" + type.originalPortDefinition.declaredName;
	        	} else {
	        		typeName = type.declaredName
	        	}
	        }
        	nodeDrawer("Green", "puzzle-piece", p.declaredName, typeName)
        }
        
}