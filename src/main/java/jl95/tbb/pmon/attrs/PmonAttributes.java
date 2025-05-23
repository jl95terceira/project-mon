package jl95.tbb.pmon.attrs;

import jl95.util.StrictMap;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

import java.util.Set;

public class PmonAttributes {

    public final StrictMap<PmonType.Id, PmonType> types = strict(Map());
    public final PmonStats baseStats = new PmonStats();
    public final StrictMap<PmonAbility.Id, PmonAbility> abilities = strict(Map());
}
