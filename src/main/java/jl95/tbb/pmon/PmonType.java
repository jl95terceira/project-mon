package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.util.AutoHashcoded;

import java.util.Set;

public class PmonType extends AutoHashcoded {

    public final String name;
    public final Set<PmonType> weakerTypes = Set();
    public final Set<PmonType> strongerTypes = Set();
    public final Set<PmonType> immuneTypes = Set();

    public PmonType(String name) {
        this.name = name;
    }
}
