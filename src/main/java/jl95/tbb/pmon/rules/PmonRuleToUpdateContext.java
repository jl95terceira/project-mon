package jl95.tbb.pmon.rules;

import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.update.*;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToUpdateContext {

    public final PmonRuleset ruleset;

    public PmonRuleToUpdateContext(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void update(MonGlobalContext<Pmon> context, PmonUpdate pmonUpdate) {
        pmonUpdate.call(new PmonUpdate.Handlers() {
            @Override
            public void move(PmonUpdateByMoveDamage update) {
                for (var t: update.updatesOnTargets) {
                    var mon = context.parties.get(t.a1).monsOnField.get(t.a2);
                    var u = t.a3;
                    // damage
                    mon.status.hp = function((Integer hpRemaining) -> hpRemaining > 0? hpRemaining: 0).apply(mon.status.hp - u.damage);
                    // stat modifiers
                    for (var t2: I(
                            tuple(u.statRaises, function(Integer::sum)),
                            tuple(u.statFalls , function((Integer a, Integer b) -> (a - b))))) {

                        for (var e: t2.a1.entrySet()) {
                            PmonStatModifierType type = e.getKey();
                            Integer stages = e.getValue();
                            if (mon.status.statModifiers.containsKey(type)) {
                                mon.status.statModifiers.put(type, t2.a2.apply(mon.status.statModifiers.get(type), stages));
                            }
                        }
                    }
                    // status conditions
                    for (var condition: u.statusConditions) {
                        if (mon.status.statusConditions.containsKey(condition.id)) {
                            continue;
                        }
                        mon.status.statusConditions.put(condition.id, condition);
                        //TODO: there may be exclusive status conditions (where, if one already is in place, the incoming condition is discarded), etc
                    }
                }
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
