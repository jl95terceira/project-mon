package jl95.tbb;

import jl95.lang.variadic.Method1;
import jl95.util.StrictMap;

import java.util.Optional;

public interface Ruleset<
        PartyEntry,
        InitialConditions,
        LocalContext,
        GlobalContext,
        Decision,
        GlobalUpdate,
        LocalUpdate
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
            Decision decision);

    void
    handleUpdates(GlobalContext context,
                  StrictMap<PartyId, Decision> decisionsMap,
                  Method1<GlobalUpdate> updateHandler);

    Iterable<LocalUpdate>
    detLocalUpdates(GlobalUpdate globalUpdate,
                    PartyId partyId);

    void
    update(GlobalContext context,
           GlobalUpdate globalUpdate);

    Optional<PartyId>
    detWinner(GlobalContext context);
}
