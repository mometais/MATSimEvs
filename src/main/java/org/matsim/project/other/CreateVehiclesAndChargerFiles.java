package org.matsim.project.other;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.util.CSVLineBuilder;
import org.matsim.contrib.util.CompactCSVWriter;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

class CreateEvsAndChargerFiles{

    public static void main(String[] args) throws IOException {

        //charger file
        ArrayList links = new ArrayList();
        for(int i =1; i<=20; i++){
            links.add(i);
        }
        createChargersFile("scenarios/equil/testChargers", links);

        //vehicle file
        ArrayList vehicles = new ArrayList();
        for(int i = 1; i<=10; i++){
            vehicles.add(i);
        }
        createEvsFile("scenarios/equil/testEvs", vehicles, 5);

    }

    //create default 100kW chargers with 5 plugs
    public static void createChargersFile(String name, ArrayList linkIds) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(name+".xml"));
        writer.append("<!DOCTYPE chargers SYSTEM \"http://matsim.org/files/dtd/chargers_v1.dtd\">\n\n<chargers>");
        for(int i = 0; i<linkIds.size(); i++){
            writer.append("\n");
            writer.append("\t<charger id=\"charger"+i+"\" link=\""+linkIds.get(i)+"\" plug_power=\"100.0\" plug_count=\"5\"/>");

        }
        writer.append("\n</chargers>");
        writer.close();
    }

    //create default 60kWh evs
    public static void createEvsFile(String name, ArrayList vehicleIds, int initialSoc) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(name+".xml"));
        writer.append("<!DOCTYPE vehicles SYSTEM \"http://matsim.org/files/dtd/electric_vehicles_v1.dtd\">\n\n<vehicles>");
        for(int i=0; i<vehicleIds.size(); i++){
            writer.append("\n");
            writer.append("\t<vehicle id=\""+vehicleIds.get(i)+"\" battery_capacity=\"60\" initial_soc=\""+initialSoc+"\" charger_types=\"default\" vehicle_type=\"defaultVehicleType\"/>");
        }
        writer.append("\n</vehicles>");
        writer.close();
    }
}
