package jl95.tbb.pmon;

import static java.lang.Math.floor;
import static jl95.lang.SuperPowers.*;

import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.decision.PmonDecisionByPass;
import jl95.tbb.pmon.decision.PmonDecisionBySwitchIn;
import jl95.tbb.pmon.decision.PmonDecisionByUseMove;
import jl95.tbb.pmon.rules.*;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;

import java.util.*;

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

    public Tuple2<Integer, Double> detDamage(Pmon mon,
                                             Integer moveIndex,
                                             Boolean critical,
                                             Pmon targetMon) {

        return new PmonRuleToDetermineDamage(this).detDamage(mon, moveIndex, critical, targetMon);
    }

    public Boolean isAlive(Pmon mon) {
        return mon.status.hp > 0;
    }

    @Override
    public PmonGlobalContext init(StrictMap<PartyId, MonPartyEntry<Pmon>> parties, PmonInitialConditions pmonInitialConditions) {
        
        var context = new PmonGlobalContext();
        for (var e: parties.entrySet()) {
            var partyId = e.getKey();
            var partyEntry = e.getValue();
            context.parties.put(partyId, MonParty.fromEntry(partyEntry));
        }
        return context;
    }

    @Override
    public Iterable<PmonUpdate> detInitialUpdates(PmonGlobalContext context, PmonInitialConditions pmonInitialConditions) {
        
        return I();
    }

    @Override
    public PmonLocalContext detLocalContext(PmonGlobalContext context, PartyId ownPartyId) {
        
        return new PmonRuleToDetermineLocalContext(this).detLocalContext(context, ownPartyId);
    }

    @Override
    public Boolean isValid(PmonGlobalContext context, PartyId partyId, MonPartyDecision<PmonDecision> decision) {
        var ref = new Ref<>(true);
        var party = context.parties.get(partyId);
        for (var e: decision.monDecisions.entrySet()) {
            var monId = e.getKey();
            if (!party.monsOnField.containsKey(monId)) {
                return false;
            }
            PmonDecision monDecision = e.getValue();
            monDecision.call(new PmonDecision.Handlers() {

                @Override
                public void pass(PmonDecisionByPass decision) {}
                @Override
                public void switchIn(PmonDecisionBySwitchIn decision) {

                    if (decision.monSwitchInIndex < 0 || decision.monSwitchInIndex >= party.mons.size()) {
                        ref.set(false);
                    }
                    else {
                        var mon = party.mons.get(decision.monSwitchInIndex);
                        if (!isAlive(mon)) {
                            ref.set(false);
                        }
                    }
                }
                @Override
                public void useMove(PmonDecisionByUseMove decision) {
                    var mon = party.monsOnField.get(monId);
                    if (decision.moveIndex < 0 || decision.moveIndex >= mon.moves.size()) {
                        ref.set(false);
                    }
                    else {
                        var move = mon.moves.get(decision.moveIndex);
                        if (move.status.disabled || move.status.pp <= 0) {
                            ref.set(false);
                        }
                    }
                }
            });
            if (!ref.get()) {
                break;
            }
        }
        return ref.get();
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
    public Boolean allowedToDecide(PmonGlobalContext context, PartyId partyId, MonPosition monId) {

        return new PmoRuleToAllowDecision(this).allowDecide(context, partyId, monId);
    }
}
