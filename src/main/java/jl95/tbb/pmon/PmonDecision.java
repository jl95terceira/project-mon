package jl95.tbb.pmon;

import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchOut;

public interface PmonDecision {

    void call(Handlers handlers);

    interface Handlers {

        void pass     (PmonDecisionToPass     decision);
        void switchOut(PmonDecisionToSwitchOut decision);
        void useMove  (PmonDecisionToUseMove  decision);
    }

    static PmonDecision from(PmonDecisionToPass      decision) {
        return handlers -> handlers.pass     (decision);
    }
    static PmonDecision from(PmonDecisionToSwitchOut decision) {
        return handlers -> handlers.switchOut(decision);
    }
    static PmonDecision from(PmonDecisionToUseMove   decision) {
        return handlers -> handlers.useMove  (decision);
    }
}
