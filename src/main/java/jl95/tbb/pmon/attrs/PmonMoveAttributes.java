package jl95.tbb.pmon.attrs;

import jl95.lang.StrictList;
import jl95.lang.variadic.Function0;
import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonMoveAttributes {

    public PmonMoveTargettingType targetType = PmonMoveTargettingType.FOE_SINGLE_MON;
    public PmonMoveType type = PmonMoveType.NORMAL;
    public PmonType pmonType;
    public PmonMovePower power = PmonMovePower.none();
    public Double healbackFactor = null;
    public Function1<Double, Integer> powerReductionFactorByNrTargets = n -> (1.0 / n);
    public Tuple2<Integer, Integer> hitNrTimesRange = tuple(1,1);
    public Integer accuracy = 0;
    public StrictMap<PmonStatModifierType, Chanced<Integer>> statModifiers = strict(Map());
    public Integer priorityModifier = 0;
    public Boolean pursuit = false;
    public StrictList<Chanced<Function0<PmonStatusCondition>>> statusConditions = strict(List());
    public Boolean disableLastMove = false;

    public PmonMoveAttributes(PmonType pmonType) {
        this.pmonType = pmonType;
    }
}
