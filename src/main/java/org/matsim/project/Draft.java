package org.matsim.project;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.handlers.MyActivityEventHandler;

import java.util.Collection;

public class Draft {

    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("scenarios/equil/config.xml");
        config.qsim().setEndTime(48*3600);
//        config.qsim().setSimStarttimeInterpretation( QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime );

        config.controler().setOutputDirectory("output_draft");
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

//        config.planCalcScore().getActivityParams("h").setOpeningTime(0);
//        config.planCalcScore().getActivityParams("w").setOpeningTime(0);
//        config.planCalcScore().getActivityParams("h").setClosingTime(48*3600);
//        config.planCalcScore().getActivityParams("w").setOpeningTime(48*3600);
//        config.planCalcScore().getActivityParams("h").setLatestStartTime(48*3600);
//        config.planCalcScore().getActivityParams("w").setLatestStartTime(48*3600);


//        config.planCalcScore().getActivityParams("h").setOpeningTime(0);
//        config.planCalcScore().getActivityParams("w").setOpeningTime(0);
//        config.planCalcScore().getActivityParams("h").setClosingTime(0);
//        config.planCalcScore().getActivityParams("w").setClosingTime(0);
//        config.planCalcScore().getActivityParams("h").setLatestStartTime(0);
//        config.planCalcScore().getActivityParams("w").setLatestStartTime(0);

        Collection<String> activities = config.planCalcScore().getActivityTypes();
        for (Object e:activities){
            config.planCalcScore().getActivityParams((String)e).setOpeningTime(0);
            config.planCalcScore().getActivityParams((String)e).setClosingTime(0);
            config.planCalcScore().getActivityParams((String)e).setLatestStartTime(0);
        }





        Scenario scenario = ScenarioUtils.loadScenario(config);
        for(int i=2; i<=100; i++){
            scenario.getPopulation().removePerson(Id.createPersonId(i));
        }

        {

            PopulationFactory factory = scenario.getPopulation().getFactory();
            Plan plan = factory.createPlan();


            Activity activity0 = factory.createActivityFromLinkId("h", Id.createLinkId(3));
            activity0.setEndTime(12 * 3600);
            plan.addActivity(activity0);
            Leg leg1 = factory.createLeg("car");
            plan.addLeg(leg1);

            Activity activity2 = factory.createActivityFromLinkId("w", Id.createLinkId(10));
            activity2.setEndTime(26 * 3600);
            plan.addActivity(activity2);



            plan.addLeg(leg1);

            Activity activity1 = factory.createActivityFromLinkId("w", Id.createLinkId(12));
            activity1.setEndTime(32 * 3600);
            plan.addActivity(activity1);

            plan.addLeg(leg1);

            Activity activity3 = factory.createActivityFromLinkId("h", Id.createLinkId(3));
            plan.addActivity(activity3);


            Person person = factory.createPerson(Id.createPersonId(1000));

            person.addPlan(plan);

            scenario.getPopulation().addPerson(person);

        }

//        RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), 2, scenario.getNetwork(), 2, true);



        Controler controler = new Controler(scenario);


        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                addEventHandlerBinding().toInstance(new MyActivityEventHandler());
            }
        });

        controler.run();
    }
}
