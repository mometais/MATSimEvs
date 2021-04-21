package org.matsim.project;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;

public class EmptyBatteryEventGenerator implements LinkEnterEventHandler, LinkLeaveEventHandler, PersonEntersVehicleEventHandler, MobsimScopeEventHandler {

    private EventsManager eventsManager;
    private Map<Id<Vehicle>, Id<Person>> vehicleToDriver = new HashMap<>();
//    private ElectricFleet electricFleet;

    @Inject
    public ElectricFleet electricFleet;

    @Inject
    public EmptyBatteryEventGenerator(EventsManager eventsManager) {
        this.eventsManager = eventsManager;
        this.eventsManager.addHandler(this);
    }

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        Id<Vehicle> vehicleId = linkEnterEvent.getVehicleId();
        Id<ElectricVehicle> evId = Id.create(vehicleId, ElectricVehicle.class);
        if (electricFleet.getElectricVehicles().get(evId) != null){// in case the vehicle is not electric
           Double soc = electricFleet.getElectricVehicles().get(evId).getBattery().getSoc();
           if (soc == 0){
               double time = linkEnterEvent.getTime();
               Id<Person> personId = vehicleToDriver.get(linkEnterEvent.getVehicleId());
               Id<Link> linkId = linkEnterEvent.getLinkId();
               eventsManager.processEvent(new EmptyBatteryEvent(time, personId,vehicleId, linkId )); //EmptyBatteryEvent generated
               System.err.println("EV n°" + vehicleId.toString() +" has is battery empty");
           }
        }
    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {

    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent personEntersVehicleEvent) {
        vehicleToDriver.put(personEntersVehicleEvent.getVehicleId(), personEntersVehicleEvent.getPersonId());
    }
}
