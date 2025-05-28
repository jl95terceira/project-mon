package jl95.tbb.pmon.update.atomic;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPosition;

public class PmonAtomicEffectBySwitchIn {

        public PartyId partyId;
        public MonPosition monId;
        public Integer monToSwitchInIndex = 0;

        public PmonAtomicEffectBySwitchIn(PartyId partyId, MonPosition monId) {
                this.partyId = partyId;
                this.monId = monId;
        }
}
