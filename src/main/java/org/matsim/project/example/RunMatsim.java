package org.matsim.project.example;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

public class RunMatsim {

    public static void main(String[] args) {

        Config config = ConfigUtils.loadConfig("scenarios/equil/config.xml");
        config.controler().setOutputDirectory("outputExample");
        config.controler().setLastIteration(10);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.network().setInputFile("network.xml");

        config.plans().setInputFile("plans100.xml");

        Scenario scenario = ScenarioUtils.loadScenario(config);

        scenario.getNetwork().removeLink(Id.createLinkId(14));



        Controler controler = new Controler(scenario);

        controler.run();
    }


}
