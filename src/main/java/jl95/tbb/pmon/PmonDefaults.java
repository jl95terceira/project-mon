package jl95.tbb.pmon;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonId;

public class PmonDefaults {

    public static final PartyId NO_PARTY = new PartyId();
    public static final MonFieldPosition NO_FIELD_POSITION = new MonFieldPosition();
    public static final MonId NO_MON = new MonId(NO_PARTY,NO_FIELD_POSITION);
    public static final Integer NO_PARTY_POSITION = -1;
}
