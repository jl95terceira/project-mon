package jl95.tbb.pmon.status;

import jl95.tbb.pmon.attrs.PmonStatusConditionAttributes;

public class PmonStatusCondition {

    public static class Id {}

    public final Id id;
    public Integer turnNr = 0; //TODO: use this
    public PmonStatusConditionAttributes attrs = new PmonStatusConditionAttributes();

    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
