package jl95.tbb.pmon.decision;

import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.mon.MonId;

public class PmonDecisionToUseMove {

    public interface Target {

        interface Handler {
            void mon(MonId monId);
            void party(PartyId partyId);
            void all();
            void none();
        }

        void get(Handler handler);

        static Target mon(MonId monId) {
            return h -> h.mon(monId);
        }
        static Target party(PartyId partyId) {
            return h -> h.party(partyId);
        }
        static Target all() {
            return h -> h.all();
        }
        static Target none() {
            return h -> h.none();
        }
    }

    public Integer moveIndex = -1;
    public Target target = Target.none();
}
