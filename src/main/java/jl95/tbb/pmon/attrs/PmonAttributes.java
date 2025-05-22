package jl95.tbb.pmon.attrs;

import jl95.lang.SuperPowers;

import java.util.Set;

public class PmonAttributes {

    public final Set<PmonType> types = SuperPowers.Set();
    public final PmonStats baseStats = new PmonStats();
    public final Set<PmonAbility> abilities = SuperPowers.Set();
}
