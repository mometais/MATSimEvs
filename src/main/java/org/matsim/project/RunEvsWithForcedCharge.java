package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.other.CreateChargerFile;
import org.matsim.project.other.CreateVehicleFile;
import org.matsim.project.other.RandomPlanGenerator;

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

        String configFile = "scenarios/equil/config.xml";
        String networkFile = "brandenburg-motorways.xml.gz";
        String populationFile = "plans100.xml";

        String chargerFile = "testChargers.xml";
        String evFile = "testEvs.xml";

        String outputDirectory = "output_forced_charge";


        Config config = ConfigUtils.loadConfig(configFile);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setLastIteration(100);

        config.network().setInputFile(networkFile);
        config.plans().setInputFile(populationFile);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        EvConfigGroup evConfigGroup = new EvConfigGroup();
        evConfigGroup.setChargersFile(CreateChargerFile.createDefaultChargersForNLinks((int)(scenario.getNetwork().getLinks().size()/2), scenario.getNetwork(), chargerFile));
        evConfigGroup.setVehiclesFile(CreateVehicleFile.createAllDefaultVehicleFile(scenario.getPopulation(), evFile));
        evConfigGroup.setTimeProfiles(true);
        config.addModule(evConfigGroup);

        //generate a random population with random plans and add it in the simulation
        RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), 30, scenario.getNetwork(), 1, true);


//        scenario = ScenarioUtils.loadScenario(config);

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
                addRoutingModuleBinding(TransportMode.car).toProvider(new EvNetworkRoutingProvider(TransportMode.car));
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

        controler.run();
    }


}
