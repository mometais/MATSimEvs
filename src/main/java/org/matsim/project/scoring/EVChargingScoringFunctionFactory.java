package org.matsim.project.scoring;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.ev.charging.ChargingStartEvent;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;
import org.matsim.project.events.EmptyBatteryEvent;

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
        sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(params, scenario.getNetwork())); //tried to delete this to see if they charge when they don't mind how much time they travel, the answer is no
        sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring(params));
        sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

        // EV discharged scoring : penalty if vehicle is discharged
        sumScoringFunction.addScoringFunction( new PenaltyEmptyBatteryScoring());


        //Bonus if charging activity is selected (to delete, just for the tests)
        sumScoringFunction.addScoringFunction( new ChargingBonusScoring()); // to try if rewarding charging helps


        return sumScoringFunction;
    }

    private class PenaltyEmptyBatteryScoring implements SumScoringFunction.ArbitraryEventScoring{
        private double score;

        @Override
        public void handleEvent(Event event) {
            if(event instanceof EmptyBatteryEvent/* a battery is empty*/){
                score  -= 100;
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


    private class ChargingBonusScoring implements SumScoringFunction.ArbitraryEventScoring {

        private double score;

        @Override
        public void handleEvent(Event event){
            if(event instanceof ChargingStartEvent){
                score += 100;
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
