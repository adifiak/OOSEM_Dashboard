package D_Propulsion_CombinedESC {
    private import OOSEM::OOSEM_Metadata::*;
    private import D_Quadcopter::S_Propulsion;
    private import PropulsionComponents::*;

    #design part def D_Propulsion_CombinedESC :> S_Propulsion {
        part esc_4in1 : ElectronicSpeedController_4in1;
        
        part motor1 : Motor;
        part motor2 : Motor;
        part motor3 : Motor;
        part motor4 : Motor;
        
        interface propCmdBus to esc_4in1.propCmdBus;
        
        interface powerBus_12V to esc_4in1.powerBus_12V;
        
        interface esc_4in1.motorPort1 to motor1.motorPort;
        interface esc_4in1.motorPort2 to motor2.motorPort;
        interface esc_4in1.motorPort3 to motor3.motorPort;
        interface esc_4in1.motorPort4 to motor4.motorPort;
    }
}