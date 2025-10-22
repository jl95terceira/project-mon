package jl95.tbb.pmon.rules;

import jl95.lang.P;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.effect.PmonEffectByDamage;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByDamage;

import static java.lang.Math.floor;
import static jl95.lang.SuperPowers.*;

public class PmonRuleToDetermineDamage {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineDamage(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public PmonUpdateOnTargetByDamage detDamage(Pmon mon,
                                                PmonEffectByDamage effect,
                                                Integer nrTargets,
                                                Boolean critical,
                                                Pmon targetMon) {

        var isDamagingR = new P<>(true);
        var damageR = new P<>(0);
        var effectivenessFactorR = new P<>(1.0);
        effect.power.call(new PmonMovePower.Handler() {
            @Override
            public void none() {
                isDamagingR.set(false);
            }
            @Override
            public void typed(Integer power) {
                var sourceAttack = function((PmonStats stats) -> effect.type == PmonMoveType.NORMAL
                        ? stats.attack
                        : stats.specialAttack).apply(mon.attrs.baseStats);
                var sourceAttackStatModifierType = effect.type == PmonMoveType.NORMAL
                        ? PmonStatModifierType.ATTACK
                        : PmonStatModifierType.SPECIAL_ATTACK;
                sourceAttack = (int)(sourceAttack * ruleset.constants.STAT_MODIFIER_FACTOR.apply(sourceAttackStatModifierType, mon.status.statModifiers.getOrDefault(sourceAttackStatModifierType, 0)));
                var targetDefense = function((PmonStats stats) -> effect.type == PmonMoveType.NORMAL
                        ? stats.defense
                        : stats.specialDefense).apply(targetMon.attrs.baseStats);
                var targetDefenseStatModifierType = effect.type == PmonMoveType.NORMAL
                        ? PmonStatModifierType.DEFENSE
                        : PmonStatModifierType.SPECIAL_DEFENSE;
                targetDefense = (int)(targetDefense * ruleset.constants.STAT_MODIFIER_FACTOR.apply(targetDefenseStatModifierType, targetMon.status.statModifiers.getOrDefault(targetDefenseStatModifierType, 0)));
                for (var targetMonType: targetMon.attrs.types.values()) {
                    effectivenessFactorR.set(effectivenessFactorR.get() * ruleset.constants.EFFECTIVENESS_POWER_FACTOR_MAP.get(effect.pmonType.effectivenessAgainst(targetMonType)));
                }
                damageR.set((int)(0.44
                               * power
                               * sourceAttack / targetDefense
                               * (mon.attrs.types.containsKey(effect.pmonType.id)? ruleset.constants.STAB_FACTOR: 1.0)
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
        if (!isDamagingR.get()) {
            return null;
        }
        var update = new PmonUpdateOnTargetByDamage();
        update.damage = (int) floor(effect.powerReductionFactorByNrTargets.apply(nrTargets) * damageR.get());
        update.effectivenessFactor = effectivenessFactorR.get();
        if (effect.healbackFactor != null) {
            update.healback = (int)(effect.healbackFactor * update.damage);
        }
        update.criticalHit = critical;
        return update;
    }
}
