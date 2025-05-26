package jl95.tbb.pmon.update;

public interface PmonUpdate {

    void call(Handlers handlers);

    interface Handlers {

        void switchIn(PmonUpdateBySwitchIn update);
        void move(PmonUpdateByMoveDamage update);
        void notify(PmonNotification update);
        // TODO: Are there other "types" of updates? Do we create a class for each possible "type" of update?
    }

    static PmonUpdate bySwitchIn(PmonUpdateBySwitchIn update) {
        return cb -> cb.switchIn(update);
    }
    static PmonUpdate byMove(PmonUpdateByMoveDamage update) {
        return cb -> cb.move(update);
    }
    static PmonUpdate byNotify(PmonNotification update) {return cb -> cb.notify(update); }
}
