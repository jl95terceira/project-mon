package jl95.tbb.pmon.status;

import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.effect.PmonEndOfTurnEffects;
import jl95.util.StrictList;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonStatusCondition {

    public static class Id {}

    public final Id id;
    public Boolean allowDecide = true; //TODO: use this
    public Boolean allowSwitchOut = true;
    public StrictMap<PmonStatModifierType, Double> statFactors = strict(Map());
    public Function0<Integer> cureChanceBeforeMove = () -> 0; //TODO: use this
    public Function0<Integer> immobiliseChanceOnMove = () -> 0; //TODO: use this
    public Function0<Integer> cureChanceAfterTurn = () -> 0; //TODO: use this
    public Method3<PartyId, MonFieldPosition, Integer> onDamage = (partyId, monId, damage) -> {}; //TODO: use this
    public Method0 afterTurn = () -> {}; //TODO: use this
    public Function0<StrictList<Tuple3<PartyId, MonFieldPosition, PmonEndOfTurnEffects>>> afterTurnEffects = () -> strict(List());

    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
