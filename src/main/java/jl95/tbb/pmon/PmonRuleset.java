package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.rules.*;
import jl95.tbb.pmon.status.PmonStatModifierType;
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
    public Function0<Double> rng = new Random()::nextDouble;

    public Double rng() { 
        
        return rng.apply(); 
    }

    public Integer detDamage(Pmon mon, PmonMove move, Pmon targetMon) {

        return new PmonDamageDetRule(this).detDamage(mon, move, targetMon);
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
        
        return new PmonLocalContextDetRule(this).detLocalContext(context, ownPartyId);
    }

    @Override
    public Iterable<PmonUpdate> detUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {

        return new PmonUpdateDetRule(this).detUpdates(context, decisionsMap);
    }

    @Override
    public void update(PmonGlobalContext context, PmonUpdate pmonUpdate) {

        new PmonContextUpdateRule(this).update(context, pmonUpdate);
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
    public Boolean allowDecide(PmonGlobalContext context, PartyId partyId, MonParty.MonId monId) {

        return new PmonDecisionAllowanceRule(this).allowDecide(context, partyId, monId);
    }
}
