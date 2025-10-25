package jl95.tbb.pmon.rules;

import jl95.lang.variadic.Method1;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdateOnTarget;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatModifier;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatusCondition;

import static jl95.lang.SuperPowers.strict;

public class PmonRuleToDetermineUpdatesFromEffects {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdatesFromEffects(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void detUpdates(PmonGlobalContext ctx, PartyId originPartyId, MonFieldPosition originMonPos, PartyId targetPartyId, MonFieldPosition targetMonPos, PmonEffects effects, Integer nrTargets, Boolean followUp, Method1<PmonUpdateOnTarget> updateHandler) {

        var mon       = ctx.parties.get(originPartyId).monsOnField.get(originMonPos);
        var targetMon = ctx.parties.get(targetPartyId).monsOnField.get(targetMonPos);
        // damage
        var damageUpdate = ruleset.detDamage(mon, effects.damage, nrTargets, ruleset.rngCritical.roll(ruleset.constants.CRITICAL_HIT_CHANCE), targetMon);
        if (damageUpdate != null) {
            updateHandler.accept(PmonUpdateOnTarget.by(damageUpdate));
            if (followUp) {
                var damage = damageUpdate.damage;
                for (var statusCondition : targetMon.status.statusConditions.values()) {
                    statusCondition.onDamageToSelf.accept(originPartyId, originMonPos, damage);
                    var effectsOnFoe = statusCondition.onDamageToSelfEffectsOnFoe.apply(damage);
                    if (effectsOnFoe != null) {
                        detUpdates(ctx, targetPartyId, targetMonPos, originPartyId, originMonPos, effectsOnFoe, 1, false, updateHandler);
                    }
                    var effectsOnSelf = statusCondition.onDamageToSelfEffectsOnSelf.apply(damage);
                    if (effectsOnSelf != null) {
                        detUpdates(ctx, originPartyId, originMonPos, originPartyId, originMonPos, effectsOnSelf, 1, false, updateHandler);
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
        // status conditions
        var conditionUpdate = new PmonUpdateOnTargetByStatusCondition();
        for (var chancedStatusConditionSupplier: effects.status.statusConditionsInflict) {
            if (ruleset.rngStatusCondition.roll(chancedStatusConditionSupplier.chance)) {
                conditionUpdate.statusConditionsInflict.add(chancedStatusConditionSupplier.value.apply());
            }
        }
        for (var chancedStatusConditionSupplier: effects.status.statusConditionsCure) {
            if (ruleset.rngStatusCondition.roll(chancedStatusConditionSupplier.chance)) {
                conditionUpdate.statusConditionsCure.add(chancedStatusConditionSupplier.value);
            }
        }
        if (!(conditionUpdate.statusConditionsInflict.isEmpty() && conditionUpdate.statusConditionsCure.isEmpty())) {
            updateHandler.accept(PmonUpdateOnTarget.by(conditionUpdate));
        }
    }
}
