package jl95.tbb.pmon.status;

import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonStatusCondition {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public Integer turnNr = 0;


    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
