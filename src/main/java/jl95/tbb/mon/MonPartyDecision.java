package jl95.tbb.mon;

import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class MonPartyDecision<MonDecision> {

    public final StrictMap<MonParty.MonId, MonDecision> monDecisions = strict(Map());
}
