package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.util.AutoHashcoded;

import java.util.Set;

public abstract class PmonType extends AutoHashcoded {

    public final String name;
    public PmonType(String name) {
        this.name = name;
    }

    public abstract Boolean isSuperEffectiveAgainst(PmonType other);
    public abstract Boolean isNotVeryEffectiveAgainst(PmonType other);
    public abstract Boolean doesNotAffect(PmonType other);
}
