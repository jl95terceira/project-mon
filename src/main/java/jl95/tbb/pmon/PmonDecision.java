package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.util.StrictMap;

public interface PmonDecision {

    void call(Callbacks decisions);

    interface Callbacks {

        void switchIn(Integer monSwitchInIndex);
        void useMove(Integer moveIndex, StrictMap<PartyId, ? extends Iterable<MonParty.MonId>> targets);
    }

    public static PmonDecision switchIn(Integer monSwitchInIndex) {
        return cb -> cb.switchIn(monSwitchInIndex);
    }
    public static PmonDecision move(Integer moveIndex, StrictMap<PartyId, ? extends Iterable<MonParty.MonId>> targets) {
        return cb -> cb.useMove(moveIndex, targets);
    }
}
