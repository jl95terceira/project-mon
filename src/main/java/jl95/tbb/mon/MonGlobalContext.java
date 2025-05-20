package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class MonGlobalContext<Mon> {

    public final StrictMap<PartyId, MonParty<Mon>> parties = strict(Map());
}
