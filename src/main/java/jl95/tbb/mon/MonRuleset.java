package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.util.StrictMap;

import java.util.Optional;

public interface MonRuleset<
        Mon, FoeMonView,
        InitialConditions,
        MonDecision,
        GlobalUpdate, LocalUpdate
        > {

    MonGlobalContext<Mon>
    init(StrictMap<PartyId, MonPartyEntry<Mon>> parties,
         InitialConditions initialConditions);

    Iterable<GlobalUpdate>
    detInitialUpdates(MonGlobalContext<Mon> context,
                      InitialConditions initialConditions);

    MonLocalContext<Mon, FoeMonView>
    detLocalContext(MonGlobalContext<Mon> context,
                    PartyId partyId);

    Iterable<GlobalUpdate>
    detUpdates(MonGlobalContext<Mon> monGlobalContext,
               StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap);

    void
    update(MonGlobalContext<Mon> context,
           GlobalUpdate globalUpdate);

    Iterable<LocalUpdate>
    detLocalUpdates(GlobalUpdate globalUpdate,
                    PartyId partyId);

    Optional<PartyId>
    detVictory(MonGlobalContext<Mon> context);

    Boolean
    allowedToMove(MonGlobalContext<Mon> context,
                  PartyId partyId,
                  MonParty.MonId monId);

    default jl95.tbb.Ruleset<
            MonPartyEntry<Mon>,
            InitialConditions,
            MonLocalContext<Mon, FoeMonView>,
            MonGlobalContext<Mon>,
            MonPartyDecision<MonDecision>,
            GlobalUpdate, LocalUpdate
            > upcast() {

        return new jl95.tbb.Ruleset<>() {

            @Override
            public MonGlobalContext<Mon> init(StrictMap<PartyId, MonPartyEntry<Mon>> parties, InitialConditions initialConditions) {
                return MonRuleset.this.init(parties, initialConditions);
            }

            @Override
            public Iterable<GlobalUpdate> detInitialUpdates(MonGlobalContext<Mon> monGlobalContext, InitialConditions initialConditions) {
                return MonRuleset.this.detInitialUpdates(monGlobalContext, initialConditions);
            }

            @Override
            public MonLocalContext<Mon, FoeMonView> detLocalContext(MonGlobalContext<Mon> monGlobalContext, PartyId partyId) {
                return MonRuleset.this.detLocalContext(monGlobalContext, partyId);
            }

            @Override
            public Iterable<GlobalUpdate> detUpdates(MonGlobalContext<Mon> monGlobalContext, StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap) {
                return MonRuleset.this.detUpdates(monGlobalContext, decisionsMap);
            }

            @Override
            public Iterable<LocalUpdate> detLocalUpdates(GlobalUpdate globalUpdate, PartyId partyId) {
                return MonRuleset.this.detLocalUpdates(globalUpdate, partyId);
            }

            @Override
            public void update(MonGlobalContext<Mon> monGlobalContext, GlobalUpdate globalUpdate) {
                MonRuleset.this.update(monGlobalContext, globalUpdate);
            }

            @Override
            public Optional<PartyId> detVictory(MonGlobalContext<Mon> monGlobalContext) {
                return MonRuleset.this.detVictory(monGlobalContext);
            }
        };
    }
}
