package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToUpdateContext {

    public final PmonRuleset ruleset;

    public PmonRuleToUpdateContext(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void update(PmonGlobalContext context, PmonUpdate pmonUpdate) {

        pmonUpdate.call(new PmonUpdate.Handler() {

            @Override
            public void move(PmonUpdateByMove moveUpdate) {

                var party = context.parties.get(moveUpdate.partyId);
                var mon   = party.monsOnField.get(moveUpdate.monId);
                for (var t: moveUpdate.updatesOnTargets) {

                    var targetPartyId  = t.a1;
                    var targetParty    = context.parties.get(targetPartyId);
                    var targetMonId    = t.a2;
                    var targetMon      = targetParty.monsOnField.get(targetMonId);
                    var updateOnTarget = t.a3;
                    updateOnTarget.call(new PmonUpdateByMove.UpdateOnTarget.Handler() {

                        @Override
                        public void miss() {

                            /* haw haw! */
                        }

                        @Override
                        public void hit(Iterable<PmonUpdateOnTarget> updates) {

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
                                        targetParty.monsOnField.put(targetMonId, targetParty.mons.get(update.monToSwitchInIndex));
                                    }
                                });
                            }
                        }

                        @Override
                        public void noTarget() {

                            /* nothing to do */
                        }
                    });
                }
            }

            @Override
            public void pass(PmonUpdateByPass update) {

            }

            @Override
            public void switchOut(PmonUpdateBySwitchOut update) {

                var party = context.parties.get(update.partyId);
                party.monsOnField.put(update.monFieldPosition, party.mons.get(update.monToSwitchInPartyPosition));
            }
        });
        for (var condition: context.fieldConditions.values()) {

            condition.turnNr += 1;
        }
        for (var party: context.parties.values()) {

            for (var condition: party.fieldConditions.values()) {

                condition.turnNr += 1;
            }
            for (var condition: I.of(party.fieldConditionsByMon.values()).flatmap(StrictMap::values)) {

                condition.turnNr += 1;
            }
        }
    }
}
