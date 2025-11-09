package jl95.tbb.pmon.effect;

public class PmonEffects {

    public PmonEffectByDamage damage = new PmonEffectByDamage();
    public PmonEffectByHeal heal = new PmonEffectByHeal(); //TODO: use this
    public PmonEffectByStatModify stats = new PmonEffectByStatModify();
    public PmonEffectByStatusCondition status = new PmonEffectByStatusCondition();
    public PmonEffectByLockDecisionToUseMove lockDecision = new PmonEffectByLockDecisionToUseMove(); //TODO: use this
}
