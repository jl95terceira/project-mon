package jl95.tbb.pmon.attrs;

import jl95.util.AutoHashcoded;

public class PmonAbility {

    public static class Id extends AutoHashcoded {}

    public final Id id;

    public PmonAbility(Id id) {
        this.id = id;
    }
}
