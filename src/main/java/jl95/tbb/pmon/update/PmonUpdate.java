package jl95.tbb.pmon.update;

public interface PmonUpdate {

    void call(Callbacks callbacks);

    interface Callbacks {

        void move(PmonUpdateByMove update);
        void switchIn(PmonUpdateBySwitchIn update);
    }

    public static PmonUpdate byMove(PmonUpdateByMove update) {
        return cb -> cb.move(update);
    }
    public static PmonUpdate bySwitchIn(PmonUpdateBySwitchIn update) {
        return cb -> cb.switchIn(update);
    }
}
