package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.*;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.events.EmptyBatteryEventGenerator;
import org.matsim.project.handlers.ElectricHandlerWithFileWriter;
import org.matsim.project.handlers.MyActivityEventHandler;
import org.matsim.project.other.CreateChargerFile;
import org.matsim.project.other.CreateVehicleFile;
import org.matsim.project.other.RandomPlanGenerator;
import org.matsim.project.routing.MyEvNetworkRoutingProvider;
import org.matsim.project.scoring.EVChargingScoringFunctionFactory;
//import org.matsim.contrib.common.util.LoggerUtils;


import java.util.*;
//import java.util.logging.Logger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import static org.matsim.core.config.ConfigUtils.loadConfig;


public class RunEvsWithForcedCharge {
    /*
    The goal is to systematically charge EVs if they stop where a charger is available
    Several ways considered :
     - modify activities, e.g have an attribute "charging" for the activity
     - try to parallelize activities (an agent can charge and do an other activity at the same time) : almost equivalent
     to the previous option, and probably more complicated because charging and the other activity must have the same duration
     - Create a custom EvRoutingModule (maybe a bit hard, but probably the most efficient way to do, especially if we want
     to change charging strategy after that. See the edrt module for clues)
            - One of the option considered for routing module is to prohibit vehicles to run if their battery is empty,
             instead of penalizing empty battery (penalize did not work, they didn't reroute to find a charger, and even
             didn't charge at the chargers they could have)
            - Charging is for the vehicle, score is for agents... Can be a problem
     */


