package jl95.tbb.mon;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.tbb.PartyId;
import jl95.lang.I;
import jl95.util.StrictMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jl95.lang.SuperPowers.*;

public class MonBattle<
        Mon,
        InitialConditions,
        MonDecision,
        GlobalUpdate, LocalUpdate
        > {

    public final jl95.tbb.Battle<
            MonPartyEntry<Mon>,
            InitialConditions,
            MonLocalContext<Mon>,
            MonGlobalContext<Mon>,
            MonPartyDecision<MonDecision>,
            GlobalUpdate,
            LocalUpdate
            > upcastBattle;

    public MonBattle(MonRuleset<
                Mon,
                InitialConditions,
                MonDecision,
                GlobalUpdate, LocalUpdate
                > ruleset) {

        this.upcastBattle = new jl95.tbb.Battle<>(ruleset.upcast());
    }

    public Optional<PartyId> spawn(
            StrictMap<PartyId, MonPartyEntry<Mon>> parties,
            InitialConditions initialConditions,
            StrictMap<PartyId, Function1<MonDecision, MonPartyMonId>> decisionFunctionsMap,
            jl95.tbb.Battle.Callbacks<LocalUpdate, MonLocalContext<Mon>> callbacks,
            Function0<Boolean> toInterrupt
    ) {

        var localContextsMap = strict(new HashMap<PartyId, MonLocalContext<Mon>>());
        var extendedCallbacks = new jl95.tbb.Battle.Callbacks<LocalUpdate, MonLocalContext<Mon>>() {

            @Override public void onLocalContext(PartyId id, MonLocalContext<Mon> monLocalContext) {
                localContextsMap.put(id, monLocalContext);
                callbacks.onLocalContext(id, monLocalContext);
            }
            @Override public void onLocalUpdate(PartyId id, LocalUpdate localUpdate) {
                callbacks.onLocalUpdate(id, localUpdate);
            }
        };
        return this.upcastBattle.spawn(parties, initialConditions, strict(I.of(decisionFunctionsMap.entrySet()).toMap(Map.Entry::getKey, e -> {
            var partyId = e.getKey();
            var monDecisionFunction = e.getValue();
            return () -> {
                var partyDecision = new MonPartyDecision<MonDecision>();
                for (var monId: localContextsMap.get(partyId).ownParty.monsOnField.keySet()) {
                    partyDecision.monDecisions.put(monId, monDecisionFunction.apply(monId));
                }
                return partyDecision;
            };
        })), extendedCallbacks, toInterrupt);
    }
}
