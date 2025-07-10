package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.StrictList;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.PmonUpdateByMove;
import jl95.tbb.pmon.update.PmonUpdateOnTarget;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatModifier;
import jl95.tbb.pmon.update.PmonUpdateOnTargetByStatusCondition;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;

public class PmonRuleToDetermineUpdatesFromEffects {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineUpdatesFromEffects(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Iterable<PmonUpdateOnTarget> detUpdates(Pmon mon, Pmon targetMon, PmonEffects effects, Integer nrTargets) {

        StrictList<PmonUpdateOnTarget> atomicUpdates = strict(List());
        // damage
        var damageUpdate = ruleset.detDamage(mon, effects.damage, nrTargets, ruleset.constants.CRITICAL_HIT_CHANCE >= ruleset.rng(), targetMon);
        if (damageUpdate != null) {
            atomicUpdates.add(PmonUpdateOnTarget.by(damageUpdate));
        }
        // stat modify
        var statUpdate = new PmonUpdateOnTargetByStatModifier();
        for (var e: effects.stats.statModifiers.entrySet()) {
            PmonStatModifierType type = e.getKey();
            Chanced<Integer> chancedStatModify = e.getValue();
            if (ruleset.roll100(chancedStatModify.chance)) {
                statUpdate.increments.put(type, chancedStatModify.value);
            }
        }
        if (!(statUpdate.increments.isEmpty() && statUpdate.resets.isEmpty())) {
            atomicUpdates.add(PmonUpdateOnTarget.by(statUpdate));
        }
        // status conditions
        var conditionUpdate = new PmonUpdateOnTargetByStatusCondition();
        for (var chancedStatusConditionSupplier: effects.status.statusConditions) {
            if (ruleset.roll100(chancedStatusConditionSupplier.chance)) {
                conditionUpdate.statusConditionsApply.add(chancedStatusConditionSupplier.value.apply());
            }
        }
        if (!(conditionUpdate.statusConditionsApply.isEmpty() && conditionUpdate.statusConditionsRemove.isEmpty())) {
            atomicUpdates.add(PmonUpdateOnTarget.by(conditionUpdate));
        }
        return atomicUpdates;
    }
}
