package jl95.tbb.pmon.attrs;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonAttributes {

    public final StrictMap<PmonType.Id, PmonType> types = strict(Map());
    public final PmonStats baseStats = new PmonStats();
    public final StrictMap<PmonAbility.Id, PmonAbility> abilities = strict(Map());
}
