package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonLocalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonFoeView;
import jl95.tbb.pmon.PmonRuleset;

import java.util.Map;

import static jl95.lang.SuperPowers.strict;

public class PmonDecisionAllowanceRule {

    public final PmonRuleset ruleset;

    public PmonDecisionAllowanceRule(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public Boolean allowDecide(MonGlobalContext<Pmon> context, PartyId partyId, MonParty.MonId monId) {

        var mon = context.parties.get(partyId).monsOnField.get(monId);
        return true; //TODO: should be based on move lock-in, charging / recharging, etc
    }
}
