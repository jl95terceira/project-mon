package jl95.tbb.pmon.update.atomic;

public interface PmonAtomicEffect {

    interface Handlers {

        void damage(PmonAtomicEffectByDamage update);
        void statModify(PmonAtomicEffectByStatModifier update);
        void statusCondition(PmonAtomicEffectByStatusCondition update);
        void switchIn(PmonAtomicEffectBySwitchIn update);
    }

    void call(Handlers handlers);

    static PmonAtomicEffect by(PmonAtomicEffectByDamage update) { return h -> h.damage(update); }
    static PmonAtomicEffect by(PmonAtomicEffectByStatModifier update) { return h -> h.statModify(update); }
    static PmonAtomicEffect by(PmonAtomicEffectByStatusCondition update) { return h -> h.statusCondition(update); }
    static PmonAtomicEffect by(PmonAtomicEffectBySwitchIn update) { return h -> h.switchIn(update); }
}
