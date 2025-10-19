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

    public interface Listeners extends Battle.Listeners<PmonUpdate, PmonLocalContext, PmonGlobalContext> {

        static class Editable extends Battle.Listeners.Editable<PmonUpdate, PmonLocalContext, PmonGlobalContext> implements Listeners {}
        static PmonBattle.Listeners ignore() { return new Editable(); }
    }

    public PmonBattle(MonRuleset<Pmon, PmonPartyEntry, PmonParty, PmonInitialConditions, PmonLocalContext, PmonGlobalContext, PmonDecision, PmonUpdate, PmonUpdate> ruleset) {
        super(ruleset);
    }
    public PmonBattle() {
        this(new PmonRuleset());
    }
}
