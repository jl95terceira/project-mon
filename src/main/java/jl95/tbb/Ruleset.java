package jl95.tbb;

import jl95.util.StrictMap;

import java.util.Optional;

public interface Ruleset<
        PartyEntry,
        InitialConditions,
        LocalContext,
        GlobalContext,
        Decision,
        LocalUpdate,
        GlobalUpdate
        > {

    GlobalContext
    init(StrictMap<PartyId, PartyEntry> parties,
         InitialConditions initialConditions);

    Iterable<GlobalUpdate>
    evalStart(GlobalContext context,
              InitialConditions initialConditions);

    LocalContext
    detLocalContext(GlobalContext context,
                    PartyId partyId);

    Iterable<GlobalUpdate>
    evalDecisions(GlobalContext context,
                  StrictMap<PartyId, Decision> decisionsMap);

    void
    update(GlobalContext context,
           GlobalUpdate update);

    LocalUpdate
    detLocalUpdate(GlobalUpdate context,
                   PartyId partyId);

    Optional<PartyId>
    evalVictory(GlobalContext context);
}
