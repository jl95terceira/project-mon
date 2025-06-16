package jl95.tbb.pmon.update;

import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateOnTargetByStatModifier {

    public StrictMap<PmonStatModifierType, Integer> increments = strict(Map());
    public StrictSet<PmonStatModifierType> resets = strict(Set());
}
