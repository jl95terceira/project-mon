package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.util.StrictMap;

public class MonLocalContext<Mon, FoeMonView> {

    public final MonParty<Mon> ownParty;
    public final StrictMap<PartyId, StrictMap<MonParty.MonId, FoeMonView>> foeParty;

    public MonLocalContext(MonParty<Mon> ownParty,
                           StrictMap<PartyId, StrictMap<MonParty.MonId, FoeMonView>> foeParty) {

        this.ownParty = ownParty;
        this.foeParty = foeParty;
    }
}
