package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonLocalContext;
import jl95.tbb.mon.MonFieldPosition;
import jl95.util.StrictMap;

public class PmonLocalContext extends MonLocalContext<Pmon, PmonParty> {

    public final StrictMap<PartyId, StrictMap<MonFieldPosition, PmonPartyPublicView>> allParties;

    public PmonLocalContext(PmonParty ownParty,
                            StrictMap<PartyId, StrictMap<MonFieldPosition, PmonPartyPublicView>> allParties) {

        super(ownParty);
        this.allParties = allParties;
    }
}
