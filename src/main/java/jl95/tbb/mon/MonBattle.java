package jl95.tbb.mon;

import jl95.lang.Ref;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function2;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.lang.I;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import java.util.HashMap;
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
            Function2<StrictMap<MonFieldPosition, MonDecision>, PartyId, StrictSet<MonFieldPosition>> decisionFunction,
            Battle.Listeners<LocalUpdate, LocalContext, GlobalContext> listeners,
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
        return this.upcastBattle.spawn(parties, initialConditions, () -> strict(I.of(parties.keySet()).toMap(partyId -> partyId, partyId -> {

            var partyDecision = new MonPartyDecision<MonDecision>();
            var allowedToDecide = ruleset.allowedToDecide(globalContextRef.get());
            var monDecisions = decisionFunction.apply(partyId, strict(I.of(globalContextRef.get().parties.get(partyId).monsOnField.keySet())
                    .filter(monId -> (allowedToDecide.containsKey(partyId) && allowedToDecide.get(partyId).contains(monId)))
                    .toSet()));
            partyDecision.monDecisions.putAll(monDecisions);
            return partyDecision;
        })), extendedCallbacks, toInterrupt);
    }
}
