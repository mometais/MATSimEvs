package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.other.CreateChargerFile;


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


    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("scenarios/equil/config.xml");
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setOutputDirectory("output_forced_charge");
        config.controler().setLastIteration(10);

        EvConfigGroup evModule = new EvConfigGroup();
        evModule.setChargersFile(CreateChargerFile.createDefaultChargerForAllLinks());
        evModule.setVehiclesFile();
        evModule.setTimeProfiles(true);
        config.addModule(evModule);


        Scenario scenario = ScenarioUtils.loadScenario(config);


        Controler controler = new Controler(scenario);

        controler.run();
    }


}
