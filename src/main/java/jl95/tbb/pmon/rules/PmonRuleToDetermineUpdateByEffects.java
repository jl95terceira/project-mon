package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.variadic.Method1;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.effect.PmonEffectByStatusCondition;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdateOnTarget;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByLockMove;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatModifier;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatusCondition;

import static jl95.lang.SuperPowers.strict;

public class PmonRuleToDetermineUpdateByEffects {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdateByEffects(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void detUpdates(PmonGlobalContext ctx, MonId origin, MonId target, PmonEffects effects, Integer nrTargets, Boolean followUp, Method1<PmonUpdateOnTarget> updateHandler) {

        var mon       = ctx.parties.get(origin.partyId()).monsOnField.get(origin.position());
        var targetMon = ctx.parties.get(target.partyId()).monsOnField.get(target.position());
        // damage
        var damageUpdate = ruleset.detDamage(mon, effects.damage, nrTargets, ruleset.rngCritical.roll(ruleset.constants.CRITICAL_HIT_CHANCE), targetMon);
        if (damageUpdate != null) {
            updateHandler.accept(PmonUpdateOnTarget.by(damageUpdate));
            if (followUp) {
                var damage = damageUpdate.damage;
                for (var statusCondition : targetMon.status.statusConditions.values()) {
                    statusCondition.onDamageToSelf.accept(origin.partyId(), origin.position(), damage);
                    var effectsOnFoe = statusCondition.onDamageToSelfEffectsOnFoe.apply(damage);
                    if (effectsOnFoe != null) {
                        detUpdates(ctx, target, origin, effectsOnFoe, 1, false, updateHandler);
                    }
                    var effectsOnSelf = statusCondition.onDamageToSelfEffectsOnSelf.apply(damage);
                    if (effectsOnSelf != null) {
                        detUpdates(ctx, origin, target, effectsOnSelf, 1, false, updateHandler);
                    }
                }
            }
        }
        // stat modify
        var statUpdate = new PmonUpdateOnTargetByStatModifier();
        for (var e: effects.stats.statModifiers.entrySet()) {
            PmonStatModifierType type = e.getKey();
            Chanced<Integer> chancedStatModify = e.getValue();
            if (ruleset.rngStatModify.roll(chancedStatModify.chance)) {
                statUpdate.increments.put(type, chancedStatModify.value);
            }
        }
        if (!(statUpdate.increments.isEmpty() && statUpdate.resets.isEmpty())) {
            updateHandler.accept(PmonUpdateOnTarget.by(statUpdate));
        }
        // status conditions: inflict
        var statusConditionsContext = new PmonEffectByStatusCondition.Context(ctx, origin, target);
        var conditionUpdate = new PmonUpdateOnTargetByStatusCondition();
        for (var chancedStatusConditionSupplier: I.of(effects.status.statusConditionsInflict).map(x -> x.apply(statusConditionsContext))) {
            if (ruleset.rngStatusCondition.roll(chancedStatusConditionSupplier.chance)) {
                conditionUpdate.statusConditionsInflict.add(chancedStatusConditionSupplier.value.apply());
            }
        }
        // status conditions: cure
        for (var chancedStatusConditionSupplier: I.of(effects.status.statusConditionsCure).map(x -> x.apply(statusConditionsContext))) {
            if (ruleset.rngStatusCondition.roll(chancedStatusConditionSupplier.chance)) {
                conditionUpdate.statusConditionsCure.add(chancedStatusConditionSupplier.value);
            }
        }
        if (!(conditionUpdate.statusConditionsInflict.isEmpty() && conditionUpdate.statusConditionsCure.isEmpty())) {
            updateHandler.accept(PmonUpdateOnTarget.by(conditionUpdate));
        }
        // lock move
        if (effects.lockDecision != null && effects.lockDecision.lock) {
            updateHandler.accept(PmonUpdateOnTarget.by(new PmonUpdateOnTargetByLockMove()));
        }
    }
}
