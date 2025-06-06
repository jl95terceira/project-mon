package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonLocalContext;
import jl95.tbb.mon.MonParty;
import jl95.tbb.mon.MonFieldPosition;
import jl95.util.StrictMap;

public class PmonLocalContext extends MonLocalContext<Pmon, PmonParty> {

    public final StrictMap<PartyId, StrictMap<MonFieldPosition, PmonFoeView>> foeParty;

    public PmonLocalContext(PmonParty ownParty,
                            StrictMap<PartyId, StrictMap<MonFieldPosition, PmonFoeView>> foeParty) {

        super(ownParty);
        this.foeParty = foeParty;
    }
}
