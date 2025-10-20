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

        static class Editable<LU, LC, GC> implements Listeners<LU, LC, GC> {

            public Method1<GC> onGlobalContext = gc -> {};
            public Method1<LC> onLocalContext = lc -> {};
            public Method2<PartyId,LU> onLocalUpdate = (p,lu) -> {};

            @Override public void onGlobalContext(GC gc) {onGlobalContext.accept(gc);}
            @Override public void onLocalContext(PartyId id, LC lc) {onLocalContext.accept(lc);}
            @Override public void onLocalUpdate(PartyId id, LU lu) {onLocalUpdate.accept(id,lu);}
        }
        static <LU, LC, GC> Listeners<LU, LC, GC> ignore() { return new Editable<>(); }
    }

    public static class InterruptedException extends RuntimeException {}
    public static class BadDecisionException extends RuntimeException {}

    public Optional<PartyId> spawn(StrictMap<PartyId, PartyEntry> parties,
                                   InitialConditions initialConditions,
                                   Function0<StrictMap<PartyId, Decision>> decisionFunction,
                                   Listeners<LocalUpdate, LocalContext, GlobalContext> listeners,
                                   Function0<Boolean> toInterrupt) {

        var shuttindDown = new P<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shuttindDown.set(true)));
        var partyIds = strict(parties.keySet());
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
            var winnerOptional = ruleset.detWinner(globalContext);
            if (winnerOptional.isPresent()) {
                return winnerOptional;
            }
            var localContextsMap = localContextGetter.apply();
            for (var e: localContextsMap.entrySet()) {
                listeners.onLocalContext(e.getKey(), e.getValue());
            }
            var decisionsMap = decisionFunction.apply();
            for (var decision: decisionsMap.values()) {
                if (decision == null) {
                    throw new BadDecisionException();
                }
            }
            var updates = I.of(ruleset.detUpdates(globalContext, decisionsMap)).toList();
            handleUpdates.accept(updates);
        }
    }
}
