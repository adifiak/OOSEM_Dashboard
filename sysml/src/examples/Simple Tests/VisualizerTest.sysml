package Example{
	package Parts{
		import Ports::dShaft;
		
		part def Motor
		{
			port shaft : dShaft;
		}
		
		part def RotorBlade{
			port mount : ~dShaft;
		}
	}
	package Ports{
		port def dShaft;
	}
	
	part ra{
		import Parts::*;
		
		part m: Motor;
		part b: RotorBlade;
	}
}