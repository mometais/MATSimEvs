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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.project.events.EmptyBatteryEventGenerator;
import org.matsim.project.handlers.ElectricHandlerWithFileWriter;
import org.matsim.project.other.RandomPlanGenerator;
import org.matsim.project.scoring.EVChargingScoringFunctionFactory;

/**
 * @author nagel
 *
 */
public class RunWithEvs {

	public static void main(String[] args) {
		String configFile = "scenarios/equil/config.xml";
		Config config = ConfigUtils.loadConfig(configFile);

//		String outputDirectory = "output";
		String outputDirectory = "output2";
		config.controler().setOutputDirectory(outputDirectory);
		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setLastIteration(0);
		config.network().setInputFile("brandenburg-motorways.xml.gz");
//		config.plans().setInputFile("examplefiles/plansEvExample.xml");

//		config.strategy().setMaxAgentPlanMemorySize(0);

		config.qsim().setEndTime(86400*1.5);




		EvConfigGroup evConfigGroup = new EvConfigGroup();
		evConfigGroup.setChargersFile("../../scenarios/equil/testChargers.xml");
//		evConfigGroup.setChargersFile("../../scenarios/equil/testChargerAllLinks.xml");
		evConfigGroup.setVehiclesFile("../../scenarios/equil/testEvs.xml");
		evConfigGroup.setTimeProfiles(true);
		evConfigGroup.setChargeTimeStep(5);
		evConfigGroup.setAuxDischargeTimeStep(10);
		config.addModule(evConfigGroup);


		//activity "other" taken into account in the scoring function (generated in the random plans)
		PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams("other");
		params.setTypicalDuration(3600);
		config.planCalcScore().addActivityParams(params);


		Scenario scenario = ScenarioUtils.loadScenario(config);

		//reduction of the number of agents in the simulation (to go faster in the tests)
//		for(int i = 3; i<=100; i++){
//			scenario.getPopulation().removePerson(Id.createPersonId(i));
//		}

		//generate a random population with random plans and add it in the simulation
		RandomPlanGenerator.createRandomPopulation(scenario.getPopulation(), 15, scenario.getNetwork(), 2,true);

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
//				addMobsimScopeEventHandlerBinding().to(MyElectricHandler.class);
			}
		});


		//adding EmptyBatteryEvents in the simulation (to penalize empty battery).
		//Have to be done in a QSimModule and not simply in an AbstractModule because EV battery SoC are needed
		controler.addOverridingQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				addMobsimScopeEventHandlerBinding().to(EmptyBatteryEventGenerator.class);
			}
		});



		controler.run();
	}
	
}

