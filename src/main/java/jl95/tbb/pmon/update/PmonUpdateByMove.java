package jl95.tbb.pmon.update;

import jl95.tbb.mon.MonId;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictList;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.strict;
import static jl95.tbb.pmon.PmonDefaults.*;

public class PmonUpdateByMove {

    public interface UsageResult {

        enum MissType {
            INACCURATE,
            NO_TARGET;
        }
        interface Handler {
            void hit        (Iterable<PmonUpdateOnTarget> atomicUpdates);
            void miss       (MissType type);
            void immobilised(PmonStatusCondition.Id conditionId);
        }

        void get(Handler handler);

        static UsageResult hit        (Iterable<PmonUpdateOnTarget> atomicUpdates) { return handler -> handler.hit(atomicUpdates); }
        static UsageResult miss       (MissType type)                              { return handler -> handler.miss(type); }
        static UsageResult immobilised(PmonStatusCondition.Id id)                  { return handler -> handler.immobilised(id); }
    }

    public MonId monId = NO_MON;
    public Integer moveIndex = -1;
    public StrictList<Tuple3<PartyId, MonFieldPosition, UsageResult>>
            usageResults = strict(List());
}
