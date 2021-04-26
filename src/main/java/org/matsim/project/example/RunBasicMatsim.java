package org.matsim.project.example;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.other.RandomPlanGenerator;


public class RunBasicMatsim {
    //basic matsim example run to make some tests

    public static void main(String[] args) {

        String configFile = "scenarios/equil/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);

        config.controler().setOutputDirectory("outputTestDirectory");
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(0);

        config.network().setInputFile("network.xml");

        //added for the activity "other"
        PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams("other");
        params.setTypicalDuration(3600);
        config.planCalcScore().addActivityParams(params);


        Scenario scenario = ScenarioUtils.loadScenario(config);

        //reduction of the number of agents in the simulation (to go faster in the tests)
		for(int i = 11; i<=100; i++){
			scenario.getPopulation().removePerson(Id.createPersonId(i));
		}

        RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), 10, scenario.getNetwork(),true);

        Controler controler = new Controler(scenario);

        controler.run();
    }
}
