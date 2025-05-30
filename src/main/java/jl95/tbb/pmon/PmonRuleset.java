package jl95.tbb.pmon;

import static java.lang.Math.floor;
import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.rules.*;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import java.util.*;

//TODO: make the following class into an interface (where the public non-final methods become interface methods)
// that can be implemented case-by-case, to change the rules of the game.

public class PmonRuleset implements MonRuleset<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonLocalContext,
        PmonGlobalContext,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public static PartyId NO_VICTOR = new PartyId();

    public final PmonRulesetConstants constants = new PmonRulesetConstants();
    public Function0<Double> rng = new Random()::nextDouble; // to return a number between 0 and 1

    public Double rng() {
        
        return rng.apply(); 
    }
    public Integer rngBetween(Integer a, Integer b) {
        return (int) floor(rng()*(a - b) + b);
    }
    public Integer rngBetween(Tuple2<Integer, Integer> ab) {
        return rngBetween(ab.a1, ab.a2);
    }
    public Integer rngBetweenInclusive(Tuple2<Integer, Integer> ab) {
        return rngBetween(ab.a1, ab.a2 + 1);
    }
    public Boolean roll(Double chance) {
        return chance >= rng();
    }
    public Boolean roll100(Integer chance) {
        return chance >= (100 * rng());
    }

    public Tuple3<Boolean, Integer, Double>
    detDamage(Pmon mon,
              Integer moveIndex,
              Boolean critical,
              Pmon targetMon) {

        return new PmonRuleToDetermineDamage(this).detDamage(mon, moveIndex, critical, targetMon);
    }

    public Boolean
    isAlive(Pmon mon) {
        return mon.status.hp > 0;
    }

    public Boolean
    areExclusive(PmonStatusCondition.Id condition1Id,
                 PmonStatusCondition.Id condition2Id) {

        return false;
    }

    public Integer
    howManyMonsAllowedOnField(PmonInitialConditions pmonInitialConditions, PartyId partyId) {

        return 1;
    }

    @Override
    public PmonGlobalContext init(StrictMap<PartyId, MonPartyEntry<Pmon>> parties, PmonInitialConditions initialConditions) {
        
        var context = new PmonGlobalContext();
        for (var e: parties.entrySet()) {
            var partyId = e.getKey();
            var party = MonParty.fromEntry(e.getValue());
            context.parties.put(partyId, party);
            for (var i: I.range(howManyMonsAllowedOnField(initialConditions, partyId))) {
                party.monsOnField.put(new MonFieldPosition(), party.mons.get(i));
            }
        }
        return context;
    }

    @Override
    public Iterable<PmonUpdate> detInitialUpdates(PmonGlobalContext context, PmonInitialConditions pmonInitialConditions) {

        return I();
        //TODO: allow for starting the battle under field conditions (weather), handicaps, initial status conditions, etc
    }

    @Override
    public PmonLocalContext detLocalContext(PmonGlobalContext context, PartyId ownPartyId) {
        
        return new PmonRuleToDetermineLocalContext(this).detLocalContext(context, ownPartyId);
    }

    @Override
    public Boolean isValid(PmonGlobalContext context, PartyId partyId, MonPartyDecision<PmonDecision> decision) {

        return new PmonRuleToValidateDecision(this).isValid(context, partyId, decision);
    }

    @Override
    public Iterable<PmonUpdate> detUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {

        return new PmonRuleToDetermineUpdates(this).detUpdates(context, decisionsMap);
    }

    @Override
    public void update(PmonGlobalContext context, PmonUpdate pmonUpdate) {

        new PmonRuleToUpdateContext(this).update(context, pmonUpdate);
    }

    @Override
    public Iterable<PmonUpdate> detLocalUpdates(PmonUpdate pmonUpdate, PartyId partyId) {
        
        return List.of(pmonUpdate);
    }

    @Override
    public Optional<PartyId> detVictory(PmonGlobalContext context) {
        
        Set<PartyId> partiesRemaining = Set();
        for (var e: context.parties.entrySet()) {
            var partyId = e.getKey();
            var party = e.getValue();
            for (var mon: party.mons) {
                if (mon.status.hp > 0) {
                    partiesRemaining.add(partyId);
                    break;
                }
            }
        }
        if (partiesRemaining.size() == 1) {
            return Optional.of(partiesRemaining.iterator().next());
        }
        else if (partiesRemaining.isEmpty()) {
            return Optional.of(NO_VICTOR);
        }
        return Optional.empty();
    }

    @Override
    public StrictMap<PartyId, StrictSet<MonFieldPosition>> allowedToDecide(PmonGlobalContext context) {

        return new PmoRuleToDetermineAllowedToDecide(this).allowedToDecide(context);
    }
}
