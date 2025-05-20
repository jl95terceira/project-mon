package jl95.tbb.mon;

import jl95.tbb.PartyId;
import jl95.util.StrictMap;

public class MonLocalContext<Mon, FoeMonView> {

    public final MonParty<Mon> ownParty;
    public final StrictMap<PartyId, StrictMap<MonPartyMonId, FoeMonView>> foeParty;

    public MonLocalContext(MonParty<Mon> ownParty,
                           StrictMap<PartyId, StrictMap<MonPartyMonId, FoeMonView>> foeParty) {

        this.ownParty = ownParty;
        this.foeParty = foeParty;
    }
}
