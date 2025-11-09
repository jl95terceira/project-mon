package jl95.tbb.pmon.effect;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.PmonGlobalContext;
import jl95.util.StrictList;
import jl95.lang.variadic.Function0;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.status.PmonStatusCondition;

public class PmonEffectByStatusCondition {

    public record Context(
            PmonGlobalContext globalContext,
            MonId origin,
            MonId target) {}

    public StrictList<Function1<Chanced<Function0<PmonStatusCondition>>,Context>>
            statusConditionsInflict = strict(List());
    public StrictList<Function1<Chanced<PmonStatusCondition.Id>,Context>>
            statusConditionsCure = strict(List());
}
