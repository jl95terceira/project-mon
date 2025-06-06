package jl95.tbb.pmon.status;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

import jl95.lang.variadic.Function1;
import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;

public class PmonFieldGlobalCondition {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public Function1<Double, Integer> cureChanceByTurn = turnNr_ -> 0.0;
    public Integer turnNr = 0;

    public PmonFieldGlobalCondition(Id id) {this.id = id;}
}
