package org.matsim.project;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.core.controler.Controler;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ElectricHandlerWithFileWriter
        implements VehicleLeavesTrafficEventHandler, VehicleEntersTrafficEventHandler, MobsimScopeEventHandler{

    @Inject
    public ElectricFleet electricFleet;

    /*
    Data collection :
        - parking duration at the stop locations
        - state of charge at the stop locations
        - number of parking events at the location
        - number of distinct vehicles parking at the location
        - inter dependence of the locations : see later
     */
    private static Map<Id<Link>, ArrayList<Double>> parkingDurationMap = new HashMap<>(); //
    private static Map<Id<Link>, ArrayList<Double>> socMap = new HashMap<>(); //
    private static Map<Id<Link>,Map<Id<Vehicle>,Integer>> stopCounterMap = new HashMap<>(); // <location, <vehicle stopping at the location, number of stops>>

    private static Map<Id<Vehicle>, Double> stopTimeMap = new HashMap<>(); // last stop of the vehicles, to calculate the parking duration

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent vehicleLeavesTrafficEvent) {
        Id<Vehicle> vehicleId = vehicleLeavesTrafficEvent.getVehicleId();
        Id<Link> linkId = vehicleLeavesTrafficEvent.getLinkId();
        Double stopTime = vehicleLeavesTrafficEvent.getTime();
        Id<ElectricVehicle> evId = Id.create(vehicleId, ElectricVehicle.class);
        Double soc = null;
        if (electricFleet.getElectricVehicles().get(evId) != null){// in case the vehicle is not electric
            soc = electricFleet.getElectricVehicles().get(evId).getBattery().getSoc();
        }

        //stop time
        stopTimeMap.put(vehicleId,stopTime);

        //stop counter
        if (stopCounterMap.get(linkId) == null){
            stopCounterMap.put(linkId, new HashMap<>());
        }
        if (stopCounterMap.get(linkId).get(vehicleId) == null){
            stopCounterMap.get(linkId).put(vehicleId,1);
        } else{
            Integer count = stopCounterMap.get(linkId).get(vehicleId);
            stopCounterMap.get(linkId).put(vehicleId,count+1);
        }

        //state of charge
        if(soc != null){
            if (socMap.get(linkId) == null){
                socMap.put(linkId, new ArrayList<>());
            }
            socMap.get(linkId).add(soc);
        }

        if(soc !=null){
            System.err.println("EV "+ vehicleId.toString() + " stops at " + stopTime+
                    " at the link n°"+ linkId + " with a SoC of " + soc);
        } else {
            System.err.println("ICEV "+ vehicleId.toString() + " stops at " + stopTime+
                    " at link n°"+ linkId);
        }


    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent vehicleEntersTrafficEvent) {
        Id<Vehicle> vehicleId = vehicleEntersTrafficEvent.getVehicleId();
        Id<Link> linkId = vehicleEntersTrafficEvent.getLinkId();
        Double startTime = vehicleEntersTrafficEvent.getTime();
        //this may be useful later if differentiation between EV and ICEV
//        Id<ElectricVehicle> evId = Id.create(vehicleId, ElectricVehicle.class);
//        Double soc = null;
//        if (electricFleet.getElectricVehicles().get(evId) != null){// in case the vehicle is not electric
//            soc = electricFleet.getElectricVehicles().get(evId).getBattery().getSoc();
//        }

        Double stopTime = stopTimeMap.remove(vehicleId);
        if(stopTime != null){
            double parkingDuration = startTime - stopTime;
            parkingDurationMap.putIfAbsent(linkId, new ArrayList<>());
            parkingDurationMap.get(linkId).add(parkingDuration);
            System.err.println("Vehicle "+vehicleId + " re-enters traffic after a stop of " + parkingDuration+"s");
        } else {
            System.err.println("First entry in traffic for vehicle "+ vehicleId);
        }
    }
}
