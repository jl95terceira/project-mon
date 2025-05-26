package jl95.tbb.pmon.update;

import jl95.lang.variadic.Tuple3;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonParty;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonUpdateByMoveStatModifier {

    public StrictMap<PmonStatModifierType, Integer> statRaises = strict(Map());
    public StrictMap<PmonStatModifierType, Integer> statFalls = strict(Map());
}
