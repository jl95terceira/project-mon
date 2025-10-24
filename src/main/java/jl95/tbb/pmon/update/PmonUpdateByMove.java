package jl95.tbb.pmon.update;

import jl95.util.StrictList;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_FIELD_POSITION;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_PARTY;

public class PmonUpdateByMove {

    public interface UsageResult {

        interface Handler {
            void hit     (Iterable<PmonUpdateOnTarget> atomicUpdates);
            void miss    ();
            void immobilised();
        }

        void call(Handler handler);

        static UsageResult hit     (Iterable<PmonUpdateOnTarget> atomicUpdates) { return handler -> handler.hit(atomicUpdates); }
        static UsageResult miss    ()                                           { return handler -> handler.miss(); }
        static UsageResult immobilised()                                        { return handler -> handler.immobilised(); }
    }

    public PartyId          partyId   = NO_PARTY;
    public MonFieldPosition monId     = NO_FIELD_POSITION;
    public Integer          moveIndex = -1;
    public StrictList<Tuple3<PartyId, MonFieldPosition, UsageResult>>
            statuses = strict(List());
}
