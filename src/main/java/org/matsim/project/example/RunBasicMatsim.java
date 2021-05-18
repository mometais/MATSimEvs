package org.matsim.project.example;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.other.CreateChargerFile;
import org.matsim.project.other.RandomPlanGenerator;


public class RunBasicMatsim {
    //basic matsim example run to make some tests

    public static void main(String[] args) {

        String configFile = "scenarios/equil/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);

        String outputDirectory = "outputTestDirectory";
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(0);

        config.qsim().setStartTime(0);
//        config.qsim().
        config.qsim().setEndTime(86400);

//        String networkFile = "network.xml";
//        config.network().setInputFile(networkFile);

        //new activity "other" added to the simulation (for the random plan generator)
        PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams("other");
        params.setTypicalDuration(3600);
        config.planCalcScore().addActivityParams(params);


        Scenario scenario = ScenarioUtils.loadScenario(config);

        //reduction of the number of agents in the simulation (to go faster in the tests)
//		for(int i = 11; i<=100; i++){
//			scenario.getPopulation().removePerson(Id.createPersonId(i));
//		}

		//plans are replaced by random plans
        RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), 10, scenario.getNetwork(), 2,true);

        Controler controler = new Controler(scenario);

        // allows to check plans with which the controler runs
//        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation());
//        populationWriter.write("input_plans.xml");

        //create a charger file with a charger at each link (test)
//        CreateChargerFile.createDefaultChargerForAllLinks(scenario.getNetwork(), "testChargerAllLinks.xml");

        //write config just before the run
        ConfigWriter configWriter = new ConfigWriter(config, ConfigWriter.Verbosity.minimal);
        configWriter.write("input_config.xml");






        controler.run();
    }
}
