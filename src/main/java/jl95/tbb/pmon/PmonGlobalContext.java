package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonGlobalContext;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.status.PmonFieldMonCondition;
import jl95.tbb.pmon.status.PmonFieldGlobalCondition;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonGlobalContext extends MonGlobalContext<Pmon, PmonParty> {

    public StrictMap<PmonFieldGlobalCondition.Id, PmonFieldGlobalCondition> fieldConditions = strict(Map());
}
