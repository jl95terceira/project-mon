package jl95.tbb.pmon.effect;

import static jl95.lang.SuperPowers.*;

import jl95.tbb.pmon.Chanced;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.*;

public class PmonEffectByStatModify {

    public StrictMap<PmonStatModifierType, Chanced<Integer>> statModifiers = strict(Map());
}
