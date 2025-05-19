package jl95.tbb.mon1;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PartyDecision<MonDecision> {

    public final StrictMap<MonId, MonDecision> monDecisions = strict(Map());
}
