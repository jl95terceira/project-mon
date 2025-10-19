package jl95.tbb.pmon.status;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonStatus {

    public int hp = 0;
    public Integer lastMoveUsedIndex = null; //TODO: use this
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public StrictMap<PmonStatusCondition.Id, PmonStatusCondition> statusConditions = strict(Map());
}
