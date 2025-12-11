package D_FlightComputer {
    private import OOSEM::OOSEM_Metadata::*;
    private import D_Quadcopter::S_FlightComputer;
    private import D_Quadcopter::CommunicationBus;
    private import D_Quadcopter::PropulsionCmdBus;
    private import D_Quadcopter::PowerBus_5V;

    #design part def D_FlightComputer :> S_FlightComputer {
        part processor : Processor;
        part gyroscope : Gyroscope;
        
        #specification part firmware : S_Firmware;
        
        allocate firmware to processor;
        
        bind commBus = processor.commBus;
        bind propCmdBus = processor.propCmdBus;
        
        interface gyroscope.sensorPort to processor.sensorPort;
        
        interface powerBus_5V to processor.powerBus_5V;
        interface powerBus_5V to gyroscope.powerBus_5V;
    }
    
    #specification part def S_Firmware;
    
    part def Processor{
    	port sensorPort : SensorPort;
    	port commBus : CommunicationBus;
    	port propCmdBus : PropulsionCmdBus;
        
    	port powerBus_5V : PowerBus_5V;
    }
    
    part def Gyroscope{
    	port sensorPort : SensorPort;
        
    	port powerBus_5V : PowerBus_5V;
    }
    
    port def SensorPort;
}