library package PropulsionComponents {
	private import D_Quadcopter::PropulsionCmdBus;
	private import D_Quadcopter::PowerBus_12V;
	
	part def ElectronicSpeedController_Single {
		port propCmdBus : PropulsionCmdBus;
		port motorPort : MotorPort;
        
		port powerBus_12V : PowerBus_12V;
	}
	
	part def ElectronicSpeedController_4in1 {
		port propCmdBus : PropulsionCmdBus;
        
		port motorPort1 : MotorPort;
		port motorPort2 : MotorPort;
		port motorPort3 : MotorPort;
		port motorPort4 : MotorPort;
        
		port powerBus_12V : PowerBus_12V;
	}
	
	part def Motor {
		port motorPort : MotorPort;
	}
	
	port def MotorPort;
}