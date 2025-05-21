package jl95.tbb.pmon;

import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictMap;

public class PmonStatus {

    public Integer hp = 0;
    public StrictMap<PmonStatModifierType, Integer> statModifiers;
    public StrictMap<String, PmonStatusCondition> statusProblems;
}
