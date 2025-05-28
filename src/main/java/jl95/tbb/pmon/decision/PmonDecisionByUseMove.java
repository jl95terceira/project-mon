package jl95.tbb.pmon.decision;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPosition;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonDecisionByUseMove {

    public Integer moveIndex = -1;
    public StrictMap<PartyId, ? extends Iterable<MonPosition>> targets = strict(Map());
}
