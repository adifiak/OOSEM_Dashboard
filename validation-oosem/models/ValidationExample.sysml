package OOSEMTest {
    private import OOSEM::OOSEM_Metadata::*;

	#specification part def S1;
	
	#specification part def S_err {
		#design part d_err;
	}
 
    #design part def D1 :> S1 {
        #specification part s1_1 : S1_1;
        #specification part s1_2 : S1_2;
    }
    #specification part def S1_1;
    #specification part def S1_2;

	#design part def D_err :> S1 {
		#integration i_err;
	}
	
	#design part def D1_alt :> S1;
	#design part def D1_alt2 :> S1;
	#design part def D_orp;
	#integration part def I_err;

    #design part def D1_1 :> S1_1;
    #design part def D1_2 :> S1_2{
    	#specification part s_unintegrated : S1;
    }

    #integration part def I1_2 :> D1_2;

    #integration part def I1 :> D1 {
        #design part d1_1 :>> s1_1 : D1_1;
        #integration part i1_2 :>> s1_2 : I1_2;
        #specification part s1_integratedSpec : S1; 
        #integration part i_unwanted : I_err; 
    }
}