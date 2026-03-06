library package PlatformBasedModeling {
	library package PlatformBased_Concepts {
		abstract occurrence def Component {
			occurrence subcomponents :>> components;
			occurrence functions :>> functions;
			occurrence platforms :>> platforms;
		}
		
		abstract occurrence def Function{
			occurrence subfunctions :>> functions;
		}
		
		abstract occurrence def Platform{
			occurrence subplatforms :>> platforms;
		}
		
		abstract occurrence components : Component[*] nonunique;
		abstract occurrence functions : Function[*] nonunique;
		abstract occurrence platforms : Platform[*] nonunique;
	}
	
	library package PlatformBased_Metadata {
		private import PlatformBased_Concepts::*;
        private import Metaobjects::SemanticMetadata;
        
        metadata def component :> SemanticMetadata {
            :>> baseType = components meta SysML::Usage;
        }

        metadata def function :> SemanticMetadata {
            :>> baseType = functions meta SysML::Usage;
        }

        metadata def platform :> SemanticMetadata {
            :>> baseType = platforms meta SysML::Usage;
        }
	}
	
	package Demo0 {
		part def C1 {
			part c1_1 : C1_1;
			
			part f1 : F1;
			part p1 : P1;
		}
		
		part def C1_1 {
			part f1_1 : F1_1;
			part p1_1 : P1_1;
		}
		
		part def F1;
		part def F1_1;
		
		part def P1;
		part def P1_1;
	}
	
	package Demo1 {
		private import PlatformBased_Concepts::*;
		
		part def C1 :> Component {
			part c1_1 : C1_1 :> subcomponents;
			
			part f1 : F1 :> functions;
			part p1 : P1 :> platforms;
		}
		
		part def C1_1 :> Component {
			part f1_1 : F1_1 :> functions;
			part p1_1 : P1_1 :> platforms;
		}
		
		part def F1 :> Function;
		part def F1_1 :> Function;
		
		part def P1 :> Platform;
		part def P1_1 :> Platform;
	}
	
	package Demo2 {
		private import PlatformBased_Metadata::*;
		
		#component part def C1 {
			#component part c1_1 : C1_1;
			
			#function part f1 : F1;
			#platform part p1 : P1;
		}
		
		#component part def C1_1 {
			#function part f1_1 : F1_1;
			#platform part p1_1 : P1_1;
		}
		
		#function part def F1;
		#function part def F1_1;
		
		#platform part def P1;
		#platform part def P1_1;
	}
}