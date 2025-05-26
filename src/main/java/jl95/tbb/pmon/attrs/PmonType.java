package jl95.tbb.pmon.attrs;

import jl95.util.AutoHashcoded;

public abstract class PmonType {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public PmonType(Id id) {
        this.id = id;
    }

    public abstract PmonMoveEffectivenessType effectivenessAgainst(PmonType other);
}
