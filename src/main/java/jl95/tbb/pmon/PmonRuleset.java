package jl95.tbb.pmon;

import static java.lang.Math.floor;
import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.tbb.pmon.effect.PmonEffectByDamage;
import jl95.tbb.pmon.rules.*;
import jl95.tbb.pmon.status.PmonStatModifierType;
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

    public static class Constants {

        public final Double
                STAT_MODIFIER_PARAMETER_1
                = 0.5;
        public final Double
                STAT_MODIFIER_PARAMETER_2
                = 1.0 / 3;
        public final StrictSet<PmonStatModifierType>
                STAT_TYPES_AFFECTED_BY_PARAMETER_2
                = strict(Set(PmonStatModifierType.ACCURACY, PmonStatModifierType.EVASION));
        public final Function2<Double, PmonStatModifierType, Integer>
                STAT_MODIFIER_FACTOR
                = (smt, stages) ->
                function((Double f) -> stages > 0
                        ?      (1 + stages * f)
                        : (1 / (1 - stages * f))).apply(!STAT_TYPES_AFFECTED_BY_PARAMETER_2.contains(smt)
                        ? STAT_MODIFIER_PARAMETER_1
                        : STAT_MODIFIER_PARAMETER_2);
        public final Integer
                SPEED_RNG_BREADTH
                = 20;
        public final Double
                POWER_FACTOR_OF_TYPE_SUPER_EFFECTIVE = 2.0;
        public final Double
                POWER_FACTOR_OF_TYPE_NOT_VERY_EFFECTIVE = 0.5;
        public final Double
                POWER_FACTOR_OF_TYPE_DOES_NOT_AFFECT = 0.0;
        public final StrictMap<PmonMove.EffectivenessType, Double>
                EFFECTIVENESS_POWER_FACTOR_MAP = strict(Map(
                tuple(PmonMove.EffectivenessType.SUPER_EFFECTIVE   , POWER_FACTOR_OF_TYPE_SUPER_EFFECTIVE),
                tuple(PmonMove.EffectivenessType.NOT_VERY_EFFECTIVE, POWER_FACTOR_OF_TYPE_NOT_VERY_EFFECTIVE),
                tuple(PmonMove.EffectivenessType.DOES_NOT_AFFECT   , POWER_FACTOR_OF_TYPE_DOES_NOT_AFFECT)
        ));
        public final Double
                CRITICAL_HIT_CHANCE = 0.05;
        public final Double
                CRITICAL_HIT_POWER_FACTOR = 1.5;
        public final Double
                STAB_FACTOR = 1.5;
    }
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

    private static final Rng RNG = new Rng();

    public static final PartyId NO_WINNER = new PartyId();

    public Constants constants = new Constants();
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

    @Override
    public StrictMap<PartyId, StrictMap<MonFieldPosition, PmonDecision>> lockedDecisions(PmonGlobalContext context) {
        return strict(I
                .of(context.parties.entrySet())
                .toMap(
                        e -> e.getKey(),
                        e -> strict(I
                                .of(e.getValue().monsOnField.entrySet())
                                .filter(f -> f.getValue().status.moveLocked)
                                .toMap(
                                        f -> f.getKey(),
                                        f -> PmonDecision.from(f.getValue().moveLastUsed)))
        ));
    }
}
