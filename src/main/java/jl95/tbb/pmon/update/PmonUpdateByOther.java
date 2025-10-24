package jl95.tbb.pmon.update;

import jl95.lang.variadic.Tuple2;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.util.StrictList;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_FIELD_POSITION;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_PARTY;

public class PmonUpdateByOther {

    public Tuple2<PartyId,MonFieldPosition> origin;
    public StrictMap<Tuple2<PartyId, MonFieldPosition>,StrictList<PmonUpdateOnTarget>> atomicUpdates = strict(Map());
}
