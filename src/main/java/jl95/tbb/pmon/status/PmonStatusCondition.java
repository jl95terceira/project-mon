package jl95.tbb.pmon.status;

import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.PmonLocalContext;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonStatusCondition {

    public static class Id {}
    @FunctionalInterface public interface AfterTurnEffects {
        StrictMap<Tuple2<PartyId, MonFieldPosition>, PmonEffects> apply(PartyId partyId, MonFieldPosition monId, PmonLocalContext context);
    }

    public final Id id;
    public Boolean allowDecide = true; //TODO: use this
    public Boolean allowSwitchOut = true;
    public StrictMap<PmonStatModifierType, Double> statFactorsOnSelf = strict(Map());
    public Function0<Integer> cureChanceBeforeMove = () -> 0; //TODO: use this
    public Function0<Integer> immobiliseChanceOnMove = () -> 0;
    public Function0<PmonEffects> onImmobilisedEffectsOnSelf = () -> null;
    public Method3<PartyId, MonFieldPosition, Integer> onDamageToSelf = (partyId, monId, damage) -> {};
    public Function1<PmonEffects, Integer> onDamageToSelfEffectsOnFoe = (damage) -> null;
    public Function1<PmonEffects, Integer> onDamageToSelfEffectsOnSelf = (damage) -> null;
    public Boolean untargetable = false;
    public Method0 afterTurn = () -> {};
    public AfterTurnEffects afterTurnEffects = (partyId,monId,context) -> strict(Map());

    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
