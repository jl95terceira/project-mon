package jl95.tbb.pmon;

import jl95.lang.variadic.Function2;
import jl95.tbb.pmon.attrs.PmonMovePriorityType;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonRulesetConstants {

    public static final Function2<Double, PmonStatModifierType, Integer> STAT_MODIFIER_MULTIPLIER = (smt, stage) -> stage > 0? ((2.0 + stage) / 2.0): (2.0 / (2.0 - stage));
}
