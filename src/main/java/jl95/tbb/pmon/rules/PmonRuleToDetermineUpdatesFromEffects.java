package jl95.tbb.pmon.rules;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.util.StrictList;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdateOnTarget;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatModifier;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatusCondition;

import java.util.Optional;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;

public class PmonRuleToDetermineUpdatesFromEffects {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdatesFromEffects(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Iterable<PmonUpdateOnTarget> detUpdates(PmonGlobalContext ctx, PartyId originPartyId, MonFieldPosition originMonPos, PartyId targetPartyId, MonFieldPosition targetMonPos, PmonEffects effects, Integer nrTargets, Boolean followUp) {

        var mon       = ctx.parties.get(originPartyId).monsOnField.get(originMonPos);
        var targetMon = ctx.parties.get(targetPartyId).monsOnField.get(targetMonPos);
        StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
        // damage
        var damageUpdate = ruleset.detDamage(mon, effects.damage, nrTargets, ruleset.constants.CRITICAL_HIT_CHANCE >= ruleset.rngCritical.get(), targetMon);
        if (damageUpdate != null) {
            atomicUpdates.add(PmonUpdateOnTarget.by(damageUpdate));
            if (followUp) {
                var damage = damageUpdate.damage;
                for (var statusCondition : targetMon.status.statusConditions.values()) {
                    statusCondition.onDamage.accept(originPartyId, originMonPos, damage);
                    var effectsOnFoe = statusCondition.onDamageEffectsOnFoe.apply(damage);
                    if (effectsOnFoe != null) {
                        atomicUpdates.addAll(detUpdates(ctx, targetPartyId, targetMonPos, originPartyId, originMonPos, effectsOnFoe, 1, false));
                    }
                }
            }
        }
        // stat modify
        var statUpdate = new PmonUpdateOnTargetByStatModifier();
        for (var e: effects.stats.statModifiers.entrySet()) {
            PmonStatModifierType type = e.getKey();
            Chanced<Integer> chancedStatModify = e.getValue();
            if (ruleset.rngStatModify.roll100(chancedStatModify.chance)) {
                statUpdate.increments.put(type, chancedStatModify.value);
            }
        }
        if (!(statUpdate.increments.isEmpty() && statUpdate.resets.isEmpty())) {
            atomicUpdates.add(PmonUpdateOnTarget.by(statUpdate));
        }
        // status conditions
        var conditionUpdate = new PmonUpdateOnTargetByStatusCondition();
        for (var chancedStatusConditionSupplier: effects.status.statusConditions) {
            if (ruleset.rngStatusCondition.roll100(chancedStatusConditionSupplier.chance)) {
                conditionUpdate.statusConditionsApply.add(chancedStatusConditionSupplier.value.apply());
            }
        }
        if (!(conditionUpdate.statusConditionsApply.isEmpty() && conditionUpdate.statusConditionsRemove.isEmpty())) {
            atomicUpdates.add(PmonUpdateOnTarget.by(conditionUpdate));
        }
        return atomicUpdates;
    }
}
