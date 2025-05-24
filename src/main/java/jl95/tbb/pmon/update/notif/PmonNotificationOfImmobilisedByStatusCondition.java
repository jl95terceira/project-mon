package jl95.tbb.pmon.update.notif;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.status.PmonStatusCondition;

import static jl95.lang.SuperPowers.strict;

public class PmonNotificationOfImmobilisedByStatusCondition {

    PartyId partyId;
    MonParty.MonId monId;
    PmonStatusCondition conditionId;

    public PmonNotificationOfImmobilisedByStatusCondition(PartyId partyId,
                                                          MonParty.MonId monId,
                                                          PmonStatusCondition conditionId) {
        this.partyId = partyId;
        this.monId = monId;
        this.conditionId = conditionId;
    }
}
