package jl95.tbb.pmon.update;

import jl95.tbb.mon.MonPartyFieldPosition;
import jl95.util.StrictList;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateByOther {

    public MonPartyFieldPosition origin;
    public StrictMap<MonPartyFieldPosition, StrictList<PmonUpdateOnTarget>> atomicUpdates = strict(Map());
}
