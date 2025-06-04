package jl95.tbb.pmon.update;

import jl95.lang.StrictList;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.update.atomic.PmonAtomicEffect;

import java.util.List;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_FIELD_POSITION;
import static jl95.tbb.pmon.attrs.PmonDefaults.NO_PARTY;

public class PmonUpdateByMove {

    public interface UpdateOnTarget {

        interface Handlers {
            void miss();
            void hit(Iterable<PmonAtomicEffect> atomicUpdates);
            void noTarget();
        }

        void call(Handlers handlers);

        static UpdateOnTarget miss() { return handlers -> handlers.miss(); }
        static UpdateOnTarget hit(Iterable<PmonAtomicEffect> atomicUpdates) { return handlers -> handlers.hit(atomicUpdates); }
        static UpdateOnTarget noTarget() { return handlers -> handlers.noTarget(); }
    }

    public PartyId partyId = NO_PARTY;
    public MonFieldPosition monId = NO_FIELD_POSITION;
    public Integer moveIndex = -1;
    public StrictList<Tuple3<PartyId, MonFieldPosition, UpdateOnTarget>> updatesOnTargets = strict(List());
}
