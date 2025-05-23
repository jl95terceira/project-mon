package jl95.tbb.pmon;

import jl95.lang.variadic.Function2;
import jl95.tbb.pmon.status.PmonStatModifierType;

import static jl95.lang.SuperPowers.function;

public class PmonRulesetConstants {

    public final Double
            STAT_MODIFIER_FACTOR
            = 1.0;
    public final Function2<Double, PmonStatModifierType, Integer>
            STAT_MODIFIER_MULTIPLIER
            = (smt, stage) -> function((Double f) -> stage > 0
                            ?      (1 + stage * f)
                            : (1 / (1 - stage * f))).apply(STAT_MODIFIER_FACTOR / 2.0);
    public final Integer
            SPEED_RNG_BREADTH
            = 20;
}
