package org.matsim.project.other;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.router.RoutingModule;

import java.util.ArrayList;
import java.util.Random;

public class RandomPlanGenerator {

    static PopulationFactory populationFactory;



    public static Population createRandomPopulation(Population population, int populationSize, Network network, int durationInDay, Boolean reinitializePopulation){
        /*
        Create a random population of populationSize persons in the given network
        /!\ It does not really create a population, but takes a population, eventually reinitializes it,
        /!\ and adds persons in this population
         */
        if(reinitializePopulation==true){
            population.getPersons().clear();
        }

        for(int i=0; i< populationSize; i++){
            population.addPerson(createRandomPerson(Id.createPersonId(i), network, population.getFactory(), durationInDay));
        }


        return population;
    }

    public static Person createRandomPerson(Id<Person> personId, Network network, PopulationFactory populationFactory, int durationInDays){
        /*
        create a person with a random plan in a network
        */
        Person person = populationFactory.createPerson(personId);
        
        person.addPlan(createRandomPlan(getRandomLink(network), getRandomLink(network), getRandomLink(network), populationFactory, durationInDays));
        


        return person;
    }



    public static Plan createRandomPlan(Link homeLink, Link workLink, Link otherActivityLink, PopulationFactory populationFactory, int durationInDays){
        /*
        create daily plan for a person living at homeLink
        with home, work, a potential other activity after work, and home again
        */
        

        int meanLeaveHomeTime = 8*60*60;
        int meanLeaveWorkTime = 18*60*60;
        int meanLeaveEveningActivityTime = 21*60*60;

        Plan plan = populationFactory.createPlan();
        
        for(int dayNumber = 0; dayNumber <= durationInDays; dayNumber++){
            int startTime = dayNumber * 60 * 60 * 24;
            Activity homeMorning = populationFactory.createActivityFromLinkId("h", homeLink.getId());
            homeMorning.setStartTime(startTime);
            homeMorning.setEndTime(startTime + meanLeaveHomeTime + (new Random()).nextGaussian()*20*60);
            plan.addActivity(homeMorning);

            plan.addLeg(populationFactory.createLeg("car"));

            Activity work = populationFactory.createActivityFromLinkId("w", workLink.getId());
            work.setEndTime(startTime + meanLeaveWorkTime + (new Random()).nextGaussian()*20*60);
            plan.addActivity(work);

            plan.addLeg(populationFactory.createLeg("car"));

            if (Math.random() < 0.3){
                Activity backHomeAtNoon = populationFactory.createActivityFromLinkId("h",homeLink.getId());
                backHomeAtNoon.setEndTime(startTime + 12*60*60 +(new Random()).nextGaussian()*10*60);
                plan.addActivity(backHomeAtNoon);

                plan.addLeg(populationFactory.createLeg("car"));

                Activity backToWork = populationFactory.createActivityFromLinkId("w",workLink.getId());
                backToWork.setEndTime(startTime + 14*60*60 +(new Random()).nextGaussian()*10*60);
                plan.addActivity(backToWork);

                plan.addLeg(populationFactory.createLeg("car"));
            }

            //remember to add the activity in the main run
            if(Math.random() < 0.3){
                Activity otherActivity = populationFactory.createActivityFromLinkId("other",otherActivityLink.getId());
                otherActivity.setEndTime(startTime + meanLeaveEveningActivityTime+(new Random()).nextGaussian()*10*60);
                plan.addActivity(otherActivity);

                plan.addLeg(populationFactory.createLeg("car"));
            }
            else if(Math.random() < 0.45){ //0.45 so the probability of this "other" activity is equal to the previous one
                Activity otherActivity = populationFactory.createActivityFromLinkId("other",otherActivityLink.getId());
                otherActivity.setEndTime(startTime + 16*60*60 +(new Random()).nextGaussian()*10*60);
                plan.addActivity(otherActivity);

                plan.addLeg(populationFactory.createLeg("car"));
            }


            Activity homeNight = populationFactory.createActivityFromLinkId("h", homeLink.getId());
            plan.addActivity(homeNight);
        }



        return plan;
    }


    public static Link getRandomLink(Network network){
        ArrayList<Link> links = new ArrayList(network.getLinks().values());
        int randomNumber = (int) (Math.random() * links.size());

        return links.get(randomNumber);

    }



}
