package jl95.tbb.pmon.update;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPosition;

import static jl95.tbb.pmon.attrs.PmonDefaults.NO_MON;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_PARTY;

public class PmonUpdateBySwitchIn {

        public PartyId partyId = NO_PARTY;
        public MonPosition monId = NO_MON;
        public Integer monToSwitchInIndex = 0;
}
