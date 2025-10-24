package jl95.tbb.mon;

import jl95.lang.variadic.Method1;
import jl95.tbb.PartyId;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import java.util.Optional;

public interface MonRuleset<
        Mon,
        PartyEntry extends MonPartyEntry<Mon>,
        Party extends MonParty<Mon>,
        InitialConditions,
        LocalContext extends MonLocalContext<Mon, Party>,
        GlobalContext extends MonGlobalContext<Mon, Party>,
        MonDecision,
        GlobalUpdate, LocalUpdate
        > {

    GlobalContext
    init(StrictMap<PartyId, PartyEntry> parties,
         InitialConditions initialConditions);

    void
    detInitialUpdates(GlobalContext context,
                      InitialConditions initialConditions,
                      Method1<GlobalUpdate> updateHandler);

    LocalContext
    detLocalContext(GlobalContext context,
                    PartyId partyId);

    Boolean
    isValid(GlobalContext context,
            PartyId partyId,
            MonPartyDecision<MonDecision> decision);

    void
    handleUpdates(GlobalContext monGlobalContext,
                  StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap,
                  Method1<GlobalUpdate> updateHandler);

    void
    update(GlobalContext context,
           GlobalUpdate globalUpdate);

    Iterable<LocalUpdate>
    detLocalUpdates(GlobalUpdate globalUpdate,
                    PartyId partyId);

    Optional<PartyId>
    detWinner(GlobalContext context);

    StrictMap<PartyId, StrictSet<MonFieldPosition>>
    allowedToDecide(GlobalContext context);

    default jl95.tbb.Ruleset<
            PartyEntry,
            InitialConditions,
            LocalContext,
            GlobalContext,
            MonPartyDecision<MonDecision>,
            GlobalUpdate, LocalUpdate
            > upcast() {

        return new jl95.tbb.Ruleset<>() {

            @Override
            public GlobalContext init(StrictMap<PartyId, PartyEntry> parties, InitialConditions initialConditions) {
                return MonRuleset.this.init(parties, initialConditions);
            }

            @Override
            public void detInitialUpdates(GlobalContext monGlobalContext, InitialConditions initialConditions, Method1<GlobalUpdate> updateHandler) {
                MonRuleset.this.detInitialUpdates(monGlobalContext, initialConditions, updateHandler);
            }

            @Override
            public LocalContext detLocalContext(GlobalContext monGlobalContext, PartyId partyId) {
                return MonRuleset.this.detLocalContext(monGlobalContext, partyId);
            }

            @Override
            public Boolean isValid(GlobalContext context, PartyId partyId, MonPartyDecision<MonDecision> monDecisionMonPartyDecision) {
                return MonRuleset.this.isValid(context, partyId, monDecisionMonPartyDecision);
            }

            @Override
            public void handleUpdates(GlobalContext monGlobalContext, StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap, Method1<GlobalUpdate> updateHandler) {
                MonRuleset.this.handleUpdates(monGlobalContext, decisionsMap, updateHandler);
            }

            @Override
            public Iterable<LocalUpdate> detLocalUpdates(GlobalUpdate globalUpdate, PartyId partyId) {
                return MonRuleset.this.detLocalUpdates(globalUpdate, partyId);
            }

            @Override
            public void update(GlobalContext monGlobalContext, GlobalUpdate globalUpdate) {
                MonRuleset.this.update(monGlobalContext, globalUpdate);
            }

            @Override
            public Optional<PartyId> detWinner(GlobalContext monGlobalContext) {
                return MonRuleset.this.detWinner(monGlobalContext);
            }
        };
    }
}
