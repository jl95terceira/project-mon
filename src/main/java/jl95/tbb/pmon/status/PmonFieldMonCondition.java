package jl95.tbb.pmon.status;

import jl95.lang.variadic.Function1;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonFieldMonCondition {

    public static class Id {}

    public final Id id;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public Function1<Double, Integer> cureChanceByTurn = turnNr_ -> 0.0;
    public Integer turnNr = 0;

    public PmonFieldMonCondition(Id id) {this.id = id;}
}
