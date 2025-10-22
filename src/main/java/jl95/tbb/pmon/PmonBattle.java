package jl95.tbb.pmon;

import jl95.tbb.Battle;
import jl95.tbb.mon.MonBattle;
import jl95.tbb.mon.MonRuleset;
import jl95.tbb.pmon.update.PmonUpdate;

public class PmonBattle extends MonBattle<
        Pmon, PmonPartyEntry, PmonParty,
        PmonInitialConditions,
        PmonLocalContext,
        PmonGlobalContext,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public interface Handler extends Battle.Handler<PmonUpdate, PmonLocalContext, PmonGlobalContext> {

        static class Editable extends Battle.Handler.Editable<PmonUpdate, PmonLocalContext, PmonGlobalContext> implements Handler {}
        static class Extendable extends Battle.Handler.Extendable<PmonUpdate, PmonLocalContext, PmonGlobalContext> implements Handler {}
        static Handler ignore() { return new Editable(); }
    }

    public PmonBattle(MonRuleset<Pmon, PmonPartyEntry, PmonParty, PmonInitialConditions, PmonLocalContext, PmonGlobalContext, PmonDecision, PmonUpdate, PmonUpdate> ruleset) {
        super(ruleset);
    }
    public PmonBattle() {
        this(new PmonRuleset());
    }
}
