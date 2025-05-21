package jl95.tbb.pmon.status;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public abstract class PmonStatusCondition {

    public final String id;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());

    public PmonStatusCondition(String id) {
        this.id = id;
    }
}
