package jl95.tbb.pmon.update;

import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.util.StrictList;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateByOther {

    public Tuple2<PartyId,MonFieldPosition> origin;
    public StrictMap<Tuple2<PartyId, MonFieldPosition>,StrictList<PmonUpdateOnTarget>> atomicUpdates = strict(Map());
}
