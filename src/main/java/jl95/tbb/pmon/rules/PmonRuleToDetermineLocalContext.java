package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.PartyId;
import jl95.tbb.pmon.PmonPartyPublicView;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.tbb.pmon.PmonLocalContext;
import jl95.tbb.pmon.PmonRuleset;

import java.util.Map;

import static jl95.lang.SuperPowers.strict;

public class PmonRuleToDetermineLocalContext {

    public final PmonRuleset ruleset;

    public PmonRuleToDetermineLocalContext(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public PmonLocalContext detLocalContext(PmonGlobalContext context, PartyId ownPartyId) {

        var partyViews = strict(I
                .of(context.parties.keySet())
//                .filter(id -> !id.equals(ownPartyId))
                .toMap(id -> id, id -> strict(I
                        .of(context.parties.get(id).monsOnField.entrySet())
                        .toMap(Map.Entry::getKey, e -> {
                            var mon = e.getValue();
                            var monView = new PmonPartyPublicView(mon.id);
                            monView.types = mon.types;
                            monView.status = mon.status;
                            return monView;
                        }))));
        return new PmonLocalContext(context.parties.get(ownPartyId), partyViews);
    }
}
