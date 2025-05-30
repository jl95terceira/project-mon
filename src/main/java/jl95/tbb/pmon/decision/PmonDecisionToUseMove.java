package jl95.tbb.pmon.decision;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonDecisionToUseMove {

    public Integer moveIndex = -1;
    public StrictMap<PartyId, Iterable<MonFieldPosition>> targets = strict(Map());
}
