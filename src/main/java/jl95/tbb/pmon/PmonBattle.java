package jl95.tbb.pmon;

import jl95.tbb.mon.MonBattle;
import jl95.tbb.mon.MonRuleset;
import jl95.tbb.pmon.update.PmonUpdate;

public class PmonBattle extends MonBattle<
        Pmon, PmonFoeView,
        PmonInitialConditions,
        PmonLocalContext,
        PmonGlobalContext,
        PmonDecision,
        PmonUpdate, PmonUpdate
        > {

    public PmonBattle(MonRuleset<Pmon, PmonFoeView, PmonInitialConditions, PmonLocalContext, PmonGlobalContext, PmonDecision, PmonUpdate, PmonUpdate> ruleset) {
        super(ruleset);
    }
}
