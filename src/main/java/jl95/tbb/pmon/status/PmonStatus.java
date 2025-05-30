package jl95.tbb.pmon.status;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonStatus {

    public Integer hp = 0;
    public Integer lastMoveUsedIndex = null;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public StrictMap<PmonStatusCondition.Id, PmonStatusCondition> statusConditions = strict(Map());
}
