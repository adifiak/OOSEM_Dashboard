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
import org.omg.sysml.lang.sysml.Type
import org.omg.sysml.model.Element

class OOSEMVisualizer {
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

        static def dispatch void processNode(EObject object) {
        	System.out.println("'Non-namespace object: " + object)
        }
        
        static def void namespaceProcessor(Namespace n){
        	depth++
        	for (m : n.ownedMember) {
        		if(!m.declaredName.nullOrEmpty) {
        			if(!m.declaredName.equals("OOSEM")) {
        				processNode(m)
        			}
        		}	
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
            nodeDrawer("White", "folder", p.declaredName, "")
        }
        
        static def dispatch void drawNode(PartDefinition p) {
        	var Color = getBlockColor(p, true)
            nodeDrawer(Color, "cog", p.declaredName, "")
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
        	var Color = getBlockColor(p, false)
        	nodeDrawer(Color, "cog", p.declaredName, typeName)
        }
        
        static def dispatch void drawNode(PortDefinition p) {
            nodeDrawer("White", "puzzle-piece", p.declaredName, "")
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
        	nodeDrawer("White", "puzzle-piece", p.declaredName, typeName)
        }
        
        //TODO: Use OOSEMUtils.getOOSEMBlockType()
        static def String getBlockColor(Type t, Boolean dark) {
        	var types = t.allSupertypes
        	
        	var spec = false
        	var desi = false
        	var inte = false
        	
        	for (type : types) {
        		if(type.declaredName.equals("SpecificationBlock")) {
        			spec = true
        		} else if(type.declaredName.equals("DesignBlock")) {
        			desi = true
        		} else if(type.declaredName.equals("IntegrationBlock")) {
        			inte = true
        		}
        	}
        	
        	if(inte){
        		return dark ? "Navy" : "RoyalBlue"
        	} else if(desi){
        		return dark ? "OliveDrab" : "YellowGreen"
        	} else if(spec){
        		return dark ? "Crimson" : "LightCoral"
        	}
        	
        	return "White"
        }
}