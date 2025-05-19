package jl95.tbb.mon1;

import jl95.lang.variadic.Function0;
import jl95.tbb.PartyId;
import jl95.lang.I;
import jl95.lang.variadic.Function2;
import jl95.util.StrictMap;

import java.util.Map;
import java.util.Optional;

import static jl95.lang.SuperPowers.*;

public class Battle<
        Mon,
        InitialConditions,
        MonDecision,
        LocalUpdate,
        GlobalUpdate
        > {

    public final jl95.tbb.Battle<
            jl95.tbb.mon1.PartyEntry<Mon>,
            InitialConditions,
            jl95.tbb.mon1.LocalContext<Mon>,
            jl95.tbb.mon1.GlobalContext<Mon>,
            PartyDecision<MonDecision>,
            LocalUpdate,
            GlobalUpdate
            > upcastBattle;

    public Battle(Ruleset<
            Mon,
            InitialConditions,
            MonDecision,
            LocalUpdate,
            GlobalUpdate
            > ruleset) {

        this.upcastBattle = new jl95.tbb.Battle<>(ruleset.upcast());
    }

    public Optional<PartyId> spawn(
            StrictMap<PartyId, PartyEntry<Mon>> parties,
            InitialConditions initialConditions,
            StrictMap<PartyId, Function2<MonDecision, LocalContext<Mon>, MonId>> decisionFunctionsMap,
            jl95.tbb.Battle.Callbacks<LocalContext<Mon>, LocalUpdate> callbacks,
            Function0<Boolean> toInterrupt
    ) {

        return this.upcastBattle.spawn(parties, initialConditions, strict(I.of(decisionFunctionsMap.entrySet()).toMap(Map.Entry::getKey, e -> {
            var monDecisionFunction = e.getValue();
            return (LocalContext<Mon> context) -> {
                var partyDecision = new PartyDecision<MonDecision>();
                for (var monId: context.ownParty.monsOnField.keySet()) {
                    partyDecision.monDecisions.put(monId, monDecisionFunction.apply(context, monId));
                }
                return partyDecision;
            };
        })), callbacks, toInterrupt);
    }
}
