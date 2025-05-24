package jl95.tbb.mon;

import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.lang.I;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jl95.lang.SuperPowers.*;

public class MonBattle<
        Mon, FoeMonView,
        InitialConditions,
        LocalContext extends MonLocalContext<Mon, FoeMonView>, GlobalContext extends MonGlobalContext<Mon>,
        MonDecision,
        GlobalUpdate, LocalUpdate
        > {

    public final MonRuleset<
            Mon, FoeMonView,
            InitialConditions,
            LocalContext, GlobalContext,
            MonDecision,
            GlobalUpdate, LocalUpdate
            > ruleset;
    public final jl95.tbb.Battle<
            MonPartyEntry<Mon>,
            InitialConditions,
            LocalContext, GlobalContext,
            MonPartyDecision<MonDecision>,
            GlobalUpdate,
            LocalUpdate
            > upcastBattle;

    public MonBattle(MonRuleset<
                Mon, FoeMonView,
                InitialConditions,
                LocalContext, GlobalContext,
                MonDecision,
                GlobalUpdate, LocalUpdate
                > ruleset) {

        this.ruleset = ruleset;
        this.upcastBattle = new jl95.tbb.Battle<>(ruleset.upcast());
    }

    public Optional<PartyId> spawn(
            StrictMap<PartyId, MonPartyEntry<Mon>> parties,
            InitialConditions initialConditions,
            StrictMap<PartyId, Function1<StrictMap<MonParty.MonId, MonDecision>, StrictSet<MonParty.MonId>>> decisionFunctionsMap,
            Battle.Listeners<LocalUpdate, MonLocalContext<Mon, FoeMonView>, MonGlobalContext<Mon>> listeners,
            Function0<Boolean> toInterrupt
    ) {

        var globalContextRef = new Ref<GlobalContext>();
        var localContextsMap = strict(new HashMap<PartyId, LocalContext>());
        var extendedCallbacks = new Battle.Listeners<LocalUpdate, LocalContext, GlobalContext>() {

            @Override
            public void onGlobalContext(GlobalContext globalContext) {
                globalContextRef.set(globalContext);
                listeners.onGlobalContext(globalContext);
            }

            @Override public void onLocalContext(PartyId id, LocalContext monLocalContext) {
                localContextsMap.put(id, monLocalContext);
                listeners.onLocalContext(id, monLocalContext);
            }
            @Override public void onLocalUpdate(PartyId id, LocalUpdate localUpdate) {
                listeners.onLocalUpdate(id, localUpdate);
            }
        };
        return this.upcastBattle.spawn(parties, initialConditions, strict(I.of(decisionFunctionsMap.entrySet()).toMap(Map.Entry::getKey, e -> {
            var partyId = e.getKey();
            var monDecisionFunction = e.getValue();
            return function(() -> {
                var partyDecision = new MonPartyDecision<MonDecision>();
                var monDecisions = monDecisionFunction.apply(strict(I.of(globalContextRef.get().parties.get(partyId).monsOnField.keySet())
                        .filter(monId -> ruleset.allowDecide(globalContextRef.get(), partyId, monId))
                        .toSet()));
                partyDecision.monDecisions.putAll(monDecisions);
                return partyDecision;
            });
        })), extendedCallbacks, toInterrupt);
    }
}
