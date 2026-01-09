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

    public interface Handler<LocalUpdate, LocalContext, GlobalContext> {

        void onGlobalContext(GlobalContext context);
        void onLocalContext(PartyId id, LocalContext context);
        void onLocalUpdate(PartyId id, LocalUpdate update);

        static class Editable<LU, LC, GC> implements Handler<LU, LC, GC> {

            public Method1<GC> onGlobalContext = gc -> {};
            public Method2<PartyId,LC> onLocalContext = (p,lc) -> {};
            public Method2<PartyId,LU> onLocalUpdate = (p,lu) -> {};

            @Override public void onGlobalContext(GC gc) {onGlobalContext.accept(gc);}
            @Override public void onLocalContext(PartyId id, LC lc) {onLocalContext.accept(id,lc);}
            @Override public void onLocalUpdate(PartyId id, LU lu) {onLocalUpdate.accept(id,lu);}
        }
        static class Extendable<LU, LC, GC> implements Handler<LU, LC, GC> {

            public List<Method1<GC>> onGlobalContext = List();
            public List<Method2<PartyId,LC>> onLocalContext = List();
            public List<Method2<PartyId,LU>> onLocalUpdate = List();

            public final void add(Handler<LU,LC,GC> handler) {
                onGlobalContext.add(handler::onGlobalContext);
                onLocalContext.add(handler::onLocalContext);
                onLocalUpdate.add(handler::onLocalUpdate);
            }

            @Override public void onGlobalContext(GC gc) {onGlobalContext.forEach(cb -> cb.accept(gc));}
            @Override public void onLocalContext(PartyId id, LC lc) {onLocalContext.forEach(cb -> cb.accept(id,lc));}
            @Override public void onLocalUpdate(PartyId id, LU lu) {onLocalUpdate.forEach(cb -> cb.accept(id,lu));}
        }
        static <LU, LC, GC> Handler<LU, LC, GC> ignore() { return new Editable<>(); }
    }

    public static class InterruptedException extends RuntimeException {}
    public static class BadDecisionException extends RuntimeException {}

    public Optional<PartyId> start(StrictMap<PartyId, PartyEntry> parties,
                                   InitialConditions initialConditions,
                                   Function0<StrictMap<PartyId, Decision>> decisionFunction,
                                   Handler<LocalUpdate, LocalContext, GlobalContext> handler,
                                   Function0<Boolean> toInterrupt) {

        var shuttindDown = new P<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shuttindDown.set(true)));
        var partyIds = strict(parties.keySet());
        var globalContext = ruleset.init(parties, initialConditions);
        handler.onGlobalContext(globalContext);
        var localContextGetter = function(() -> strict(I.of(partyIds)
                                                        .toMap(id -> id,
                                                               id -> ruleset.detLocalContext(globalContext, id))));
        var tellLocalContexts = method(() -> {
            var localContext = localContextGetter.apply();
            for (var partyId: partyIds) {
                handler.onLocalContext(partyId, localContext.get(partyId));
            }
        });
        var tellLocalUpdates = method((GlobalUpdate globalUpdate_) -> {
            for (var partyId: partyIds) {
                for (var localUpdate: ruleset.detLocalUpdates(globalUpdate_, partyId)) {
                    handler.onLocalUpdate(partyId, localUpdate);
                }
            }
        });
        tellLocalContexts.accept();
        var handleUpdate = method((GlobalUpdate update) -> {
            tellLocalUpdates.accept(update);
            ruleset.update(globalContext, update);
            handler.onGlobalContext(globalContext);
            tellLocalContexts.accept();
        });
        ruleset.detInitialUpdates(globalContext, initialConditions, handleUpdate);
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
                handler.onLocalContext(e.getKey(), e.getValue());
            }
            var decisionsMap = decisionFunction.apply();
            for (var decision: decisionsMap.values()) {
                if (decision == null) {
                    throw new BadDecisionException();
                }
            }
            ruleset.handleUpdates(globalContext, decisionsMap, handleUpdate);
        }
    }
}
