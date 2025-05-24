package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.util.StrictMap;

public class MonLocalContext<Mon, FoeMonView> {

    public final MonParty<Mon> ownParty;

    public MonLocalContext(MonParty<Mon> ownParty) {

        this.ownParty = ownParty;
    }
}
