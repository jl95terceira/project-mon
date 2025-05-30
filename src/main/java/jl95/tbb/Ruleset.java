package jl95.tbb;

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

    Iterable<GlobalUpdate>
    detInitialUpdates(GlobalContext context,
                      InitialConditions initialConditions);

    LocalContext
    detLocalContext(GlobalContext context,
                    PartyId partyId);

    Boolean
    isValid(GlobalContext context,
            PartyId partyId,
            Decision decision);

    Iterable<GlobalUpdate>
    detUpdates(GlobalContext context,
               StrictMap<PartyId, Decision> decisionsMap);

    Iterable<LocalUpdate>
    detLocalUpdates(GlobalUpdate globalUpdate,
                    PartyId partyId);

    void
    update(GlobalContext context,
           GlobalUpdate globalUpdate);

    Optional<PartyId>
    detWinner(GlobalContext context);
}
