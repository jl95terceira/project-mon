package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.lang.I;
import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.*;
import jl95.util.StrictMap;

import java.util.Map;
import java.util.Optional;

public class PmonRuleset implements MonRuleset<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    @Override
    public MonGlobalContext<Pmon> init(StrictMap<PartyId, MonPartyEntry<Pmon>> parties, PmonInitialConditions pmonInitialConditions) {
        var context = new MonGlobalContext<Pmon>();
        for (var e: parties.entrySet()) {
            var partyId = e.getKey();
            var partyEntry = e.getValue();
            context.parties.put(partyId, MonParty.fromEntry(partyEntry));
        }
        return context;
    }

    @Override
    public Iterable<PmonUpdate> detInitialUpdates(MonGlobalContext<Pmon> context, PmonInitialConditions pmonInitialConditions) {
        return I();
    }

    @Override
    public MonLocalContext<Pmon, PmonFoeView> detLocalContext(MonGlobalContext<Pmon> context, PartyId ownPartyId) {
        var foePartiesView = strict(I
            .of(context.parties.keySet())
            .filter(id -> !id.equals(ownPartyId))
            .toMap(id -> id, id -> strict(I
                .of(context.parties.get(id).monsOnField.entrySet())
                .toMap(Map.Entry::getKey, e -> {
                    var foeMon = e.getValue();
                    var foeMonView = new PmonFoeView(foeMon.id);
                    foeMonView.types = foeMon.types;
                    foeMonView.status = foeMon.status;
                    return foeMonView;
                }))));
        return new MonLocalContext<>(context.parties.get(ownPartyId), foePartiesView);
    }

    @Override
    public void update(MonGlobalContext<Pmon> context, PmonUpdate pmonUpdate) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public PmonUpdate detLocalUpdate(PmonUpdate pmonUpdate, PartyId partyId) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Optional<PartyId> detVictory(MonGlobalContext<Pmon> context) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Iterable<Tuple2<PartyId, MonPartyMonId>> prioritised(MonGlobalContext<Pmon> context, StrictMap<PartyId, MonPartyDecision<PmonDecision>> decisionsMap) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Iterable<PmonUpdate> detDecisionsPerMon(MonGlobalContext<Pmon> context, PartyId partyId, MonPartyMonId monId, PmonDecision pmonDecision) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Boolean allowedToMove(MonGlobalContext<Pmon> context, PartyId partyId, MonPartyMonId monId) {
        throw new UnsupportedOperationException(); //TODO
    }
}