package jl95.tbb.pmon.rules;

import jl95.lang.Ref;
import jl95.lang.variadic.Tuple2;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.status.PmonStatModifierType;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineDamage {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineDamage(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Tuple3<Boolean, Integer, Double> detDamage(Pmon mon,
                                                      Integer moveIndex,
                                                      Boolean critical,
                                                      Pmon targetMon) {

        var move = mon.moves.get(moveIndex);
        var isDamaging = new Ref<>(true);
        var damageR = new Ref<>(0);
        var effectivenessFactorR = new Ref<>(1.0);
        move.attrs.power.call(new PmonMovePower.Handlers() {
            @Override
            public void none() {
                isDamaging.set(false);
            }
            @Override
            public void typed(Integer power) {
                var sourceAttack = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.attack
                        : stats.specialAttack).apply(mon.attrs.baseStats);
                var sourceAttackStatModifierType = move.attrs.type == PmonMoveType.NORMAL
                        ? PmonStatModifierType.ATTACK
                        : PmonStatModifierType.SPECIAL_ATTACK;
                sourceAttack = (int)(sourceAttack * ruleset.constants.STAT_MODIFIER_FACTOR.apply(sourceAttackStatModifierType, mon.status.statModifiers.getOrDefault(sourceAttackStatModifierType, 0)));
                var targetDefense = function((PmonStats stats) -> move.attrs.type == PmonMoveType.NORMAL
                        ? stats.defense
                        : stats.specialDefense).apply(targetMon.attrs.baseStats);
                var targetDefenseStatModifierType = move.attrs.type == PmonMoveType.NORMAL
                        ? PmonStatModifierType.DEFENSE
                        : PmonStatModifierType.SPECIAL_DEFENSE;
                targetDefense = (int)(targetDefense * ruleset.constants.STAT_MODIFIER_FACTOR.apply(targetDefenseStatModifierType, targetMon.status.statModifiers.getOrDefault(targetDefenseStatModifierType, 0)));
                for (var targetMonType: targetMon.attrs.types.values()) {
                    effectivenessFactorR.set(effectivenessFactorR.get() * ruleset.constants.POWER_FACTOR_MAP.get(move.attrs.pmonType.effectivenessAgainst(targetMonType)));
                }
                damageR.set((int)(0.44
                               * power
                               * sourceAttack / targetDefense
                               * (mon.attrs.types.containsKey(move.attrs.pmonType.id)? ruleset.constants.STAB_FACTOR: 1.0)
                               * (critical? ruleset.constants.CRITICAL_HIT_POWER_FACTOR: 1.0)));

                //TODO: consider abilities, status conditions, screen effects, etc.

            }
            @Override
            public void constant(Integer damage) {

                damageR.set(damage);
            }
            @Override
            public void byHp(Double percent) {

                damageR.set((int) (percent * targetMon.status.hp));
            }
            @Override
            public void byMaxHp(Double percent) {

                damageR.set((int) (percent * targetMon.attrs.baseStats.hp));
            }
        });
        return tuple(isDamaging.get(), damageR.get(), effectivenessFactorR.get());
    }
}
