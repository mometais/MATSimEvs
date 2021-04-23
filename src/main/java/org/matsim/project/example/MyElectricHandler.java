package org.matsim.project.example;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;

public class MyElectricHandler implements VehicleLeavesTrafficEventHandler, MobsimScopeEventHandler {
	@Inject
	public ElectricFleet electricFleet;

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		Id<Vehicle> vehicleId = event.getVehicleId();
		Id<ElectricVehicle> evId = Id.create(vehicleId, ElectricVehicle.class);

		ElectricVehicle ev = electricFleet.getElectricVehicles().get(evId);
		
		if (ev != null) {
			System.err.println("EV charging state: " + ev.getBattery().getSoc());
		}
	}
}
