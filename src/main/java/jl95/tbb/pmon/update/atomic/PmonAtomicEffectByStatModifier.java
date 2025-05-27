package jl95.tbb.pmon.update.atomic;

import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonAtomicEffectByStatModifier {

    public StrictMap<PmonStatModifierType, Integer> statRaises = strict(Map());
    public StrictMap<PmonStatModifierType, Integer> statFalls = strict(Map());
}
