package jl95.tbb.pmon.status;

import jl95.util.StrictMap;

public class PmonStatus {

    public Integer hp = 0;
    public StrictMap<PmonStatModifierType, Integer> statModifiers;
    public StrictMap<PmonStatusCondition.Id, PmonStatusCondition> statusConditions;
}
