library package OOSEM {
    library package OOSEM_Concepts {
        abstract occurrence def SpecificationBlock;
        abstract occurrence specificationBlocks : SpecificationBlock[*] nonunique;

        abstract occurrence def DesignBlock :> SpecificationBlock {
            occurrence components :>> specificationBlocks;
        }
        abstract occurrence designBlocks : DesignBlock[*] nonunique;

        abstract occurrence def IntegrationBlock :> DesignBlock{
            occurrence components :>> components, designBlocks;
            // Test 1
            //occurrence integratedComponents :>> components, integrationBlocks;
            //Nem lehet kétszer redefiniálni...
            // ITT VAN BAJ  Arra --V 
        }
        abstract occurrence integrationBlocks : IntegrationBlock[*] nonunique;

    }
    
    library package OOSEM_Metadata {
        private import OOSEM_Concepts::*;
        private import Metaobjects::SemanticMetadata;

        metadata def specification :> SemanticMetadata {
            :>> baseType = specificationBlocks meta SysML::Usage;
        }

        metadata def design :> SemanticMetadata {
            :>> baseType = designBlocks meta SysML::Usage;
        }

        metadata def integration :> SemanticMetadata {
            :>> baseType = integrationBlocks meta SysML::Usage;
        }
    }

    package Demo0 {

        part def S1;
 
        part def D1 :> S1 {
            part s1_1 : S1_1;
            part s1_2 : S1_2;
        }
        part def S1_1;
        part def S1_2;

        part def D1_1 :> S1_1;
        part def D1_2 :> S1_2;

        part def I1_2 :> D1_2;

        part def I1 :> D1{
            part d1_1 :>> s1_1 : D1_1;
            part i1_2 :>> s1_2 : I1_2;
        }
    }

    package Demo1 {
        private import OOSEM_Concepts::*;

        part def S1 :> SpecificationBlock;

        part def D1 :> S1, DesignBlock {
            part s1_1 : S1_1 :> components;
            part s1_2 : S1_2 :> components;
        }
        part def S1_1 :> SpecificationBlock;
        part def S1_2 :> SpecificationBlock;

        part def D1_1 :> S1_1, DesignBlock;
        part def D1_2 :> S1_2, DesignBlock;

        part def I1_2 :> D1_2, IntegrationBlock;

        part def I1 :> D1, IntegrationBlock {
            part d1_1 :>> s1_1 : D1_1 :> IntegrationBlock::components;
            part i1_2 :>> s1_2 : I1_2 :> IntegrationBlock::components;
        }
    }

    package Demo2 {
        private import OOSEM_Metadata::*;

        #specification part def S1;
 
        #design part def D1 :> S1 {
            #specification part s1_1 : S1_1;
            #specification part s1_2 : S1_2;
        }
        #specification part def S1_1;
        #specification part def S1_2;

        #design part def D1_1 :> S1_1;
        #design part def D1_2 :> S1_2;

        #integration part def I1_2 :> D1_2;

        #integration part def I1 :> D1 {
            #design part d1_1 :>> s1_1 : D1_1;
            #integration part i1_2 :>> s1_2 : I1_2;
        }
    }

}