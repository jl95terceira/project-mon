package jl95.tbb.pmon.attrs;

import jl95.util.AutoHashcoded;

public abstract class PmonType extends AutoHashcoded {

    public final String name;
    public PmonType(String name) {
        this.name = name;
    }

    public abstract Boolean isSuperEffectiveAgainst(PmonType other);
    public abstract Boolean isNotVeryEffectiveAgainst(PmonType other);
    public abstract Boolean doesNotAffect(PmonType other);
}
