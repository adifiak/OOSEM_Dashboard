package D_Quadcopter {
    private import OOSEM::OOSEM_Metadata::*;
    private import S_Quadcopter::S_Quadcopter;
    private import S_Quadcopter::WindForces;
    private import S_Quadcopter::RFCommunication;

    #design part def D_Quadcopter :> S_Quadcopter {
        #specification part airframe : S_Airframe;
        #specification part commModule : S_CommunicationsModule;
        #specification part flightComputer : S_FlightComputer;
        #specification part propulsion : S_Propulsion;
        #specification part powerSupply : S_PowerSupply;
        
        bind wind = airframe.windPort;
        bind comm = commModule.commPort;
        
        interface commModule.commBus to flightComputer.commBus;
        interface propulsion.propCmdBus to flightComputer.propCmdBus;
        
        interface commModule.powerBus_5V to powerSupply.powerBus_5V;
        interface flightComputer.powerBus_5V to powerSupply.powerBus_5V;
        interface propulsion.powerBus_12V to powerSupply.powerBus_12V;
    }
        #specification part def S_Airframe{
    	port windPort : WindForces;
    }
    
    #specification part def S_CommunicationsModule{
    	port commPort : RFCommunication;
    	port commBus : CommunicationBus;
    	port powerBus_5V : PowerBus_5V;
    }
    port def CommunicationBus;
    
    #specification part def S_FlightComputer {
    	port commBus : CommunicationBus;
    	port propCmdBus : PropulsionCmdBus;
    	port powerBus_5V : PowerBus_5V;
    }
    
    #specification part def S_Propulsion {
    	port propCmdBus : PropulsionCmdBus;
    	port powerBus_12V : PowerBus_12V;
    }
    port def PropulsionCmdBus;
    
    #specification part def S_PowerSupply {
    	port powerBus_12V : PowerBus_12V;
    	port powerBus_5V : PowerBus_5V;
    }
    port def PowerBus;
    port def PowerBus_12V :> PowerBus;
    port def PowerBus_5V :> PowerBus;
}
