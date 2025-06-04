package jl95.tbb.pmon.attrs;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

import jl95.lang.variadic.Function1;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;

public class PmonStatusConditionAttributes {

    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public Function1<Double, Integer> cureChanceByTurn = turnNr_ -> 0.0;
    public Function1<Double, Integer> immobiliseChanceByTurn = turnNr_ -> 0.0;
    public Boolean allowDecide = true;
}
