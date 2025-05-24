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
    public final Double
            POWER_FACTOR_OF_TYPE_SUPER_EFFECTIVE = 2.0;
    public final Double
            POWER_FACTOR_OF_TYPE_NOT_VERY_EFFECTIVE = 0.5;
    public final Double
            POWER_FACTOR_OF_TYPE_DOES_NOT_AFFECT = 0.0;
}
