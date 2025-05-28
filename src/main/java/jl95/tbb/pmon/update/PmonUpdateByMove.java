package jl95.tbb.pmon.update;

import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonPosition;
import jl95.tbb.pmon.update.atomic.PmonAtomicEffect;

import java.util.List;

import static jl95.lang.SuperPowers.List;

public class PmonUpdateByMove {

    public interface UpdateOnTarget {

        interface Handlers {
            void miss();
            void hit(Iterable<PmonAtomicEffect> atomicUpdates);
            void noTarget();
        }

        void call(Handlers handlers);

        static UpdateOnTarget miss() { return cb -> cb.miss(); }
        static UpdateOnTarget hit(Iterable<PmonAtomicEffect> atomicUpdates) { return cb -> cb.hit(atomicUpdates); }
        static UpdateOnTarget noTarget() { return cb -> cb.noTarget(); }
    }

    public List<Tuple3<PartyId, MonPosition, UpdateOnTarget>> updatesOnTargets = List();
}
