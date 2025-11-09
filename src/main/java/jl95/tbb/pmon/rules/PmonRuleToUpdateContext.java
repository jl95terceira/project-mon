package jl95.tbb.pmon.rules;

import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.tbb.pmon.update.*;

import static jl95.lang.SuperPowers.*;

public class PmonRuleToUpdateContext {

    public final PmonRuleset ruleset;

    public PmonRuleToUpdateContext(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public void update(PmonGlobalContext context, PmonUpdate pmonUpdate) {

        pmonUpdate.get(new PmonUpdate.Handler() {

            @Override public void move(PmonUpdateByMove moveUpdate) {

                var party = context.parties.get(moveUpdate.monId.partyId());
                var mon   = party.monsOnField.get(moveUpdate.monId.position());
                for (var t: moveUpdate.usageResults) {

                    var targetPartyId  = t.a1;
                    var targetParty    = context.parties.get(targetPartyId);
                    var targetMonId    = t.a2;
                    var targetMon      = targetParty.monsOnField.get(targetMonId);
                    var updateOnTarget = t.a3;
                    updateOnTarget.get(new PmonUpdateByMove.UsageResult.Handler() {

                        @Override public void hit(Iterable<PmonUpdateOnTarget> updates) {
                            new PmonRuleToUpdateContextByUpdateOnTarget(ruleset).update(context, updates, moveUpdate.monId, new MonId(targetPartyId, targetMonId));
                        }
                        @Override public void miss(PmonUpdateByMove.UsageResult.MissType type) {
                            /* haw haw! */
                        }
                        @Override public void immobilised(PmonStatusCondition.Id id) {
                            /* OOF */
                        }

                    });
                }
            }
            @Override public void pass(PmonUpdateByPass update) {
                // no update, since pass
            }
            @Override public void switchOut(PmonUpdateBySwitchOut update) {

                var party = context.parties.get(update.partyId);
                party.monsOnField.put(update.monFieldPosition, party.mons.get(update.monToSwitchInPartyPosition));
            }
            @Override public void other(PmonUpdateByOther update) {
                for (var e: update.atomicUpdates.entrySet()) {
                    new PmonRuleToUpdateContextByUpdateOnTarget(ruleset).update(context, e.getValue(), update.origin, e.getKey());
                }
            }
        });
    }
}
