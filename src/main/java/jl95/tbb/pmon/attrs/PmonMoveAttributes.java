package jl95.tbb.pmon.attrs;

import jl95.lang.variadic.Tuple2;
import jl95.tbb.pmon.effect.PmonEffectByDamage;
import jl95.tbb.pmon.effect.PmonEffectByStatModify;
import jl95.tbb.pmon.effect.PmonEffectByStatusCondition;

import static jl95.lang.SuperPowers.*;

public class PmonMoveAttributes {

    public PmonMoveTargettingType targetType = PmonMoveTargettingType.FOE_SINGLE_MON; //TODO: validate move target(s) against targeting type, in PmonRuleToValidateDecision
    public Integer accuracy = 0;
    public Integer priorityModifier = 0;
    public Boolean interceptsSwitch = false;
    public PmonEffectByDamage damageEffect = new PmonEffectByDamage();
    public Tuple2<Integer, Integer> hitNrTimesRange = tuple(1,1);
    public PmonEffectByStatModify statModifierEffect = new PmonEffectByStatModify();
    public PmonEffectByStatusCondition statusConditionEffect = new PmonEffectByStatusCondition();

    public PmonMoveAttributes(PmonType pmonType) {
        this.damageEffect.pmonType = pmonType;
    }
}
