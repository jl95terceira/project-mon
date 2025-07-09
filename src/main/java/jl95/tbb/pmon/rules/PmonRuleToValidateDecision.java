package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.lang.Ref;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.pmon.PmonDecision;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchIn;
import jl95.tbb.pmon.decision.PmonDecisionToUseMove;

public class PmonRuleToValidateDecision {

    public final PmonRuleset ruleset;

    public PmonRuleToValidateDecision(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Boolean isValid(PmonGlobalContext context, PartyId partyId, MonPartyDecision<PmonDecision> decision) {

        var ref = new Ref<>(true);
        var party = context.parties.get(partyId);
        for (var e: decision.monDecisions.entrySet()) {
            var monId = e.getKey();
            var mon = party.monsOnField.get(monId);
            if (!party.monsOnField.containsKey(monId)) {

                return false;
            }
            PmonDecision monDecision = e.getValue();
            monDecision.call(new PmonDecision.Handlers() {

                @Override
                public void pass(PmonDecisionToPass decision) {
                    if (!ruleset.isAlive(mon)) {
                        ref.set(false); // mon fainted - must NOT pass
                    }
                }
                @Override
                public void switchIn(PmonDecisionToSwitchIn decision) {

                    if (!I.range(party.mons.size()).toSet().contains(decision.monSwitchInIndex)) {
                        ref.set(false); return;
                    }
                    for (var condition: mon.status.statusConditions.values()) {
                        if (!condition.attrs.allowSwitchOut) {
                            ref.set(false); return;
                        }
                    }
                    var monToSwitchIn = party.mons.get(decision.monSwitchInIndex);
                    if (!ruleset.isAlive(monToSwitchIn)) {
                        ref.set(false); return;
                    }
                }
                @Override
                public void useMove(PmonDecisionToUseMove decision) {
                    if (!ruleset.isAlive(mon)) {
                        ref.set(false); return; // mon fainted - must NOT use move
                    }
                    if  (!I.range(mon.moves.size()).toSet().contains(decision.moveIndex)) {
                        ref.set(false); return;
                    }
                    var move = mon.moves.get(decision.moveIndex);
                    if (move.status.disabled || move.status.pp <= 0) {
                        ref.set(false); return;
                    }
                }
            });
            if (!ref.get()) {
                break;
            }
        }
        return ref.get();
    }
}
