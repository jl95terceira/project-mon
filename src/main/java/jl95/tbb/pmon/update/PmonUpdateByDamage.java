package jl95.tbb.pmon.update;

import jl95.lang.SuperPowers;
import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.tbb.pmon.status.PmonStatusCondition;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateByDamage {

    public static class UpdateOnTarget {

        public Integer damage = null;
        public PmonMoveEffectivenessType effectivenessType = PmonMoveEffectivenessType.NORMAL;
    }

    public List<Tuple3<PartyId, MonParty.MonId, UpdateOnTarget>> updatesOnTargets = List();
}
