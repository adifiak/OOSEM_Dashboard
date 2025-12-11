 package S_Quadcopter {
 	private import OOSEM::OOSEM_Metadata::*;
	#specification part def S_Quadcopter {
		port wind : WindForces;
	 	port comm : RFCommunication;
	}
	port def WindForces;
	port def RFCommunication;
}

 