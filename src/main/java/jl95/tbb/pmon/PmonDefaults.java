package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonPartyFieldPosition;

public class PmonDefaults {

    public static final Pmon.Id NO_MON = new Pmon.Id();
    public static final PmonMove.Id NO_MOVE = new PmonMove.Id();
    public static final PartyId NO_PARTY = new PartyId();
    public static final MonFieldPosition NO_FIELD_POSITION = new MonFieldPosition();
    public static final MonPartyFieldPosition NO_PARTY_FIELD_POSITION = new MonPartyFieldPosition(NO_PARTY,NO_FIELD_POSITION);
    public static final Integer NO_PARTY_POSITION = -1;
}
