package jl95.tbb.pmon.rules;

import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;
import jl95.tbb.pmon.update.atomic.*;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToUpdateContext {

    public final PmonRuleset ruleset;

    public PmonRuleToUpdateContext(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void update(MonGlobalContext<Pmon> context, PmonUpdate pmonUpdate) {

        pmonUpdate.call(new PmonUpdate.Handlers() {

            @Override
            public void move(PmonUpdateByMove moveUpdate) {


                for (var t: moveUpdate.updatesOnTargets) {

                    var mon = context.parties.get(t.a1).monsOnField.get(t.a2);
                    var u = t.a3;
                    // damage
                    u.call(new PmonUpdateByMove.UpdateOnTarget.Handlers() {
                        @Override
                        public void miss() {
                            /* Haw Haw! */
                        }

                        @Override
                        public void hit(Iterable<PmonAtomicEffect> updates) {
                            for (var update: updates) {
                                update.call(new PmonAtomicEffect.Handlers() {
                                    @Override
                                    public void damage(PmonAtomicEffectByDamage damageUpdate) {
                                        // damage
                                        mon.status.hp = function((Integer hpRemaining) -> hpRemaining > 0 ? hpRemaining : 0).apply(mon.status.hp - damageUpdate.damage);
                                    }

                                    @Override
                                    public void statModify(PmonAtomicEffectByStatModifier statUpdate) {
                                        // stat modifiers
                                        for (var t2: I(
                                                tuple(statUpdate.statRaises, function(Integer::sum)),
                                                tuple(statUpdate.statFalls , function((Integer a, Integer b) -> (a - b))))) {

                                            for (var e: t2.a1.entrySet()) {

                                                PmonStatModifierType type = e.getKey();
                                                Integer stages = e.getValue();
                                                if (mon.status.statModifiers.containsKey(type)) {

                                                    mon.status.statModifiers.put(type, t2.a2.apply(mon.status.statModifiers.get(type), stages));
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void statusCondition(PmonAtomicEffectByStatusCondition conditionUpdate) {
                                        // status conditions
                                        for (var condition: conditionUpdate.statusConditionsApply) {

                                            if (mon.status.statusConditions.containsKey(condition.id)) {
                                                continue;
                                            }
                                            mon.status.statusConditions.put(condition.id, condition);
                                            //TODO: there may be exclusive status conditions (where, if one already is in place, the incoming condition is discarded), etc
                                        }
                                    }

                                    @Override
                                    public void switchIn(PmonAtomicEffectBySwitchIn update) {

                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void notify(PmonNotification update) {

                /* no need to update - notifications are meant to give information to players only */
            }

            @Override
            public void switchIn(PmonUpdateBySwitchIn update) {

                var party = context.parties.get(update.partyId);
                party.monsOnField.remove(update.monId);
                party.monsOnField.put(new MonParty.MonId(), party.mons.get(update.monToSwitchInIndex));
            }
        });
    }
}
