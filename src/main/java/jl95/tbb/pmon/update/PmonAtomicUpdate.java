package jl95.tbb.pmon.update;

public interface PmonAtomicUpdate {

    void call(Callbacks callbacks);

    interface Callbacks {

        void move(PmonAtomicUpdateByMove update);
        void switchIn(PmonAtomicUpdateBySwitchIn update);
    }

    public static PmonAtomicUpdate byMove(PmonAtomicUpdateByMove update) {
        return cb -> cb.move(update);
    }
    public static PmonAtomicUpdate bySwitchIn(PmonAtomicUpdateBySwitchIn update) {
        return cb -> cb.switchIn(update);
    }
}
