package jl95.tbb.mon;

import jl95.lang.P;
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
        Mon, PartyEntry extends MonPartyEntry<Mon>,
        Party extends MonParty<Mon>,
        InitialConditions,
        LocalContext extends MonLocalContext<Mon, Party>,
        GlobalContext extends MonGlobalContext<Mon, Party>,
        MonDecision,
        GlobalUpdate, LocalUpdate
        > {

    public final MonRuleset<
            Mon, PartyEntry, Party,
            InitialConditions,
            LocalContext, GlobalContext,
            MonDecision,
            GlobalUpdate, LocalUpdate
            > ruleset;
    public final jl95.tbb.Battle<
            PartyEntry,
            InitialConditions,
            LocalContext, GlobalContext,
            MonPartyDecision<MonDecision>,
            GlobalUpdate,
            LocalUpdate
            > upcastBattle;

    public MonBattle(MonRuleset<
                Mon, PartyEntry, Party,
                InitialConditions,
                LocalContext, GlobalContext,
                MonDecision,
                GlobalUpdate, LocalUpdate
                > ruleset) {

        this.ruleset = ruleset;
        this.upcastBattle = new jl95.tbb.Battle<>(ruleset.upcast());
    }

    public Optional<PartyId> spawn(
            StrictMap<PartyId, PartyEntry> parties,
            InitialConditions initialConditions,
            Function1<StrictMap<PartyId, StrictMap<MonFieldPosition, MonDecision>>,
                      StrictMap<PartyId, StrictSet<MonFieldPosition>>> decisionFunction,
            Battle.Handler<LocalUpdate, LocalContext, GlobalContext> handler,
            Function0<Boolean> toInterrupt
    ) {

        var globalContextRef = new P<GlobalContext>(null);
        var localContextsMap = strict(new HashMap<PartyId, LocalContext>());
        var extendedCallbacks = new Battle.Handler<LocalUpdate, LocalContext, GlobalContext>() {

            @Override
            public void onGlobalContext(GlobalContext globalContext) {
                globalContextRef.set(globalContext);
                handler.onGlobalContext(globalContext);
            }

            @Override public void onLocalContext(PartyId id, LocalContext monLocalContext) {
                localContextsMap.put(id, monLocalContext);
                handler.onLocalContext(id, monLocalContext);
            }
            @Override public void onLocalUpdate(PartyId id, LocalUpdate localUpdate) {
                handler.onLocalUpdate(id, localUpdate);
            }
        };
        return this.upcastBattle.spawn(parties, initialConditions, () -> {

            var allowedToDecide = ruleset.allowedToDecide(globalContextRef.get());
            var partyDecisionsMap = decisionFunction.apply(allowedToDecide);
            return strict(I.of(partyDecisionsMap.entrySet()).toMap(Map.Entry::getKey, e -> {
                var partyDecision = new MonPartyDecision<MonDecision>();
                partyDecision.monDecisions.putAll(e.getValue());
                return partyDecision;
            }));
        }, extendedCallbacks, toInterrupt);
    }
}
