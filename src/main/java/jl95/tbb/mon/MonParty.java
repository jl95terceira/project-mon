package jl95.tbb.mon;

import jl95.util.AutoHashcoded;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class MonParty<Mon> {

    public static class MonId extends AutoHashcoded {}

    public final StrictMap<MonId, Mon> monsOnField = strict(Map());
    public final List<Mon> mons = List();

    public static <Mon> MonParty<Mon> fromEntry(MonPartyEntry<Mon> entry) {
        var party = new MonParty<Mon>();
        party.mons.addAll(entry.mons);
        return party;
    }
}
