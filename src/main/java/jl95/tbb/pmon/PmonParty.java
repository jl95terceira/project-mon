package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonParty;
import jl95.tbb.mon.MonPartyEntry;
import jl95.tbb.pmon.status.PmonFieldMonCondition;
import jl95.tbb.pmon.status.PmonFieldPartyCondition;
import jl95.util.StrictMap;

public class PmonParty extends MonParty<Pmon> {

    public StrictMap<PmonFieldPartyCondition.Id, PmonFieldPartyCondition>
        fieldConditions = strict(Map());
    public StrictMap<MonFieldPosition,
           StrictMap<PmonFieldMonCondition.Id, PmonFieldMonCondition>>
        fieldConditionsByMon   = strict(Map());

    public static PmonParty fromEntry(PmonPartyEntry entry) {
        var party = new PmonParty();
        party.mons.addAll(entry.mons);
        return party;
    }
}
