package org.matsim.project.handlers;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.contrib.ev.infrastructure.Charger;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructureSpecification;
import org.matsim.core.scoring.EventsToActivities;
import org.matsim.core.scoring.PersonExperiencedActivity;

public class MyActivityEventHandler implements ActivityStartEventHandler, ActivityEndEventHandler {
    @Inject ChargingInfrastructureSpecification chargingInfrastructureSpecification;
    //just to see what activities are performed
    @Override
    public void handleEvent(ActivityStartEvent activity) {
        System.err.println("Agent "+ activity.getPersonId()+ ": Activity "+ activity.getActType()+ " starts at "+ fancyTime(activity.getTime()));



        for(Id<Charger> key : chargingInfrastructureSpecification.getChargerSpecifications().keySet()) {
            if (activity.getLinkId() == chargingInfrastructureSpecification.getChargerSpecifications().get(key).getLinkId()) { // Si on a un chargeur à l'endroit, chercher comment c'est construit)
                System.err.println("Charger available at destination for agent "+activity.getPersonId());
            }
        }
    }

    @Override
    public void handleEvent(ActivityEndEvent activity) {
        System.err.println("Agent "+ activity.getPersonId()+ ": Activity "+ activity.getActType()+ " ends at "+ fancyTime(activity.getTime()));

    }


    public String fancyTime(double time){
        int ss = (int)time%60;
        int mm = (int)(time%3600)/60;
        int hh =(int) (time/3600);
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }


}