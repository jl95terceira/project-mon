package jl95.tbb.pmon;

import jl95.lang.SuperPowers;
import jl95.tbb.pmon.attrs.PmonAbility;
import jl95.tbb.pmon.attrs.PmonStats;
import jl95.tbb.pmon.attrs.PmonType;

import java.util.Set;

public class PmonAttributes {

    public final Set<PmonType> types = SuperPowers.Set();
    public final PmonStats baseStats = new PmonStats();
    public final Set<PmonAbility> abilities = SuperPowers.Set();
}
