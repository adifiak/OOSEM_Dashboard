package I_Quadcopter {
    private import OOSEM::OOSEM_Metadata::*;
    private import D_Quadcopter::D_Quadcopter;
    private import I_Airframe::I_Airframe;
    private import I_CommunicationsModule::I_CommunicationsModule;
    private import I_FlightComputer::I_FlightComputer;
    private import I_Propulsion_CombinedESC::I_Propulsion_CombinedESC;
    private import I_PowerSupply::I_PowerSupply;

    #integration part def I_Quadcopter :> D_Quadcopter {
        #integration part :>> airframe : I_Airframe;
        #integration part :>> commModule : I_CommunicationsModule;
        #integration part :>> flightComputer : I_FlightComputer;
        #integration part :>> propulsion : I_Propulsion_CombinedESC;
        #integration part :>> powerSupply : I_PowerSupply;
        //TODO: Auto-generated block skeleton
    }
}