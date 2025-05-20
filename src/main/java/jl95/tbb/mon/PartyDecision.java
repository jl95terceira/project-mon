package jl95.tbb.mon;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PartyDecision<MonDecision> {

    public final StrictMap<PartyMonId, MonDecision> monDecisions = strict(Map());
}
