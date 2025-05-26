package jl95.tbb.pmon.update;

import static jl95.lang.SuperPowers.*;

import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.util.StrictMap;

import java.util.List;

public class PmonNotification {

    public static class Id {}

    StrictMap<Id, Tuple2<PartyId, StrictMap<Id, MonParty.MonId>>> mons = strict(Map());
}
