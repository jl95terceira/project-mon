package jl95.tbb.pmon.update;

public interface PmonUpdateOnTarget {

    interface Handlers {

        void damage(PmonUpdateOnTargetByDamage update);
        void statModify(PmonUpdateOnTargetByStatModifier update);
        void statusCondition(PmonUpdateOnTargetByStatusCondition update);
        void switchIn(PmonUpdateOnTargetBySwitchIn update);
    }

    void call(Handlers handlers);

    static PmonUpdateOnTarget by(PmonUpdateOnTargetByDamage update) { return h -> h.damage(update); }
    static PmonUpdateOnTarget by(PmonUpdateOnTargetByStatModifier update) { return h -> h.statModify(update); }
    static PmonUpdateOnTarget by(PmonUpdateOnTargetByStatusCondition update) { return h -> h.statusCondition(update); }
    static PmonUpdateOnTarget by(PmonUpdateOnTargetBySwitchIn update) { return h -> h.switchIn(update); }
}
