package org.matsim.project;

import com.google.inject.Inject;
import org.apache.commons.lang3.event.EventUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.contrib.ev.MobsimScopeEventHandling;
import org.matsim.contrib.ev.charging.ChargingModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.discharging.AuxDischargingHandler;
import org.matsim.contrib.ev.discharging.DischargingModule;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricFleetModule;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructureModule;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.contrib.ev.stats.EvStatsModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.Injector;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;

public class RunWithEvs3 {
    public static void main(String[] args) {
        //config générale
        String configFile = "scenarios/equil/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);
        config.controler().setOutputDirectory("RunWithEvs3");
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(0);

        //ajout du configGroup EV
        EvConfigGroup evConfigGroup = new EvConfigGroup();
//        evConfigGroup.setVehiclesFile("../../testEvs/additional_input_testEvs/evs.xml");
//        evConfigGroup.setChargersFile("../../testEvs/additional_input_testEvs/chargers.xml");
        evConfigGroup.setVehiclesFile("../../scenarios/equil/evs.xml");
        evConfigGroup.setChargersFile("../../scenarios/equil/chargers.xml");
        config.addModule(evConfigGroup);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        Network network = scenario.getNetwork();



        EvModule evModule = new EvModule(){
            @Override
            public void install(){
                //éléments d'origine
                this.bind(MobsimScopeEventHandling.class).asEagerSingleton();
                this.addControlerListenerBinding().to(MobsimScopeEventHandling.class);
                this.install(new ElectricFleetModule());
                this.install(new ChargingInfrastructureModule());
                this.install(new ChargingModule());
                this.install(new DischargingModule());
                this.install(new EvStatsModule());

                //éléments manquants d'après les erreurs renvoyées
                this.bind(EventsManager.class).toInstance(eventsManager);
                this.bind(Network.class).toInstance(network);
//                this.bind(ElectricFleet.class); //normalement on a install(ElectricFleetModule) donc on devrait pas avoir à faire ça
            }
        };
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

//                this.install(new EvModule());
//                this.bind(EvModule.class);

            }
        };
        controler.addOverridingModule(abstractModule);

        controler.configureQSimComponents(components -> components.addNamedComponent(EvModule.EV_COMPONENT));

        com.google.inject.Injector injector = Injector.createInjector(config, evModule);

        ElectricFleet electricFleet = injector.getInstance(ElectricFleet.class);

        controler.run();

//
//        eventsManager.addHandler(new MyHandler());
//        eventsManager.initProcessing();
//        new MatsimEventsReader(eventsManager).readFile("RunWithEvs3/output_events.xml.gz");


    }



    private static class MyHandler implements VehicleLeavesTrafficEventHandler{
        @Inject ElectricFleet electricFleet;

        public MyHandler( ElectricFleet electricFleet){
            this.electricFleet = electricFleet;
        }

        @Override
        public void handleEvent(VehicleLeavesTrafficEvent vehicleLeavesTrafficEvent) {

            Id<ElectricVehicle> evId = Id.create(vehicleLeavesTrafficEvent.getVehicleId(),ElectricVehicle.class);
            Double soc = electricFleet.getElectricVehicles().get(evId).getBattery().getSoc();

            System.out.println("vehicule" + evId.toString() + "s'arrete avec un SOC de " + soc);

        }
    }
}
