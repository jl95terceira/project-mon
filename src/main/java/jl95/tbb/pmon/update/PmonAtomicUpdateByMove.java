package jl95.tbb.pmon.update;

import jl95.lang.SuperPowers;
import jl95.lang.variadic.Function1;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonAtomicUpdateByMove {

    public Function1<Integer, Integer> damage = (hp) -> null;
    public PmonMoveEffectivenessType effectivenessType = PmonMoveEffectivenessType.NORMAL;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public List<PmonStatusCondition> statusConditions = SuperPowers.List();
    public Boolean disableLastMove = false;
    public Boolean exhaustLastMove = false;
    public Integer switchIn = null;
}
