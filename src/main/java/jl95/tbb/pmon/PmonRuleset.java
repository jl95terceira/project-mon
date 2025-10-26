package jl95.tbb.pmon;

import static java.lang.Math.floor;
import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.effect.PmonEffectByDamage;
import jl95.tbb.pmon.rules.*;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import java.util.*;

//TODO 2.0: make the following class into an interface (where the public non-final methods become interface methods)
// that can be implemented case-by-case, to change the rules of the game.

public class PmonRuleset implements MonRuleset<
        Pmon, PmonPartyEntry, PmonParty,
        PmonInitialConditions,
        PmonLocalContext,
        PmonGlobalContext,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    private static final Rng RNG = new Rng();

    public static final PartyId NO_WINNER = new PartyId();

    public static class Rng {

        private final Function0<Double> source;

        public Rng(Function0<Double> source) {this.source = source;}
        public Rng() {this(new Random()::nextDouble);}

        public Double  get() {
            return source.apply();
        }
        public Integer between(Integer a, Integer b) {
            return Math.min((int) floor(get()*(b + 1 - a) + a), b);
        }
        public Integer between(Tuple2<Integer, Integer> ab) {
            return between(ab.a1, ab.a2);
        }
        public Integer betweenInclusive(Tuple2<Integer, Integer> ab) {
            return between(ab.a1, ab.a2);
        }
        public Boolean roll(Double chance) {
            return chance >= get();
        }
        public Boolean roll(Integer chance) {
            return chance >= (100 * get());
        }
    }

    public PmonRulesetConstants constants = new PmonRulesetConstants();
    /* By default, all random events share the same source of randomness.
     * This may be changed for testing purposes etc. */
    public Rng rngSpeed           = RNG;
    public Rng rngStatModify      = RNG;
    public Rng rngStatusCondition = RNG;
    public Rng rngAccuracy        = RNG;
    public Rng rngHitNrTimes      = RNG;
    public Rng rngCritical        = RNG;
    public Rng rngImmobilise      = RNG;

    public PmonUpdateOnTargetByDamage
    detDamage(Pmon mon,
              PmonEffectByDamage effect,
              Integer nrTargets,
              Boolean critical,
              Pmon targetMon) {
        return new PmonRuleToDetermineDamage(this).detDamage(mon, effect, nrTargets, critical, targetMon);
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
    public PmonGlobalContext init(StrictMap<PartyId, PmonPartyEntry> parties, PmonInitialConditions initialConditions) {
        var context = new PmonGlobalContext();
        for (var e: parties.entrySet()) {
            var partyId = e.getKey();
            var party = PmonParty.fromEntry(e.getValue());
            context.parties.put(partyId, party);
            for (var i: I.range(howManyMonsAllowedOnField(initialConditions, partyId))) {
                party.monsOnField.put(new MonFieldPosition(), party.mons.get(i));
            }
        }
        return context;
    }

    @Override
    public void detInitialUpdates(PmonGlobalContext context, PmonInitialConditions pmonInitialConditions, Method1<PmonUpdate> updateHandler) {
        //TODO 2.0: allow for starting the battle under field conditions (weather), handicaps, initial status conditions, etc
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
    public void handleUpdates(PmonGlobalContext context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap, Method1<PmonUpdate> updateHandler) {
        new PmonRuleToDetermineUpdatesByDecisions(this).handleUpdates(context, decisionsMap, updateHandler);
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
    public Optional<PartyId> detWinner(PmonGlobalContext context) {
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
            return Optional.of(NO_WINNER);
        }
        return Optional.empty();
    }

    @Override
    public StrictMap<PartyId, StrictSet<MonFieldPosition>> allowedToDecide(PmonGlobalContext context) {
        return new PmoRuleToDetermineAllowedToDecide(this).allowedToDecide(context);
    }
}
