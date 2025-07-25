package jl95.tbb.pmon.effect;

import jl95.lang.variadic.Function1;
import jl95.tbb.pmon.attrs.PmonMovePower;
import jl95.tbb.pmon.attrs.PmonMoveType;
import jl95.tbb.pmon.attrs.PmonType;

public class PmonEffectByDamage {

    public PmonMoveType type = PmonMoveType.NORMAL;
    public PmonType pmonType;
    public PmonMovePower power = PmonMovePower.none();
    public Function1<Double, Integer> powerReductionFactorByNrTargets = n -> (1.0 / n);
    public Double healbackFactor = null;
}
