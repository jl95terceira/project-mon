package jl95.tbb.pmon.update.notif;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.PmonMove;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.Map;
import static jl95.lang.SuperPowers.strict;

public class PmonNotificationOfMove {

    PartyId partyId;
    MonParty.MonId monId;
    PmonMove.Id moveId;
    StrictMap<PartyId, MonParty.MonId> targets = strict(Map());

    public PmonNotificationOfMove(PartyId partyId,
                                  MonParty.MonId monId,
                                  PmonMove.Id moveId) {
        this.partyId = partyId;
        this.monId = monId;
        this.moveId = moveId;
    }
}
