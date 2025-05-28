package jl95.tbb.pmon.update;

public interface PmonUpdate {

    void call(Handlers handlers);

    interface Handlers {

        void switchIn(PmonUpdateBySwitchIn update);
        void move    (PmonUpdateByMove update);
        // TODO: Are there other "types" of updates? Do we create a class for each possible "type" of update?
    }

    static PmonUpdate by(PmonUpdateBySwitchIn update) {
        return cb -> cb.switchIn(update);
    }
    static PmonUpdate by(PmonUpdateByMove update) {
        return cb -> cb.move(update);
    }
}
