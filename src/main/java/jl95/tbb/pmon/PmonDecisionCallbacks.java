package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyMonId;
import jl95.util.StrictMap;

public interface PmonDecisionCallbacks {

    void switchIn(Integer monSwitchInIndex);
    void useMove(Integer moveIndex, StrictMap<PartyId, ? extends Iterable<MonPartyMonId>> targets);
}
