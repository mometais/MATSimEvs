package org.matsim.project.handlers;

import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.scoring.EventsToActivities;
import org.matsim.core.scoring.PersonExperiencedActivity;

public class MyActivityEventHandler implements ActivityStartEventHandler, ActivityEndEventHandler {
    //just to see what activities are performed
    @Override
    public void handleEvent(ActivityStartEvent activity) {
        System.err.println("Activity "+ activity.getActType()+ " starts at "+ activity.getTime()+ " for agent " + activity.getPersonId());
    }

    @Override
    public void handleEvent(ActivityEndEvent activity) {
        System.err.println("Activity "+ activity.getActType()+ " ends at "+ activity.getTime()+ " for agent " + activity.getPersonId());

    }
}