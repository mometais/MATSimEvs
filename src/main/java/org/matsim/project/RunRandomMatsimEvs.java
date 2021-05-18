/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructure;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructureModule;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.QSim;


import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.other.CreateChargerFile;
import org.matsim.project.other.RandomPlanGenerator;

import static org.matsim.project.other.CreateChargerFile.createDefaultChargerForAllLinks;
import static org.matsim.project.other.CreateChargerFile.createDefaultChargersForNLinks;

/**
 * @author nagel
 *
 */
public class RunRandomMatsimEvs {

    /*
    Run several simulations with different random scenarios
     */

    public static void main(String[] args) {

        int simulationNumber = 0;

        String outputDirectory = "output_multiple_simulations1/output";
        int iterationPerScenario = 0;
        String inputNetworkFile = "brandenburg-motorways.xml.gz";
        Network inputNetwork = NetworkUtils.readNetwork("scenarios/equil/"+inputNetworkFile);
        String inputChargerFile = createDefaultChargersForNLinks(200, inputNetwork, "simulationChargers.xml"  );
//        String inputChargerFile = createDefaultChargerForAllLinks(inputNetwork, "chargerAllLinks.xml");
        String inputEvFile = "testEvs2.xml";
        int populationSize = 50;
        int simulationDuration = 2; //duration of the simulation in days

        for(int i = 0; i<= simulationNumber; i++){
            runEvs(outputDirectory+i,  iterationPerScenario,  inputNetworkFile,  inputChargerFile,  inputEvFile,  populationSize,  simulationDuration);
        }



    }

    public static void runEvs(String outputDirectory, int iterationPerScenario, String inputNetworkFile,
            String inputChargerFile, String inputEvFile, int populationSize, int simulationDuration){

//        String configFile = "scenarios/equil/config.xml";
        String configFile = "scenarios/equil/berlin-v5.5-1pct.output_config_reduced.xml";
        Config config = ConfigUtils.loadConfig(configFile);

        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
        config.controler().setLastIteration(iterationPerScenario);
        config.network().setInputFile(inputNetworkFile);

//        config.planCalcScore().getActivityParams("h").setMinimalDuration(3600*3);
//        config.planCalcScore().getActivityParams("w").setMinimalDuration(3600*3);
//        config.planCalcScore().getActivityParams("car charging interaction").setScoringThisActivityAtAll(true);

//        config.planCalcScore().getActivityParams("car interaction").setTypicalDuration(0);


        config.qsim().setEndTime(3600*24*simulationDuration);

//        config.plans().setInputFile("../../input_plans.xml");



        EvConfigGroup evConfigGroup = new EvConfigGroup();
        evConfigGroup.setChargersFile(inputChargerFile);
        evConfigGroup.setVehiclesFile(inputEvFile);
        evConfigGroup.setTimeProfiles(true);
        evConfigGroup.setChargeTimeStep(5);
        evConfigGroup.setAuxDischargeTimeStep(10);
        config.addModule(evConfigGroup);


        //activity "other" taken into account in the scoring function (generated in the random plans)
        PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams("other");
        params.setTypicalDuration(3600);
        config.planCalcScore().addActivityParams(params);


        Scenario scenario = ScenarioUtils.loadScenario(config);


        //generate a random population with random plans and add it in the simulation
		RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), populationSize, scenario.getNetwork(), simulationDuration, true);

        //writing input plans file to check the random plans generated
//		PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation());
//		populationWriter.write("input_plans.xml");


        Controler controler = new Controler( scenario ) ;

        //###############################################
        //###Additional modules added to the controler###
        //###############################################

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

