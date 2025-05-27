package jl95.tbb.pmon;

import jl95.lang.variadic.Function2;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

public class PmonRulesetConstants {

    public final Double
            STAT_MODIFIER_PARAMETER_1
            = 0.5;
    public final Double
            STAT_MODIFIER_PARAMETER_2
            = 1.0 / 3;
    public final StrictSet<PmonStatModifierType>
            STAT_TYPES_AFFECTED_BY_PARAMETER_2
            = strict(Set(PmonStatModifierType.ACCURACY, PmonStatModifierType.EVASION));
    public final Function2<Double, PmonStatModifierType, Integer>
            STAT_MODIFIER_FACTOR
            = (smt, stages) ->
                function((Double f) -> stages > 0
                           ?      (1 + stages * f)
                           : (1 / (1 - stages * f))).apply(!STAT_TYPES_AFFECTED_BY_PARAMETER_2.contains(smt)
                                                          ? STAT_MODIFIER_PARAMETER_1
                                                          : STAT_MODIFIER_PARAMETER_2);
    public final Integer
            SPEED_RNG_BREADTH
            = 20;
    public final Double
            POWER_FACTOR_OF_TYPE_SUPER_EFFECTIVE = 2.0;
    public final Double
            POWER_FACTOR_OF_TYPE_NOT_VERY_EFFECTIVE = 0.5;
    public final Double
            POWER_FACTOR_OF_TYPE_DOES_NOT_AFFECT = 0.0;
    public final StrictMap<PmonMoveEffectivenessType, Double>
            POWER_FACTOR_MAP = strict(Map(
                tuple(PmonMoveEffectivenessType.SUPER_EFFECTIVE   , POWER_FACTOR_OF_TYPE_SUPER_EFFECTIVE),
                tuple(PmonMoveEffectivenessType.NOT_VERY_EFFECTIVE, POWER_FACTOR_OF_TYPE_NOT_VERY_EFFECTIVE),
                tuple(PmonMoveEffectivenessType.DOES_NOT_AFFECT   , POWER_FACTOR_OF_TYPE_DOES_NOT_AFFECT)
    ));
    public final Double
            CRITICAL_HIT_CHANCE = 0.05;
    public final Double
            CRITICAL_HIT_POWER_FACTOR = 1.5;
    public final Double
            STAB_FACTOR = 1.5;
}
