package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.function;

public class PmonRuleToUpdateContextByUpdateOnTarget {

    public final PmonRuleset ruleset;

    public PmonRuleToUpdateContextByUpdateOnTarget(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void update(PmonGlobalContext context, Iterable<PmonUpdateOnTarget> updates, Tuple2<PartyId, MonFieldPosition> origin, Tuple2<PartyId, MonFieldPosition> target) {

        var mon = context.parties.get(origin.a1).monsOnField.get(origin.a2);
        var targetParty = context.parties.get(target.a1);
        var targetMon = targetParty.monsOnField.get(target.a2);
        for (var update: updates) {

            update.call(new PmonUpdateOnTarget.Handler() {

                @Override
                public void damage(PmonUpdateOnTargetByDamage damageUpdate) {

                    // damage
                    targetMon.status.hp = function((Integer hpRemaining) -> hpRemaining > 0? hpRemaining: 0)
                            .apply(targetMon.status.hp - damageUpdate.damage);
                    if (damageUpdate.healback != null) {
                        mon.status.hp = function((Integer hpRemaining) -> hpRemaining < mon.attrs.baseStats.hp? hpRemaining: mon.attrs.baseStats.hp)
                                .apply(mon.status.hp + damageUpdate.healback);
                    }
                }

                @Override
                public void statModify(PmonUpdateOnTargetByStatModifier statUpdate) {

                    // stat modifiers
                    var monStatModifiers = targetMon.status.statModifiers;
                    for (var e: statUpdate.increments.entrySet()) {

                        PmonStatModifierType type = e.getKey();
                        Integer stages = e.getValue();
                        monStatModifiers.put(type, (monStatModifiers.containsKey(type)? monStatModifiers.get(type): 0) + stages);
                        if (monStatModifiers.get(type) == 0) {
                            monStatModifiers.remove(type);
                        }
                    }
                }

                @Override
                public void statusCondition(PmonUpdateOnTargetByStatusCondition conditionUpdate) {

                    // status conditions
                    for (var condition: conditionUpdate.statusConditionsApply) {

                        if (targetMon.status.statusConditions.containsKey(condition.id)) /* not to apply existing condition */ {

                            continue;
                        }
                        if (I.any(I.of(targetMon.status.statusConditions.keySet())
                                .map(sc -> ruleset.areExclusive(sc, condition.id)))) /* not to apply condition that is mutually exclusive with existing */ {

                            continue;
                        }
                        targetMon.status.statusConditions.put(condition.id, condition);
                    }
                }

                @Override
                public void switchOut(PmonUpdateOnTargetBySwitchOut update) {

                    // switch-in
                    targetParty.monsOnField.put(target.a2, targetParty.mons.get(update.monToSwitchInIndex));
                }
            });
        }
    }
}
