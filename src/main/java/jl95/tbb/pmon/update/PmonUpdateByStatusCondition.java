package jl95.tbb.pmon.update;

import jl95.lang.SuperPowers;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.status.PmonStatusCondition;

import java.util.List;

import static jl95.lang.SuperPowers.List;

public class PmonUpdateByStatusCondition {

    public static class UpdateOnTarget {

        public List<PmonStatusCondition> statusConditions = SuperPowers.List();
    }

    public List<Tuple3<PartyId, MonParty.MonId, UpdateOnTarget>> updatesOnTargets = List();
}
