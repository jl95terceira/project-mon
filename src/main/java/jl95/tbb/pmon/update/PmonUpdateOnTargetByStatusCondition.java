package jl95.tbb.pmon.update;

import jl95.util.StrictList;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateOnTargetByStatusCondition {

    public StrictList<PmonStatusCondition> statusConditionsApply = strict(List());
    public StrictSet<PmonStatusCondition.Id> statusConditionsRemove = strict(Set());
}
