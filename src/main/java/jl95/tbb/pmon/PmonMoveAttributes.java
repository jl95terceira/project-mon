package jl95.tbb.pmon;

import jl95.lang.SuperPowers;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.pmon.attrs.PmonMovePriorityType;
import jl95.tbb.pmon.attrs.PmonType;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonMoveAttributes {

    public PmonType pmonType;
    public Integer damage = 0;
    public Tuple2<Integer, Integer> damageNrTimesRange = tuple(1,1);
    public Integer accuracy = 0;
    public StrictMap<PmonStatModifierType, Chanced<Integer>> statModifiers = strict(Map());
    public Integer priorityModifier = 0;
    public Boolean pursuit = false;
    public List<Chanced<Function0<PmonStatusCondition>>> statusConditions = SuperPowers.List();
    public Boolean disableLastMove = false;
    public Boolean exhaustLastMove = false;

    public PmonMoveAttributes(PmonType pmonType) {
        this.pmonType = pmonType;
    }
}
