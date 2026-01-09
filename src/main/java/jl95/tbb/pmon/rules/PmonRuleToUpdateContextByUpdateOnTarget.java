package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;

import static jl95.lang.SuperPowers.function;

public class PmonRuleToUpdateContextByUpdateOnTarget {

    public final PmonRuleset ruleset;

    public PmonRuleToUpdateContextByUpdateOnTarget(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void update(PmonGlobalContext context, Iterable<PmonUpdateOnTarget> updates, MonId origin, MonId target) {

        var mon = context.parties.get(origin.partyId()).monsOnField.get(origin.position());
        var targetParty = context.parties.get(target.partyId());
        var targetMon = targetParty.monsOnField.get(target.position());
        for (var update: updates) {

            update.get(new PmonUpdateOnTarget.Handler() {

                @Override
                public void damage(PmonUpdateOnTargetByDamage damageUpdate) {

                    // damage
                    var targetMonHpBeforeDamage = targetMon.status.hp;
                    targetMon.status.hp = function((Integer hpRemaining) -> hpRemaining > 0? hpRemaining: 0)
                            .apply(targetMon.status.hp - damageUpdate.damage);
                    targetMon.status.lastFoeByDamageOnSelf = origin;
                    var damageDealt = targetMonHpBeforeDamage - targetMon.status.hp;
                    targetMon.status.damageByLastFoe = damageDealt;
                    targetMon.status.damageAccumulatedForTheTurn += damageDealt;
                    if (damageUpdate.healback != null) {
                        mon.status.hp = function((Integer hpRemaining) -> hpRemaining < mon.baseStats.hp? hpRemaining: mon.baseStats.hp)
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
                    for (var condition: conditionUpdate.statusConditionsInflict) {

                        if (targetMon.status.statusConditions.containsKey(condition.id)) /* not to apply existing condition */ {
                            continue;
                        }
                        if (I.any(I.of(targetMon.status.statusConditions.keySet())
                                .map(sc -> ruleset.areExclusive(sc, condition.id)))) /* not to apply condition that is mutually exclusive with existing */ {
                            continue;
                        }
                        targetMon.status.statusConditions.put(condition.id, condition);
                    }
                    for (var conditionId: conditionUpdate.statusConditionsCure) {

                        if (!targetMon.status.statusConditions.containsKey(conditionId)) /* not to apply existing condition */ {
                            continue;
                        }
                        targetMon.status.statusConditions.remove(conditionId);
                    }
                }

                @Override
                public void switchOut(PmonUpdateOnTargetBySwitchOut update) {

                    // switch-in
                    var monSwitchingOut = targetParty.monsOnField.get(target.position());
                    targetParty.monsOnField.put(target.position(), targetParty.mons.get(update.monToSwitchInIndex));
                    monSwitchingOut.status.statModifiers.clear(); // clear stat modifiers on switch-out
                }

                @Override
                public void lockMove(PmonUpdateOnTargetByLockMove update) {

                    // lock decision to last move used
                    targetMon.status.moveLocked = true;
                }
            });
        }
    }
}
