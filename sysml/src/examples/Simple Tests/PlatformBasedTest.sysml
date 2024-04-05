library package PlatformBasedModeling {
    library package PlatformBasedConcepts {
        abstract occurrence def Function;
        abstract occurrence functions : Function[*] nonunique;

        abstract occurrence def Platform;
        abstract occurrence platforms : Platform[*] nonunique;

        abstract occurrence def Architecture {
            occurrence functions :>> functions;
            occurrence platformElements :>> platforms;
            occurrence subsystems :>> architectures;
        }
        abstract occurrence architectures : Architecture[*] nonunique;
    }

    library package PlatformBasedMetadata {
        private import PlatformBasedConcepts::*;
        private import Metaobjects::SemanticMetadata;

        metadata def architecture :> SemanticMetadata {
            :>> baseType = architectures meta SysML::Usage;
        }

        metadata def function :> SemanticMetadata {
            :>> baseType = functions meta SysML::Usage;
        }

        metadata def platform :> SemanticMetadata {
            :>> baseType = platforms meta SysML::Usage;
        }
    }

    package Demo0 {
        part def a0 {
            part p1 : P1;
            part f1 : F1;
            part f2 : F2;
            part a1 : A1;
        }

        part def P1;
        part def F1;
        part def F2;
        part def A1;
    }

    package Demo1 {
        private import PlatformBasedConcepts::*;

        part def A0 :> Architecture {
            part p1 : P1 :> platformElements;
            part f1 : F1 :> functions;
            part f2 : F2 :> functions;
            part a1 : A1 :> subsystems;
        }

        part def P1 :> Platform;
        part def F1 :> Function;
        part def F2 :> Function;
        part def A1 :> Architecture;
    }

    package Demo2 {
        private import PlatformBasedMetadata::*;
        
        #architecture part def a0 {
            #platform part p1 : P1;
            #function part f1 : F1;
            #function part f2 : F2;
            #architecture part a1 : A1;
        }

        #platform part def P1;
        #function part def F1;
        #function part def F2;
        #architecture part def A1;
    }
}