package jl95.tbb.pmon.update;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;

import static jl95.tbb.pmon.attrs.PmonMoveTargetting.NO_TARGET_MON;
import static jl95.tbb.pmon.attrs.PmonMoveTargetting.NO_TARGET_PARTY;

public class PmonUpdateBySwitchIn {

        public PartyId partyId = NO_TARGET_PARTY;
        public MonParty.MonId monId = NO_TARGET_MON;
        public Integer monToSwitchInIndex = 0;
}
