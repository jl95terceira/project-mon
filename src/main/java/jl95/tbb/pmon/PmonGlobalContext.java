package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.status.PmonFieldCondition;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonGlobalContext extends MonGlobalContext<Pmon> {

    public StrictMap<PmonFieldCondition.Id, PmonFieldCondition>   fieldConditionsGlobal  = strict(Map());
    public StrictMap<PartyId,
           StrictMap<PmonFieldCondition.Id, PmonFieldCondition>>  fieldConditionsByParty = strict(Map());
    public StrictMap<PartyId, StrictMap<MonParty.MonId,
           StrictMap<PmonFieldCondition.Id, PmonFieldCondition>>> fieldConditionsByMon   = strict(Map());
}