    public static void main(String[] args) throws InterruptedException {

        //disable useless loggers
//        Logger.getRootLogger().setLevel(Level.WARN); //don't work ??
        Logger log1 = Logger.getLogger( EventsManagerImpl.class ) ;
        log1.setLevel(Level.WARN);
        Logger log2 = Logger.getLogger( Injector.class ) ;
        log2.setLevel(Level.WARN);
        Logger log3 = Logger.getLogger( ControlerUtils.class ) ;
        log3.setLevel(Level.WARN);
        Logger log4 = Logger.getLogger(QSim.class ) ;
        log4.setLevel(Level.WARN);




        String configFile = "scenarios/equil/config.xml";
        String networkFile = "brandenburg-motorways.xml.gz";
        String populationFile = "plans100.xml";

        String chargerFile = "testChargers.xml";
        String evFile = "testEvs.xml";

        String outputDirectory = "output_forced_charge";

        int populationSize = 10;

        int durationInDays = 1;


        Config config = ConfigUtils.loadConfig(configFile);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setLastIteration(10);

        config.network().setInputFile(networkFile);
        config.plans().setInputFile(populationFile);
        config.qsim().setEndTime(durationInDays*24*3600);
//        System.out.println("Simulation end time : "+ config.qsim().getEndTime());

        PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams("other");
        params.setTypicalDuration(3600);
        config.planCalcScore().addActivityParams(params);

        Collection<String> activities = config.planCalcScore().getActivityTypes();
        for (Object e:activities){
            config.planCalcScore().getActivityParams((String)e).setOpeningTime(0);
            config.planCalcScore().getActivityParams((String)e).setClosingTime(durationInDays*24*3600);
            config.planCalcScore().getActivityParams((String)e).setLatestStartTime(durationInDays*24*3600);
        }


        Scenario scenario = ScenarioUtils.loadScenario(config);

        EvConfigGroup evConfigGroup = new EvConfigGroup();
        int linkNumber = scenario.getNetwork().getLinks().size();
//        evConfigGroup.setChargersFile(CreateChargerFile.createDefaultChargersForNLinks((int) (scenario.getNetwork().getLinks().size() / 10), scenario.getNetwork(), chargerFile));
        evConfigGroup.setChargersFile(CreateChargerFile.createDefaultChargersForNLinks((int) (linkNumber/3), scenario.getNetwork(), chargerFile));
        evConfigGroup.setVehiclesFile(CreateVehicleFile.createAllDefaultVehicleFile(populationSize, evFile));
        evConfigGroup.setTimeProfiles(true);
        config.addModule(evConfigGroup);


        //generate a random population with random plans and add it in the simulation
        RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), populationSize, scenario.getNetwork(), durationInDays, true);

        /*
        //test plan with mandatory charging

        PopulationFactory populationFactory = scenario.getPopulation().getFactory();
        Plan plan = populationFactory.createPlan();
        Activity homeMorning = populationFactory.createActivityFromLinkId("h", Id.createLinkId("545313103_0"));
        homeMorning.setStartTime(0);
        homeMorning.setEndTime(8 * 3600);
        plan.addActivity(homeMorning);
        plan.addLeg(populationFactory.createLeg("car"));
        Activity work = populationFactory.createActivityFromLinkId("w", Id.createLinkId("145075449_0"));
        work.setEndTime(18 * 3600);
        plan.addActivity(work);
        plan.addLeg(populationFactory.createLeg("car"));
        Activity homeNight = populationFactory.createActivityFromLinkId("h", Id.createLinkId("545313103_0"));
        plan.addActivity(homeNight);

        Person person = populationFactory.createPerson(Id.createPersonId("1"));
        person.addPlan(plan);

        scenario.getPopulation().getPersons().clear();
        scenario.getPopulation().addPerson(person);

         */

        /*
        //test plan with only activities the 2nd day

            PopulationFactory populationFactory = scenario.getPopulation().getFactory();
            Plan plan = populationFactory.createPlan();
            Activity homeMorning = populationFactory.createActivityFromLinkId("h", Id.createLinkId("545313103_0"));
            homeMorning.setStartTime(24*3600);
            homeMorning.setEndTime(32 * 3600);
            plan.addActivity(homeMorning);
            plan.addLeg(populationFactory.createLeg("car"));
            Activity work = populationFactory.createActivityFromLinkId("w", Id.createLinkId("145075449_0"));
            work.setEndTime(42 * 3600);
            plan.addActivity(work);
            plan.addLeg(populationFactory.createLeg("car"));

            Activity work2 = populationFactory.createActivityFromLinkId("w", Id.createLinkId("545313103_0"));
            work2.setEndTime(51*3600);
            plan.addActivity(work2);
            plan.addLeg(populationFactory.createLeg("car"));

            Activity homeNight = populationFactory.createActivityFromLinkId("h", Id.createLinkId("545313103_0"));
            plan.addActivity(homeNight);

            Person person = populationFactory.createPerson(Id.createPersonId("1"));
            person.addPlan(plan);

            scenario.getPopulation().addPerson(person);
        */

        /*
        //test plan working on the simple example : don't work here

            for(int i=2; i<=100; i++){
                scenario.getPopulation().removePerson(Id.createPersonId(i));
            }

            PopulationFactory factory = scenario.getPopulation().getFactory();
            Plan plan = factory.createPlan();

            Link homelink = RandomPlanGenerator.getRandomLink(scenario.getNetwork());
            Link work1link = RandomPlanGenerator.getRandomLink(scenario.getNetwork());
            Link work2link = RandomPlanGenerator.getRandomLink(scenario.getNetwork());


            Activity activity0 = factory.createActivityFromLinkId("h", homelink.getId() );
            activity0.setEndTime(12*3600);
            plan.addActivity(activity0);
            Leg leg1 = factory.createLeg("car");
            plan.addLeg(leg1);

            Activity activity2 = factory.createActivityFromLinkId("w", work1link.getId());
            activity2.setEndTime(26*3600);
            plan.addActivity(activity2);

            plan.addLeg(leg1);

            Activity activity1 = factory.createActivityFromLinkId("w", work2link.getId());
            activity1.setEndTime(32*3600);
            plan.addActivity(activity1);

            plan.addLeg(leg1);

            Activity activity3 = factory.createActivityFromLinkId("h", homelink.getId());
            plan.addActivity(activity3);



            Person person = factory.createPerson(Id.createPersonId(1000));

            person.addPlan(plan);

            scenario.getPopulation().addPerson(person);

        */

        Controler controler = new Controler(scenario);

        //Scoring function for EV charging
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                this.bindScoringFunctionFactory().toInstance(new EVChargingScoringFunctionFactory(scenario));
            }
        });

        //EV Module
        EvModule evModule = new EvModule();
        controler.addOverridingModule(evModule);


        //default EV modules to add (cf original ev example)
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                addRoutingModuleBinding(TransportMode.car).toProvider(new MyEvNetworkRoutingProvider(TransportMode.car));
                installQSimModule(new AbstractQSimModule() {
                    @Override
                    protected void configureQSim() {
                        bind(VehicleChargingHandler.class).asEagerSingleton();
                        addMobsimScopeEventHandlerBinding().to(VehicleChargingHandler.class);
                    }
                });
            }
        });
        controler.configureQSimComponents(components -> components.addNamedComponent(EvModule.EV_COMPONENT));

        //Event handler for getting EVs SoC
        controler.addOverridingQSimModule(new AbstractQSimModule() {
            @Override
            protected void configureQSim() {
                addMobsimScopeEventHandlerBinding().to(ElectricHandlerWithFileWriter.class);
            }
        });


        //adding EmptyBatteryEvents in the simulation (to penalize empty battery).
        controler.addOverridingQSimModule(new AbstractQSimModule() {
            @Override
            protected void configureQSim() {
                addMobsimScopeEventHandlerBinding().to(EmptyBatteryEventGenerator.class);
            }
        });

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                this.addEventHandlerBinding().toInstance((EventHandler) new MyActivityEventHandler());
            }
        });


        controler.run();


    }


}
