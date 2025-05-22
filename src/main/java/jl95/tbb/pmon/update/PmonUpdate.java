package jl95.tbb.pmon.update;

import static jl95.lang.SuperPowers.*;

import jl95.lang.variadic.Tuple2;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPartyMonId;

import java.util.List;

public class PmonUpdate {

    public final String id;
    public final Tuple2<PartyId, MonPartyMonId> source;
    public final List<Tuple3<PartyId, MonPartyMonId, PmonAtomicUpdate>> targetUpdates;

    public PmonUpdate(String typeIdentifier,
                      Tuple2<PartyId, MonPartyMonId> source) {
        this.id = typeIdentifier;
        this.source = source;
        this.targetUpdates = List();
    }
}
