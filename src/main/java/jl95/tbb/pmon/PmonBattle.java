package jl95.tbb.pmon;

import java.util.Optional;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.tbb.Battle;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonBattle;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonRuleset;
import jl95.tbb.pmon.update.PmonUpdate;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

public class PmonBattle {

    private final PmonRuleset ruleset;
    private final MonBattle<
        Pmon, PmonPartyEntry, PmonParty,
        PmonInitialConditions,
        PmonLocalContext,
        PmonGlobalContext,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > upcastBattle;

    public interface Handler extends Battle.Handler<PmonUpdate, PmonLocalContext, PmonGlobalContext> {

        class Editable extends Battle.Handler.Editable<PmonUpdate, PmonLocalContext, PmonGlobalContext> implements Handler {}
        class Extendable extends Battle.Handler.Extendable<PmonUpdate, PmonLocalContext, PmonGlobalContext> implements Handler {}
        static Handler ignore() { return new Editable(); }
    }

    public PmonBattle(PmonRuleset ruleset) {
        this.ruleset = ruleset;
        this.upcastBattle = new MonBattle<>(ruleset);
    }
    public PmonBattle() {
        this(new PmonRuleset());
    }

    public Optional<PartyId> start(
            StrictMap<PartyId, PmonPartyEntry> parties,
            PmonInitialConditions initialConditions,
            Function1<StrictMap<PartyId, StrictMap<MonFieldPosition, PmonDecision>>,
                                  StrictMap<PartyId, StrictSet<MonFieldPosition>>> decisionFunction,
            Battle.Handler<PmonUpdate, PmonLocalContext, PmonGlobalContext> handler,
            Function0<Boolean> toInterrupt) {
        return upcastBattle.start(parties, initialConditions, decisionFunction, handler, toInterrupt);
    }
}
