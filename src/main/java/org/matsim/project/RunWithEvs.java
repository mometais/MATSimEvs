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
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.Injector;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author nagel
 *
 */
public class RunWithEvs {

	public static void main(String[] args) {
		String configFile = "scenarios/equil/config.xml";
		Config config = ConfigUtils.loadConfig(configFile);

		config.controler().setOutputDirectory("output");
		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.controler().setLastIteration(0);

		EvConfigGroup evConfigGroup = new EvConfigGroup();
		evConfigGroup.setChargersFile("../../scenarios/equil/chargers.xml");
		evConfigGroup.setVehiclesFile("../../scenarios/equil/evs.xml");
		config.addModule(evConfigGroup);

		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;

		Controler controler = new Controler( scenario ) ;

		EvModule evModule = new EvModule();
		controler.addOverridingModule(evModule);

		AbstractModule abstractModule = new AbstractModule() {
			@Override
			public void install() {
				addRoutingModuleBinding(TransportMode.car).toProvider(new EvNetworkRoutingProvider(TransportMode.car));
				installQSimModule(new AbstractQSimModule() {
					@Override
					protected void configureQSim() {
						bind(VehicleChargingHandler.class).asEagerSingleton();
						bind(MobsimScopeEventHandler.class).to(VehicleChargingHandler.class);
					}
				});
			}
		};
		controler.addOverridingModule(abstractModule);
		controler.configureQSimComponents(components -> components.addNamedComponent(EvModule.EV_COMPONENT));

		

		
		controler.run();
	}
	
}
