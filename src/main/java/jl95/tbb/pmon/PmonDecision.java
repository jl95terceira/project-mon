package jl95.tbb.pmon;

import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchIn;

public interface PmonDecision {

    void call(Handlers handlers);

    interface Handlers {

        void pass(PmonDecisionToPass decision);
        void switchIn(PmonDecisionToSwitchIn decision);
        void useMove(PmonDecisionToUseMove decision);
    }

    static PmonDecision from(PmonDecisionToPass decision) {
        return handlers -> handlers.pass(decision);
    }
    static PmonDecision from(PmonDecisionToSwitchIn decision) {
        return handlers -> handlers.switchIn(decision);
    }
    static PmonDecision from(PmonDecisionToUseMove decision) {
        return handlers -> handlers.useMove(decision);
    }
}
