package jl95.tbb.pmon.update.atomic;

import jl95.lang.StrictList;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictSet;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonAtomicEffectByStatusCondition {

    public StrictList<PmonStatusCondition> statusConditionsApply = strict(List());
    public StrictSet<PmonStatusCondition.Id> statusConditionsRemove = strict(Set());
}
