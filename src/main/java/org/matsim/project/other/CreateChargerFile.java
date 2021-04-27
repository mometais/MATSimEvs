package org.matsim.project.other;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.charging.ChargingLogic;
import org.matsim.contrib.ev.infrastructure.*;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.matsim.contrib.ev.infrastructure.ImmutableChargerSpecification.newBuilder;

public class CreateChargerFile {

    public void createDefaultChargerForAllLinks(Network network){
        ArrayList<Link> links = new ArrayList(network.getLinks().values());
        ArrayList chargerSpecifications = new ArrayList();

        String defaultType = null;
        Double defaultPlugPower = 100.;
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
        chargerWriter.write("testChargerAllLinks.xml");

    }

}
