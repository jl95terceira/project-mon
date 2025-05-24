package jl95.tbb.pmon.rules;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonRuleset;

import static jl95.lang.SuperPowers.strict;

public class PmoRuleToAllowDecision {

    public final PmonRuleset ruleset;

    public PmoRuleToAllowDecision(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Boolean allowDecide(MonGlobalContext<Pmon> context, PartyId partyId, MonParty.MonId monId) {

        var mon = context.parties.get(partyId).monsOnField.get(monId);
        return true; //TODO: should be based on move lock-in, charging / recharging, etc
    }
}
