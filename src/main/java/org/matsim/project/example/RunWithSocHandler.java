package org.matsim.project.example;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;

public class RunWithSocHandler {
    public static void main(String[] args) {
        String configFile = "scenarios/equil/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);

        String outputDirectory = "output";
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
        config.controler().setLastIteration(5);


        EvConfigGroup evConfigGroup = new EvConfigGroup();
        evConfigGroup.setChargersFile("../../scenarios/equil/testChargers.xml");
        evConfigGroup.setVehiclesFile("../../scenarios/equil/testEvs.xml");
        evConfigGroup.setTimeProfiles(true);
        config.addModule(evConfigGroup);

        Scenario scenario = ScenarioUtils.loadScenario(config);


        Controler controler = new Controler( scenario ) ;

        //###############################################
        //###Additional modules added to the controler###
        //###############################################

        //EV Module
        EvModule evModule = new EvModule();
        controler.addOverridingModule(evModule);
        controler.configureQSimComponents(components -> components.addNamedComponent(EvModule.EV_COMPONENT));


        //Event handler for getting EVs SoC
        controler.addOverridingQSimModule(new AbstractQSimModule() {
            @Override
            protected void configureQSim() {
				addMobsimScopeEventHandlerBinding().to(MyElectricHandler.class);
            }
        });



        controler.run();

    }
}
