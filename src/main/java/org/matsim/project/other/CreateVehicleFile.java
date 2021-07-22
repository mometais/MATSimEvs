package org.matsim.project.other;

import com.google.common.collect.ImmutableList;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.fleet.*;
import org.matsim.utils.objectattributes.attributable.Attributes;


import java.util.*;
import java.util.stream.Stream;


public class CreateVehicleFile {
//    public static void main(String[] args) {
//        Population population = new Population() {
//            @Override
//            public Attributes getAttributes() {
//                return null;
//            }
//
//            @Override
//            public PopulationFactory getFactory() {
//                return null;
//            }
//
//            @Override
//            public String getName() {
//                return null;
//            }
//
//            @Override
//            public void setName(String s) {
//
//            }
//
//            @Override
//            public Map<Id<Person>, ? extends Person> getPersons() {
//                return null;
//            }
//
//            @Override
//            public void addPerson(Person person) {
//
//            }
//
//            @Override
//            public Person removePerson(Id<Person> id) {
//                return null;
//            }
//        };
//        createAllDefaultVehicleFile(population, "testEvsFile.xml");
//    }

    public static String createAllDefaultVehicleFile(Population population, String filename) throws InterruptedException {
        //create an EV for all the persons in the simulation
        int nbVehicles = population.getPersons().size();
//        System.err.println(nbVehicles);
//        Thread.sleep(1000);
//        int nbVehicles = 5;

        ArrayList vehicleSpecifications = new ArrayList();

        String defaultVehicleType = "defaultVehicleType";
        double defaultBatteryCapacity =  EvUnits.kWh_to_J(60); //ElectricFleetWriter takes battery capacities in J, it's stupid but that's how it is
        double defaultInitialSoc = defaultBatteryCapacity;
        ImmutableList<String> defaultChargerTypes = ImmutableList.copyOf(Arrays.asList("default"));


        for(int i = 0; i<= nbVehicles; i++){
            ImmutableElectricVehicleSpecification.Builder builder = ImmutableElectricVehicleSpecification.newBuilder();
            builder.id(Id.create(i, ElectricVehicle.class));
            builder.vehicleType(defaultVehicleType);
            builder.chargerTypes(defaultChargerTypes);
            builder.batteryCapacity(defaultBatteryCapacity);
            builder.initialSoc(defaultInitialSoc);

            ElectricVehicleSpecification specification = builder.build();
//            System.out.println(specification.getBatteryCapacity());

            vehicleSpecifications.add(specification);
        }


        ElectricFleetWriter electricFleetWriter = new ElectricFleetWriter(vehicleSpecifications.stream());
        electricFleetWriter.setPrettyPrint(true);
        electricFleetWriter.write("scenarios/equil/"+filename);
        return filename;
    }

    public static String createAllDefaultVehicleFile(int populationSize, String filename){
        //create an EV for all the persons in the simulation


        ArrayList vehicleSpecifications = new ArrayList();

        String defaultVehicleType = "defaultVehicleType";
        double defaultBatteryCapacity =  EvUnits.kWh_to_J(60); //ElectricFleetWriter takes battery capacities in J, it's stupid but that's how it is
        double defaultInitialSoc = defaultBatteryCapacity;
        ImmutableList<String> defaultChargerTypes = ImmutableList.copyOf(Arrays.asList("default"));


        for(int i = 0; i< populationSize; i++){
            ImmutableElectricVehicleSpecification.Builder builder = ImmutableElectricVehicleSpecification.newBuilder();
            builder.id(Id.create(i, ElectricVehicle.class));
            builder.vehicleType(defaultVehicleType);
            builder.chargerTypes(defaultChargerTypes);
            builder.batteryCapacity(defaultBatteryCapacity);
            builder.initialSoc(defaultInitialSoc);

            ElectricVehicleSpecification specification = builder.build();
//            System.out.println(specification.getBatteryCapacity());

            vehicleSpecifications.add(specification);
        }


        ElectricFleetWriter electricFleetWriter = new ElectricFleetWriter(vehicleSpecifications.stream());
        electricFleetWriter.setPrettyPrint(true);
        electricFleetWriter.write("scenarios/equil/"+filename);
        return filename;
    }
}
