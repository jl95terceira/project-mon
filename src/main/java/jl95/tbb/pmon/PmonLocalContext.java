package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonLocalContext;
import jl95.tbb.mon.MonParty;
import jl95.util.StrictMap;

public class PmonLocalContext extends MonLocalContext<Pmon, PmonFoeView> {

    public final StrictMap<PartyId, StrictMap<MonParty.MonId, PmonFoeView>> foeParty;

    public PmonLocalContext(MonParty<Pmon> ownParty,
                            StrictMap<PartyId, StrictMap<MonParty.MonId, PmonFoeView>> foeParty) {

        super(ownParty);
        this.foeParty = foeParty;
    }
}
