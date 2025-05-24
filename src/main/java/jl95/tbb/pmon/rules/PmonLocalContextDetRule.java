package jl95.tbb.pmon.rules;

import jl95.lang.I;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonLocalContext;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonFoeView;
import jl95.tbb.pmon.PmonLocalContext;
import jl95.tbb.pmon.PmonRuleset;

import java.util.Map;

import static jl95.lang.SuperPowers.strict;

public class PmonLocalContextDetRule {

    public final PmonRuleset ruleset;

    public PmonLocalContextDetRule(PmonRuleset ruleset) {this.ruleset = ruleset;}

    public PmonLocalContext detLocalContext(MonGlobalContext<Pmon> context, PartyId ownPartyId) {

        var foePartiesView = strict(I
                .of(context.parties.keySet())
                .filter(id -> !id.equals(ownPartyId))
                .toMap(id -> id, id -> strict(I
                        .of(context.parties.get(id).monsOnField.entrySet())
                        .toMap(Map.Entry::getKey, e -> {
                            var foeMon = e.getValue();
                            var foeMonView = new PmonFoeView(foeMon.id);
                            foeMonView.types = foeMon.attrs.types;
                            foeMonView.status = foeMon.status;
                            return foeMonView;
                        }))));
        return new PmonLocalContext(context.parties.get(ownPartyId), foePartiesView);
    }
}
