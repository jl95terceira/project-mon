package jl95.tbb.pmon;

import jl95.lang.variadic.Function2;
import jl95.tbb.pmon.status.PmonStatModifierType;

public class PmonRulesetConstants {

    public final Function2<Double, PmonStatModifierType, Integer>
            STAT_MODIFIER_MULTIPLIER
            = (smt, stage) -> stage > 0? ((2.0 + stage) / 2.0): (2.0 / (2.0 - stage));
    public final Integer
            SPEED_RNG_BREADTH
            = 20;
}
