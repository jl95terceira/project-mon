package jl95.tbb.pmon.effect;

public interface PmonEffect {

    interface Handlers {

        void damage(PmonEffectByDamage update);
        void statModify(PmonEffectByStatModifier update);
        void statusCondition(PmonEffectByStatusCondition update);
        void switchIn(PmonEffectBySwitchIn update);
    }

    void call(Handlers handlers);

    static PmonEffect by(PmonEffectByDamage update) { return h -> h.damage(update); }
    static PmonEffect by(PmonEffectByStatModifier update) { return h -> h.statModify(update); }
    static PmonEffect by(PmonEffectByStatusCondition update) { return h -> h.statusCondition(update); }
    static PmonEffect by(PmonEffectBySwitchIn update) { return h -> h.switchIn(update); }
}
