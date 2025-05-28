package jl95.tbb.pmon.rules;

import jl95.lang.Ref;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyDecision;
import jl95.tbb.pmon.PmonDecision;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonRuleset;
import jl95.tbb.pmon.decision.PmonDecisionByPass;
import jl95.tbb.pmon.decision.PmonDecisionBySwitchIn;
import jl95.tbb.pmon.decision.PmonDecisionByUseMove;

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
                public void pass(PmonDecisionByPass decision) {
                    if (!ruleset.isAlive(mon)) {
                        ref.set(false); // mon fainted - must NOT pass
                    }
                }
                @Override
                public void switchIn(PmonDecisionBySwitchIn decision) {

                    if (decision.monSwitchInIndex < 0 || decision.monSwitchInIndex >= party.mons.size()) {
                        ref.set(false);
                    }
                    else {
                        var monToSwitchIn = party.mons.get(decision.monSwitchInIndex);
                        if (!ruleset.isAlive(monToSwitchIn)) {
                            ref.set(false);
                        }
                    }
                }
                @Override
                public void useMove(PmonDecisionByUseMove decision) {
                    if (!ruleset.isAlive(mon)) {
                        ref.set(false); // mon fainted - must NOT use move
                    }
                    else {
                        if (decision.moveIndex < 0 || decision.moveIndex >= mon.moves.size()) {
                            ref.set(false);
                        } else {
                            var move = mon.moves.get(decision.moveIndex);
                            if (move.status.disabled || move.status.pp <= 0) {
                                ref.set(false);
                            }
                        }
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
