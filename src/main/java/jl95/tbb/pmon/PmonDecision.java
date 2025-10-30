package jl95.tbb.pmon;

import jl95.tbb.pmon.decision.PmonDecisionToUseMove;
import jl95.tbb.pmon.decision.PmonDecisionToPass;
import jl95.tbb.pmon.decision.PmonDecisionToSwitchOut;

public interface PmonDecision {

    void get(Handler handler);

    interface Handler {

        void pass     (PmonDecisionToPass     decision);
        void switchOut(PmonDecisionToSwitchOut decision);
        void useMove  (PmonDecisionToUseMove  decision);
    }

    static PmonDecision from(PmonDecisionToPass      decision) {
        return handler -> handler.pass     (decision);
    }
    static PmonDecision from(PmonDecisionToSwitchOut decision) {
        return handler -> handler.switchOut(decision);
    }
    static PmonDecision from(PmonDecisionToUseMove   decision) {
        return handler -> handler.useMove  (decision);
    }
}
