package jl95.tbb.pmon;

public abstract class PmonType {

    public static class Id {}

    public final Id id;
    public PmonType(Id id) {
        this.id = id;
    }

    public abstract PmonMove.EffectivenessType effectivenessAgainst(PmonType other);
}
