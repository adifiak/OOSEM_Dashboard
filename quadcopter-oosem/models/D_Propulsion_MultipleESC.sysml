package D_Propulsion_MultipleESC {
    private import OOSEM::OOSEM_Metadata::*;
    private import D_Quadcopter::S_Propulsion;
    private import PropulsionComponents::*;

    #design part def D_Propulsion_MultipleESC :> S_Propulsion {
        part esc_s1 : ElectronicSpeedController_Single;
        part esc_s2 : ElectronicSpeedController_Single;
        part esc_s3 : ElectronicSpeedController_Single;
        part esc_s4 : ElectronicSpeedController_Single;
        
        part motor1 : Motor;
        part motor2 : Motor;
        part motor3 : Motor;
        part motor4 : Motor;
        
        interface propCmdBus to esc_s1.propCmdBus;
        interface powerBus_12V to esc_s1.powerBus_12V;
        
        interface propCmdBus to esc_s2.propCmdBus;
        interface powerBus_12V to esc_s2.powerBus_12V;
        
        interface propCmdBus to esc_s3.propCmdBus;
        interface powerBus_12V to esc_s3.powerBus_12V;
        
        interface propCmdBus to esc_s4.propCmdBus;
        interface powerBus_12V to esc_s4.powerBus_12V;
        
        interface esc_s1.motorPort to motor1.motorPort;
        interface esc_s1.motorPort to motor2.motorPort;
        interface esc_s1.motorPort to motor3.motorPort;
        interface esc_s1.motorPort to motor4.motorPort;
    }
}