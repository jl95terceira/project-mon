package jl95.tbb.mon;

import jl95.util.StrictList;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class MonParty<Mon> {

    public final StrictMap<MonFieldPosition, Mon> monsOnField = strict(Map());
    public final StrictList<Mon> mons = strict(List());

    public static <Mon> MonParty<Mon> fromEntry(MonPartyEntry<Mon> entry) {
        var party = new MonParty<Mon>();
        party.mons.addAll(entry.mons);
        return party;
    }
}
