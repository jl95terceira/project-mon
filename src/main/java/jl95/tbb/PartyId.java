package jl95.tbb;

import jl95.util.DataClass;

import java.util.UUID;

import static jl95.lang.SuperPowers.*;

public class PartyId extends DataClass {

    public final UUID value = UUID.randomUUID();

    @Override
    protected Iterable<?> data() {
        return I(tuple(value));
    }
}
