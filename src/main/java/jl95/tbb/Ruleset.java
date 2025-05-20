package jl95.tbb;

import jl95.util.StrictMap;

import java.util.Optional;

public interface Ruleset<
        PartyEntry,
        InitialConditions,
        LocalContext,
        GlobalContext,
        Decision,
        Update
        > {

    GlobalContext
    init(StrictMap<PartyId, PartyEntry> parties,
         InitialConditions initialConditions);

    Iterable<Update>
    evalStart(GlobalContext context,
              InitialConditions initialConditions);

    LocalContext
    detLocalContext(GlobalContext context,
                    PartyId partyId);

    Iterable<Update>
    evalDecisions(GlobalContext context,
                  StrictMap<PartyId, Decision> decisionsMap);

    void
    update(GlobalContext context,
           Update update);

    Optional<PartyId>
    evalVictory(GlobalContext context);
}
