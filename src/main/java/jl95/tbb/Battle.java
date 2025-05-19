package jl95.tbb;

import jl95.lang.*;
import jl95.lang.variadic.*;
import jl95.util.StrictMap;

import java.util.*;

import static jl95.lang.SuperPowers.*;

public class Battle<
        PartyEntry, InitialConditions,
        LocalContext, GlobalContext,
        Decision, LocalUpdate, GlobalUpdate
        > {

    public final Ruleset<
            PartyEntry, InitialConditions,
            LocalContext, GlobalContext,
            Decision, LocalUpdate, GlobalUpdate> ruleset;

    public Battle(Ruleset<
            PartyEntry, InitialConditions,
            LocalContext, GlobalContext,
            Decision, LocalUpdate, GlobalUpdate
            > ruleset) {

        this.ruleset = ruleset;
    }

    public interface Callbacks<LocalContext, GlobalUpdate> {

        void onUpdate(PartyId id, GlobalUpdate update);
        void onContext(PartyId id, LocalContext context);
    }

    public Optional<PartyId> spawn(StrictMap<PartyId, PartyEntry> parties,
                                   InitialConditions initialConditions,
                                   StrictMap<PartyId, Function1<Decision, LocalContext>> decisionFunctionsMap,
                                   Callbacks<LocalContext, LocalUpdate> callbacks,
                                   Function0<Boolean> toInterrupt) {

        var shuttindDown = new Ref<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shuttindDown.set(true)));
        var partyIds = strict(parties.keySet()).iter();
        var globalContext = ruleset.init(parties, initialConditions);
        var localContextGetter = function(() -> strict(I.of(partyIds)
                                                        .toMap(id -> id,
                                                               id -> ruleset.detLocalContext(globalContext, id))));
        var handleUpdates = method((Iterable<GlobalUpdate> updates) -> {
            for (GlobalUpdate update: updates) {
                for (var partyId: partyIds) {
                    callbacks.onUpdate(partyId, ruleset.detLocalUpdate(update, partyId));
                }
                ruleset.update(globalContext, update);
                var localContext = localContextGetter.apply();
                for (var partyId: partyIds) {
                    callbacks.onContext(partyId, localContext.get(partyId));
                }
            }
        });
        var updatesAtStart = ruleset.evalStart(globalContext, initialConditions);
        handleUpdates.accept(updatesAtStart);
        while (true) {
            if (shuttindDown.get() || toInterrupt.apply()) {
                return Optional.empty();
            }
            var victorOptional = ruleset.evalVictory(globalContext);
            if (victorOptional.isPresent()) {
                return victorOptional;
            }
            var localContextsMap = localContextGetter.apply();
            var decisionsMap = strict(I.of(partyIds)
                                       .toMap(id -> id,
                                              id -> decisionFunctionsMap.get(id)
                                                                        .apply(localContextsMap.get(id))));
            var updates = I.of(ruleset.evalDecisions(globalContext, decisionsMap)).toList();
            handleUpdates.accept(updates);
        }
    }
}
