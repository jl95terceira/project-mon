package jl95.tbb.pmon.attrs;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

public class PmonStatusConditionAttributes {

    public StrictMap<PmonStatModifierType, Double> statFactors = strict(Map());
    public Function0<Double> cureChance = () -> 0.0; //TODO: use this
    public Function0<Double> immobiliseChance = () -> 0.0; //TODO: use this
    public Boolean allowDecide = true; //TODO: use this
    public Boolean allowSwitchOut = true;
}
