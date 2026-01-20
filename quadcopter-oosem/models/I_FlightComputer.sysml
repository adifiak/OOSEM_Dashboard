package I_FlightComputer {
    private import OOSEM::OOSEM_Metadata::*;
    private import D_FlightComputer::D_FlightComputer;
    private import I_Firmware_Interrupt::I_Firmware_Interrupt;

    #integration part def I_FlightComputer :> D_FlightComputer {
        #integration part :>> firmware : I_Firmware_Interrupt;
        //TODO: Auto-generated block skeleton
    }
}