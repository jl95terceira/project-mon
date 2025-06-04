package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.status.PmonFieldCondition;
import jl95.tbb.pmon.status.PmonGlobalFieldCondition;
import jl95.tbb.pmon.status.PmonPartyFieldCondition;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonGlobalContext extends MonGlobalContext<Pmon> {

    public StrictMap<PmonFieldCondition.Id, PmonFieldCondition>
        fieldConditionsGlobal  = strict(Map());
    public StrictMap<PartyId,
           StrictMap<PmonPartyFieldCondition.Id, PmonPartyFieldCondition>>
        fieldConditionsByParty = strict(Map());
    public StrictMap<PartyId, StrictMap<MonFieldPosition,
           StrictMap<PmonGlobalFieldCondition.Id, PmonGlobalFieldCondition>>>
        fieldConditionsByMon   = strict(Map());
}
