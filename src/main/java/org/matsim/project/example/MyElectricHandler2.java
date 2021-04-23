package org.matsim.project.example;


import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;


//import javax.inject.Inject;

public class MyElectricHandler2 implements VehicleLeavesTrafficEventHandler {
        @Inject
        ElectricFleet electricFleet;


        @Override
        public void handleEvent(VehicleLeavesTrafficEvent vehicleLeavesTrafficEvent) {

            Id<ElectricVehicle> evId = Id.create(vehicleLeavesTrafficEvent.getVehicleId(),ElectricVehicle.class);
            Double soc = electricFleet.getElectricVehicles().get(evId).getBattery().getSoc();

            System.out.println("vehicle" + evId.toString() + " stops with a SoC of " + soc);



    }
}
