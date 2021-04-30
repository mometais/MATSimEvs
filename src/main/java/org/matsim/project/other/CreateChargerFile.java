package org.matsim.project.other;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.charging.ChargingLogic;
import org.matsim.contrib.ev.infrastructure.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.matsim.contrib.ev.infrastructure.ImmutableChargerSpecification.newBuilder;

public class CreateChargerFile {

    public static String createDefaultChargerForAllLinks(Network network, String fileName){
        /*
        Create a charger file with a default charger at each link of the given network
         */
        ArrayList<Link> links = new ArrayList(network.getLinks().values());
        ArrayList chargerSpecifications = new ArrayList();

        String defaultType = "default";
        Double defaultPlugPower = 100000.;
        int defaultPlugCount = 5;

        for(Link link:links){
            ImmutableChargerSpecification.Builder builder = newBuilder();
            builder.id(Id.create("charger_"+link.getId().toString(),Charger.class));
            builder.linkId(link.getId());
            builder.chargerType(defaultType);
            builder.plugPower(defaultPlugPower);
            builder.plugCount(defaultPlugCount);
            ChargerSpecification specification = builder.build();
//            Charger charger = new ChargerImpl(specification, link, (new ChargingLogic ????));
            chargerSpecifications.add(specification);
        }

        ChargerWriter chargerWriter = new ChargerWriter(chargerSpecifications.stream());
        chargerWriter.write("scenarios/equil/"+fileName);

        return fileName;
    }

    public static String createDefaultChargersForNLinks(int chargersNumber, Network network, String fileName){
        /*
        Create a charger file with chargerNumber chargers randomly put at the links
         */
        ArrayList<Link> allLinks = new ArrayList(network.getLinks().values());
        ArrayList<Link> selectedLinks = new ArrayList();
        ArrayList chargerSpecifications = new ArrayList();


        ArrayList intArray = new ArrayList();
        for(int i = 0; i<= allLinks.size(); i++){
            intArray.add(i);
        }
        Collections.shuffle(intArray);
        for(int j=0; j<= Math.min(chargersNumber,allLinks.size()); j++  ){
            selectedLinks.add(allLinks.get(j));
        }


        String defaultType = "default";
        Double defaultPlugPower = 100000.;
        int defaultPlugCount = 5;

        for(Link link:selectedLinks){
            ImmutableChargerSpecification.Builder builder = newBuilder();
            builder.id(Id.create("charger_"+link.getId().toString(),Charger.class));
            builder.linkId(link.getId());
            builder.chargerType(defaultType);
            builder.plugPower(defaultPlugPower);
            builder.plugCount(defaultPlugCount);
            ChargerSpecification specification = builder.build();
//            Charger charger = new ChargerImpl(specification, link, (new ChargingLogic ????));
            chargerSpecifications.add(specification);
        }

        ChargerWriter chargerWriter = new ChargerWriter(chargerSpecifications.stream());
        chargerWriter.write("scenarios/equil/"+fileName);

        return fileName;
    }




    public static void main(String[] args) {


    }

}
