package jl95.tbb.pmon.status;

import jl95.lang.variadic.Function1;
import jl95.tbb.pmon.attrs.PmonStatusConditionAttributes;
import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

public class PmonStatusCondition {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public Integer turnNr = 0; //TODO: use this
    public PmonStatusConditionAttributes attrs = new PmonStatusConditionAttributes();

    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
