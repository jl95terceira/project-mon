package jl95.tbb.pmon.update;

public interface PmonUpdate {

    void call(Callbacks callbacks);

    interface Callbacks {

        void switchIn(PmonUpdateBySwitchIn update);
        void damage(PmonUpdateByDamage update);
        void statModify(PmonUpdateByStatModify update);
        void statusCondition(PmonUpdateByStatusCondition update);
        // TODO: Are there other "types" of updates? Do we create a class for each possible "type" of update?
    }

    static PmonUpdate bySwitchIn(PmonUpdateBySwitchIn update) {
        return cb -> cb.switchIn(update);
    }
    static PmonUpdate byDamage(PmonUpdateByDamage update) {
        return cb -> cb.damage(update);
    }
    static PmonUpdate byStatModify(PmonUpdateByStatModify update) { return cb -> cb.statModify(update); }
    static PmonUpdate byStatusCondition(PmonUpdateByStatusCondition update) { return cb -> cb.statusCondition(update); }
}
