package jl95.tbb.pmon.status;

import jl95.lang.variadic.Function1;
import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;
import jl95.util.StrictSet;

import static jl95.lang.SuperPowers.*;

public class PmonStatusCondition {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public Integer turnNr = 0;
    public Function1<Double, Integer> cureChanceByTurn = turnNr_ -> 0.0;
    public Function1<Double, Integer> immobiliseChanceByTurn = turnNr_ -> 0.0;
    public Boolean allowDecide = true;


    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
