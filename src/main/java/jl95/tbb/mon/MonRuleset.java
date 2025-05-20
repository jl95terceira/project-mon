package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.lang.I;
import jl95.lang.variadic.Tuple2;
import jl95.util.StrictMap;

import java.util.Optional;

public interface MonRuleset<
        Mon,
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

    MonLocalContext<Mon>
    detLocalContext(MonGlobalContext<Mon> context,
                    PartyId partyId);

    void
    update(MonGlobalContext<Mon> context,
           GlobalUpdate globalUpdate);

    LocalUpdate
    detLocalUpdate(GlobalUpdate globalUpdate,
                   PartyId partyId);

    Optional<PartyId>
    detVictory(MonGlobalContext<Mon> context);

    Iterable<Tuple2<PartyId, MonPartyMonId>>
    prioritised(MonGlobalContext<Mon> context,
                StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap);

    Iterable<GlobalUpdate>
    detDecisionsPerMon(MonGlobalContext<Mon> context,
                       PartyId partyId,
                       MonPartyMonId monId,
                       MonDecision monDecision);

    Boolean
    allowedToMove(MonGlobalContext<Mon> context,
                  PartyId partyId,
                  MonPartyMonId monId);

    default jl95.tbb.Ruleset<
            MonPartyEntry<Mon>,
            InitialConditions,
            MonLocalContext<Mon>,
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
            public MonLocalContext<Mon> detLocalContext(MonGlobalContext<Mon> monGlobalContext, PartyId partyId) {
                return MonRuleset.this.detLocalContext(monGlobalContext, partyId);
            }

            @Override
            public Iterable<GlobalUpdate> detUpdates(MonGlobalContext<Mon> monGlobalContext, StrictMap<PartyId, MonPartyDecision<MonDecision>> decisionsMap) {
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
                            return detDecisionsPerMon(monGlobalContext,
                                    partyId,
                                    monId,
                                    decisionsMap.get(partyId).monDecisions.get(monId));
                        });
            }

            @Override
            public LocalUpdate detLocalUpdate(GlobalUpdate globalUpdate, PartyId partyId) {
                return MonRuleset.this.detLocalUpdate(globalUpdate, partyId);
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
