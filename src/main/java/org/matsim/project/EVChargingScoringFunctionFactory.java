package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.core.api.internal.HasVehicleId;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleUtils;

public class EVChargingScoringFunctionFactory implements ScoringFunctionFactory {
    private final Scenario scenario;

    public EVChargingScoringFunctionFactory(Scenario scenario){
        this.scenario = scenario;
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        SumScoringFunction sumScoringFunction = new SumScoringFunction();

        // default MATSim scoring
        final ScoringParameters params = new ScoringParameters.Builder(scenario, person).build();
        sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(params));
        sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(params, scenario.getNetwork()));
        sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring(params));
        sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

        // EV discharged scoring
        // "if electric vehicle" condition to add
        sumScoringFunction.addScoringFunction( new PenaltyEmptyBatteryScoring());


        return sumScoringFunction;
    }

    private class PenaltyEmptyBatteryScoring implements SumScoringFunction.ArbitraryEventScoring{
        private double score;

        @Override
        public void handleEvent(Event event) {
            if(event instanceof EmptyBatteryEvent/* a battery is empty*/){
                score  -= 1000;
            }
        }

        @Override
        public void finish() {

        }

        @Override
        public double getScore() {
            return score;
        }
    }


}
