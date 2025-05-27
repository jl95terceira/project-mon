package jl95.tbb.pmon.update.atomic;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;

public class PmonAtomicEffectBySwitchIn {

        public PartyId partyId;
        public MonParty.MonId monId;
        public Integer monToSwitchInIndex = 0;

        public PmonAtomicEffectBySwitchIn(PartyId partyId, MonParty.MonId monId) {
                this.partyId = partyId;
                this.monId = monId;
        }
}
