package jl95.tbb.pmon.update.notif;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.status.PmonFieldCondition;
import jl95.tbb.pmon.status.PmonStatusCondition;

public class PmonNotificationOfImmobilisedByFieldCondition {

    PartyId partyId;
    MonParty.MonId monId;
    PmonFieldCondition conditionId;

    public PmonNotificationOfImmobilisedByFieldCondition(PartyId partyId,
                                                         MonParty.MonId monId,
                                                         PmonFieldCondition conditionId) {
        this.partyId = partyId;
        this.monId = monId;
        this.conditionId = conditionId;
    }
}
