package jl95.tbb.pmon.effect;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;

import jl95.lang.StrictList;
import jl95.lang.variadic.Function0;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.status.PmonStatusCondition;

public class PmonEffectByStatusCondition {

    public StrictList<Chanced<Function0<PmonStatusCondition>>> statusConditions = strict(List());
}
