package jl95.tbb.pmon.update;

import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;

import java.util.List;

import static jl95.lang.SuperPowers.List;

public class PmonUpdateByMove {

    public interface UpdateOnTarget {

        interface Handlers {
            void miss();
            void hit(Hit hit);
        }

        void call(Handlers handlers);

        class Hit {
            public PmonUpdateByMoveDamage updateByDamage = null;
            public PmonUpdateByMoveStatModifier updateByStatModifier = null;
            public PmonUpdateByMoveStatusCondition updatesByStatusCondition = null;
        }

        static UpdateOnTarget miss() { return cb -> cb.miss(); }
        static UpdateOnTarget hit(Hit hit) { return cb -> cb.hit(hit); }
    }

    public List<Tuple3<PartyId, MonParty.MonId, UpdateOnTarget>> updatesOnTargets = List();
}
