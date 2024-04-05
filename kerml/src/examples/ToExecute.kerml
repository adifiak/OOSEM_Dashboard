package test {
	import OneToOneConnectorsModelToBeExecuted::*;
	
	classifier ToExecute {
		feature target : Bicycle [1];
	}
}

//package WithoutConnectorsExecution {
	//import withoutconnectors::ToExecute;

	//import Atoms::*;
	//import WithoutConnectorsModelToBeExecuted::*;

	//#atom
	//classifier MyWheel1 specializes Wheel;
	//#atom
	//classifier MyWheel2 specializes Wheel;

	//classifier MyWheel unions MyWheel1, MyWheel2;

	//#atom
	//classifier MyBike specializes Bicycle {
		//feature redefines rollsOn : MyWheel;
	//}
	
	//#atom
	//classifier Executed specializes ToExecute {
		//feature redefines target : MyBike;
	//}
//}