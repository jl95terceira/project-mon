package jl95.tbb.pmon.status;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

import jl95.lang.variadic.Function1;
import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;

public class PmonPartyFieldCondition {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public Integer turnNr = 0;
    public Function1<Double, Integer> cureChanceByTurn = turnNr_ -> 0.0;
    public Function1<Double, Integer> immobiliseChanceByTurn = turnNr_ -> 0.0;
    public Boolean allowDecide = true;

    public PmonPartyFieldCondition(Id id) {this.id = id;}
}
