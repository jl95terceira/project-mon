package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.decision.PmonDecisionByUseMove;
import jl95.tbb.pmon.decision.PmonDecisionByPass;
import jl95.tbb.pmon.decision.PmonDecisionBySwitchIn;
import jl95.util.StrictMap;

public interface PmonDecision {

    void call(Handlers handlers);

    interface Handlers {

        void pass(PmonDecisionByPass decision);
        void switchIn(PmonDecisionBySwitchIn decision);
        void useMove(PmonDecisionByUseMove decision);
    }

    static PmonDecision by(PmonDecisionByPass decision) {
        return cb -> cb.pass(decision);
    }
    static PmonDecision by(PmonDecisionBySwitchIn decision) {
        return cb -> cb.switchIn(decision);
    }
    static PmonDecision by(PmonDecisionByUseMove decision) {
        return cb -> cb.useMove(decision);
    }
}
