package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.core.api.internal.HasVehicleId;
import org.matsim.vehicles.Vehicle;

public class EmptyBatteryEvent extends Event implements HasPersonId, HasVehicleId {

    private Id<Person> personId;
    private Id<Vehicle> vehicleId;
    private Id<Link> linkId;

    public EmptyBatteryEvent(double time, Id<Person> personId, Id<Vehicle> vehicleId, Id<Link> linkId) {
        super(time);
        this.personId = personId;
        this.vehicleId = vehicleId;
        this.linkId = linkId;
    }

    @Override
    public String getEventType() {
        return "emptyBattery";
    }

    @Override
    public Id<Person> getPersonId() {
        return personId;
    }

    @Override
    public Id<Vehicle> getVehicleId() {
        return vehicleId ;
    }
}