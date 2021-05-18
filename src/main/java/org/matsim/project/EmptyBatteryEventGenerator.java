package org.matsim.project;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.MobsimScopeEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;

public class EmptyBatteryEventGenerator
        implements LinkEnterEventHandler, LinkLeaveEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, MobsimScopeEventHandler {

//    private EventsManager eventsManager;
    private Map<Id<Vehicle>, Id<Person>> vehicleToDriver = new HashMap<>();
//    private ElectricFleet electricFleet;

    @Inject
    public ElectricFleet electricFleet;

    @Inject
    public EventsManager eventsManager;

    /*
    The simulation doesn't run when there is a constructor, I don't understand why
    The first iteration is ok, but at the second one, the following error appears :
    2021-04-21T16:05:55,265 ERROR MatsimRuntimeModifications:81 ERROR --- This is an unexpected shutdown!
    2021-04-21T16:05:55,265 ERROR MatsimRuntimeModifications:84 Shutdown possibly caused by the following Exception:
    java.lang.IllegalStateException: This handler should have been unregistered on AfterMobsimEvent

    So there is no constructur, and it is simply replaced by an injection of the eventManager (see the line above this commented block)
    */
//    @Inject
//    public EmptyBatteryEventGenerator(EventsManager eventsManager) {
//        this.eventsManager = eventsManager;
//        this.eventsManager.addHandler(this);
//    }

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
//               System.err.println("EV n°" + vehicleId.toString() +" has his battery empty");
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

    @Override
    public void handleEvent(PersonLeavesVehicleEvent personLeavesVehicleEvent) {
        vehicleToDriver.remove(personLeavesVehicleEvent.getVehicleId());
    }


    @Override
    public void cleanupAfterMobsim(int iteration) {
        vehicleToDriver.clear();
    }


}
