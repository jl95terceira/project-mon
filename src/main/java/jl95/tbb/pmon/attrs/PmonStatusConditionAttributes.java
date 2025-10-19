package jl95.tbb.pmon.attrs;

import static jl95.lang.SuperPowers.*;

import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonStatModifierType;
import jl95.util.*;

public class PmonStatusConditionAttributes {

    public StrictMap<PmonStatModifierType, Double> statFactors = strict(Map());
    public Function0<Double> cureChance = () -> 0.0; //TODO: use this
    public Function0<Double> immobiliseChance = () -> 0.0; //TODO: use this
    public Boolean allowDecide = true; //TODO: use this
    public Boolean allowSwitchOut = true;
    public Function3<PmonEffects, Tuple2<PartyId, MonFieldPosition>, Integer, Tuple2<PartyId, MonFieldPosition>> onDamage = (originMonId, damage, targetMonId) -> null; //TODO: use this
}
