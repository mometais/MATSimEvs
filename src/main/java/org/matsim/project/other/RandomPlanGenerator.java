package org.matsim.project.other;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;

import java.util.Random;

public class RandomPlanGenerator {

    PopulationFactory populationFactory;
    Population population;
    Network network;

    int meanLeaveHomeTime = 8*60*60;
    int meanLeaveWorkTime = 18*60*60;
    int meanLeaveEveningActivityTime = 21*60*60;


    public Population createRandomPopulation(int populationSize){
        Population randomPopulation = population;

        return randomPopulation;
    }

    public Person createRandomPerson(Link homeLink){
        /*
        create a person with a random plan leaving at the link homeLink
        */
        Person person = populationFactory.createPerson(Id.createPersonId(0)); //change this to create new Id at each time




        return person;
    }



    public Plan createRandomPlan(Link homeLink, Link workLink, Link otherActivityLink){
        /*
        create daily plan for a person living at homeLink
        with home, work, a potential other activity after work, and home again
        */
        Plan plan = populationFactory.createPlan();

        Activity homeMorning = populationFactory.createActivityFromLinkId("home", homeLink.getId());
        homeMorning.setStartTime(0);
        homeMorning.setEndTime(meanLeaveHomeTime + (new Random()).nextGaussian()*20*60);
        plan.addActivity(homeMorning);

        Activity work = populationFactory.createActivityFromLinkId("work", workLink.getId());
        work.setEndTime(meanLeaveWorkTime + (new Random()).nextGaussian()*20*60);
        plan.addActivity(work);

        if(Math.random() < .3){
            Activity otherActivity = populationFactory.createActivityFromLinkId("other",otherActivityLink.getId());
            otherActivity.setEndTime(meanLeaveEveningActivityTime+(new Random()).nextGaussian()*10*60);
            plan.addActivity(otherActivity);
        }

        Activity homeNight = populationFactory.createActivityFromLinkId("home", homeLink.getId());
        plan.addActivity(homeNight);

        return plan;
    }
}
