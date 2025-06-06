package jl95.tbb.mon;

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

    Iterable<GlobalUpdate>
    detInitialUpdates(GlobalContext context,
                      InitialConditions initialConditions);

    LocalContext
    detLocalContext(GlobalContext context,
                    PartyId partyId);

    Boolean
    isValid(GlobalContext context,
            PartyId partyId,
            MonPartyDecision<MonDecision> decision);

    Iterable<GlobalUpdate>
    detUpdates(GlobalContext monGlobalContext,
               StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap);

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
            public Iterable<GlobalUpdate> detInitialUpdates(GlobalContext monGlobalContext, InitialConditions initialConditions) {
                return MonRuleset.this.detInitialUpdates(monGlobalContext, initialConditions);
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
            public Iterable<GlobalUpdate> detUpdates(GlobalContext monGlobalContext, StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap) {
                return MonRuleset.this.detUpdates(monGlobalContext, decisionsMap);
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
