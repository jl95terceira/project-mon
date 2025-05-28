package jl95.tbb;

import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.util.StrictMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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

    public interface Listeners<LocalUpdate, LocalContext, GlobalContext> {

        void onGlobalContext(GlobalContext context);
        void onLocalContext(PartyId id, LocalContext context);
        void onLocalUpdate(PartyId id, LocalUpdate update);
    }

    public static class InterruptedException extends RuntimeException {}

    public Optional<PartyId> spawn(StrictMap<PartyId, PartyEntry> parties,
                                   InitialConditions initialConditions,
                                   Function1<Decision, PartyId> decisionFunction,
                                   Listeners<LocalUpdate, LocalContext, GlobalContext> listeners,
                                   Function0<Boolean> toInterrupt) {

        var shuttindDown = new Ref<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shuttindDown.set(true)));
        var partyIds = strict(parties.keySet()).iter();
        var globalContext = ruleset.init(parties, initialConditions);
        listeners.onGlobalContext(globalContext);
        var localContextGetter = function(() -> strict(I.of(partyIds)
                                                        .toMap(id -> id,
                                                               id -> ruleset.detLocalContext(globalContext, id))));
        var tellLocalContexts = method(() -> {
            var localContext = localContextGetter.apply();
            for (var partyId: partyIds) {
                listeners.onLocalContext(partyId, localContext.get(partyId));
            }
        });
        var tellLocalUpdates = method((GlobalUpdate globalUpdate_) -> {
            for (var partyId: partyIds) {
                for (var localUpdate: ruleset.detLocalUpdates(globalUpdate_, partyId)) {
                    listeners.onLocalUpdate(partyId, localUpdate);
                }
            }
        });
        tellLocalContexts.accept();
        var decisionsThreadPool = new ScheduledThreadPoolExecutor(parties.size());
        var handleUpdates = method((Iterable<GlobalUpdate> updates) -> {
            for (GlobalUpdate globalUpdate: updates) {
                tellLocalUpdates.accept(globalUpdate);
                ruleset.update(globalContext, globalUpdate);
                listeners.onGlobalContext(globalContext);
                tellLocalContexts.accept();
            }
        });
        var updatesAtStart = ruleset.detInitialUpdates(globalContext, initialConditions);
        handleUpdates.accept(updatesAtStart);
        var toStop = function(() -> shuttindDown.get() || toInterrupt.apply());
        while (true) {
            if (toStop.apply()) {
                throw new Battle.InterruptedException();
            }
            var victorOptional = ruleset.detVictory(globalContext);
            if (victorOptional.isPresent()) {
                return victorOptional;
            }
            var localContextsMap = localContextGetter.apply();
            for (var e: localContextsMap.entrySet()) {
                listeners.onLocalContext(e.getKey(), e.getValue());
            }
            StrictMap<PartyId, CompletableFuture<Decision>> decisionPromisesMap = strict(I.of(partyIds).toMap(p -> p, p -> new CompletableFuture<>()));
            for (var partyId: partyIds) {
                decisionsThreadPool.execute(() -> {
                    while (!toStop.apply()) {
                        var decision = decisionFunction.apply(partyId);
                        if (ruleset.isValid(globalContext, partyId, decision)) {
                            decisionPromisesMap.get(partyId).complete(decision);
                            break;
                        }
                    }
                });
            }
            StrictMap<PartyId, Decision> decisionsMap = strict(I.of(decisionPromisesMap.entrySet()).toMap(e -> e.getKey(), e -> uncheck(() -> e.getValue().get())));
            if (toStop.apply()) {
                throw new Battle.InterruptedException();
            }
            var updates = I.of(ruleset.detUpdates(globalContext, decisionsMap)).toList();
            handleUpdates.accept(updates);
        }
    }
}
