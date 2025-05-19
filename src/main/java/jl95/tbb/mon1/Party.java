package jl95.tbb.mon1;

import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class Party<Mon> {

    public final StrictMap<MonId, Mon> monsOnField = strict(Map());
    public final List<Mon> mons = List();

    public static <Mon> Party<Mon> fromEntry(PartyEntry<Mon> entry) {
        var party = new Party<Mon>();
        party.mons.addAll(entry.mons);
        return party;
    }
}
