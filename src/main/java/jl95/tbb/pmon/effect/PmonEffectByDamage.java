package jl95.tbb.pmon.effect;

import jl95.lang.variadic.Function1;
import jl95.tbb.pmon.PmonMove;
import jl95.tbb.pmon.PmonType;

public class PmonEffectByDamage {

    public PmonMove.Type type = PmonMove.Type.NORMAL;
    public PmonType pmonType;
    public PmonMove.Power power = PmonMove.Power.none();
    public Function1<Double, Integer> powerReductionFactorByNrTargets = n -> (1.0 / n);
    public Double healbackFactor = null;
}
