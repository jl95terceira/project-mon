package jl95.tbb.pmon.status;

import jl95.lang.variadic.*;
import jl95.tbb.PartyId;
import jl95.tbb.mon.MonFieldPosition;
import jl95.tbb.pmon.Pmon;
import jl95.tbb.pmon.PmonLocalContext;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.util.StrictList;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class PmonStatusCondition {

    public static class Id {}

    public final Id id;
    public Boolean allowDecide = true; //TODO: use this
    public Boolean allowSwitchOut = true;
    public StrictMap<PmonStatModifierType, Double> statFactors = strict(Map());
    public Function0<Integer> immobiliseChanceOnMove = () -> 0;
    public Method3<PartyId, MonFieldPosition, Integer> onDamage = (partyId, monId, damage) -> {};
    public Function1<PmonEffects, Integer> onDamageEffectsOnFoe = (damage) -> null;
    public Function1<PmonEffects, Integer> onDamageEffectsOnSelf = (damage) -> null;
    public Method0 afterTurn = () -> {};
    public Function3<StrictMap<Tuple2<PartyId, MonFieldPosition>, PmonEffects>,PartyId,MonFieldPosition,PmonLocalContext>
            afterTurnEffects = (partyId,monId,context) -> strict(Map());

    public PmonStatusCondition(Id id) {
        this.id = id;
    }
}
