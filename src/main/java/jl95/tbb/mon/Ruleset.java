package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.lang.I;
import jl95.lang.variadic.Tuple2;
import jl95.util.StrictMap;

import java.util.Optional;

public interface Ruleset<
        Mon,
        InitialConditions,
        MonDecision,
        Update
        > {

    GlobalContext<Mon>
    init(StrictMap<PartyId, PartyEntry<Mon>> parties,
         InitialConditions initialConditions);

    Iterable<Update>
    evalStart(GlobalContext<Mon> context,
              InitialConditions initialConditions);

    LocalContext<Mon>
    detLocalContext(GlobalContext<Mon> context,
                    PartyId partyId);

    void
    update(GlobalContext<Mon> context,
           Update update);

    Optional<PartyId>
    evalVictory(GlobalContext<Mon> context);

    Iterable<Tuple2<PartyId, PartyMonId>>
    prioritised(GlobalContext<Mon> context,
                StrictMap<PartyId, PartyDecision<MonDecision>> decisionsMap);

    Iterable<Update>
    evalDecisionsPerMon(GlobalContext<Mon> context,
                        PartyId partyId,
                        PartyMonId monId,
                        MonDecision monDecision);

    Boolean
    allowedToMove(GlobalContext<Mon> context,
                  PartyId partyId,
                  PartyMonId monId);

    default jl95.tbb.Ruleset<
            PartyEntry<Mon>,
            InitialConditions,
            LocalContext<Mon>,
            GlobalContext<Mon>,
            PartyDecision<MonDecision>,
            Update
            > upcast() {

        return new jl95.tbb.Ruleset<>() {

            @Override
            public GlobalContext<Mon> init(StrictMap<PartyId, PartyEntry<Mon>> parties, InitialConditions initialConditions) {
                return Ruleset.this.init(parties, initialConditions);
            }

            @Override
            public Iterable<Update> evalStart(GlobalContext<Mon> monGlobalContext, InitialConditions initialConditions) {
                return Ruleset.this.evalStart(monGlobalContext, initialConditions);
            }

            @Override
            public LocalContext<Mon> detLocalContext(GlobalContext<Mon> monGlobalContext, PartyId partyId) {
                return Ruleset.this.detLocalContext(monGlobalContext, partyId);
            }

            @Override
            public Iterable<Update> evalDecisions(GlobalContext<Mon> monGlobalContext, StrictMap<PartyId, PartyDecision<MonDecision>> decisionsMap) {
                var monDecisionsPrioritised = prioritised(monGlobalContext, decisionsMap);
                return I.of(monDecisionsPrioritised)
                        .filter(t -> {
                            var partyId = t.a1;
                            var monId = t.a2;
                            return allowedToMove(monGlobalContext, partyId, monId);
                        })
                        .flatmap(t -> {
                            var partyId = t.a1;
                            var monId = t.a2;
                            return evalDecisionsPerMon(monGlobalContext,
                                    partyId,
                                    monId,
                                    decisionsMap.get(partyId).monDecisions.get(monId));
                        });
            }

            @Override
            public void update(GlobalContext<Mon> monGlobalContext, Update update) {
                Ruleset.this.update(monGlobalContext, update);
            }

            @Override
            public Optional<PartyId> evalVictory(GlobalContext<Mon> monGlobalContext) {
                return Ruleset.this.evalVictory(monGlobalContext);
            }
        };
    }
}
