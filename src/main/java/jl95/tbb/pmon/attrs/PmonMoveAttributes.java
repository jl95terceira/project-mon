package jl95.tbb.pmon.attrs;

import jl95.lang.variadic.Tuple2;
import jl95.tbb.pmon.effect.PmonEffects;

import static jl95.lang.SuperPowers.*;

public class PmonMoveAttributes {

    public PmonMoveTargettingType targetType = PmonMoveTargettingType.FOE_SINGLE_MON; //TODO: validate move target(s) against targeting type, in PmonRuleToValidateDecision
    public Integer accuracy = 100;
    public Integer priorityModifier = 0;
    public Boolean interceptsSwitch = false;
    public PmonEffects effects = new PmonEffects();
    public Tuple2<Integer, Integer> hitNrTimesRange = tuple(1,1);

    public PmonMoveAttributes(PmonType pmonType) {
        this.effects.damage.pmonType = pmonType;
    }
}
