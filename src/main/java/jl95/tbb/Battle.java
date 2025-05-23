package jl95.tbb;

import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.util.StrictMap;

import java.util.*;

import static jl95.lang.SuperPowers.*;

public class Battle<
        PartyEntry, InitialConditions,
        LocalContext, GlobalContext,
        Decision, GlobalUpdate, LocalUpdate
        > {

    public final Ruleset<
            PartyEntry, InitialConditions,
            LocalContext, GlobalContext,
            Decision, GlobalUpdate, LocalUpdate> ruleset;

    public Battle(Ruleset<
            PartyEntry, InitialConditions,
            LocalContext, GlobalContext,
            Decision, GlobalUpdate, LocalUpdate
            > ruleset) {

        this.ruleset = ruleset;
    }

    public interface Callbacks<LocalUpdate, LocalContext, GlobalContext> {

        void onGlobalContext(GlobalContext context);
        void onLocalContext(PartyId id, LocalContext context);
        void onLocalUpdate(PartyId id, LocalUpdate update);
    }

    public static class InterruptedException extends RuntimeException {}

    public Optional<PartyId> spawn(StrictMap<PartyId, PartyEntry> parties,
                                   InitialConditions initialConditions,
                                   StrictMap<PartyId, Function0<Decision>> decisionFunctionsMap,
                                   Callbacks<LocalUpdate, LocalContext, GlobalContext> callbacks,
                                   Function0<Boolean> toInterrupt) {

        var shuttindDown = new Ref<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shuttindDown.set(true)));
        var partyIds = strict(parties.keySet()).iter();
        var globalContext = ruleset.init(parties, initialConditions);
        callbacks.onGlobalContext(globalContext);
        var localContextGetter = function(() -> strict(I.of(partyIds)
                                                        .toMap(id -> id,
                                                               id -> ruleset.detLocalContext(globalContext, id))));
        var tellLocalContexts = method(() -> {
            var localContext = localContextGetter.apply();
            for (var partyId: partyIds) {
                callbacks.onLocalContext(partyId, localContext.get(partyId));
            }
        });
        var tellLocalUpdates = method((GlobalUpdate globalUpdate_) -> {
            for (var partyId: partyIds) {
                for (var localUpdate: ruleset.detLocalUpdates(globalUpdate_, partyId)) {
                    callbacks.onLocalUpdate(partyId, localUpdate);
                }
            }
        });
        tellLocalContexts.accept();
        var handleUpdates = method((Iterable<GlobalUpdate> updates) -> {
            for (GlobalUpdate globalUpdate: updates) {
                tellLocalUpdates.accept(globalUpdate);
                ruleset.update(globalContext, globalUpdate);
                callbacks.onGlobalContext(globalContext);
                tellLocalContexts.accept();
            }
        });
        var updatesAtStart = ruleset.detInitialUpdates(globalContext, initialConditions);
        handleUpdates.accept(updatesAtStart);
        while (true) {
            if (shuttindDown.get() || toInterrupt.apply()) {
                throw new Battle.InterruptedException();
            }
            var victorOptional = ruleset.detVictory(globalContext);
            if (victorOptional.isPresent()) {
                return victorOptional;
            }
            var localContextsMap = localContextGetter.apply();
            for (var e: localContextsMap.entrySet()) {
                callbacks.onLocalContext(e.getKey(), e.getValue());
            }
            var decisionsMap = strict(I.of(partyIds)
                                       .toMap(id -> id,
                                              id -> decisionFunctionsMap.get(id)
                                                                        .apply()));
            var updates = I.of(ruleset.detUpdates(globalContext, decisionsMap)).toList();
            handleUpdates.accept(updates);
        }
    }
}
