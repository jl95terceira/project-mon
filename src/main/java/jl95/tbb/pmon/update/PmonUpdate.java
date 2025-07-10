package jl95.tbb.pmon.update;

public interface PmonUpdate {

    void call(Handlers handlers);

    interface Handlers {

        void pass     (PmonUpdateByPass      update);
        void switchOut(PmonUpdateBySwitchOut update);
        void move     (PmonUpdateByMove      update);
    }

    static PmonUpdate by(PmonUpdateByPass      update) { return handlers -> handlers.pass     (update); }
    static PmonUpdate by(PmonUpdateBySwitchOut update) {
        return handlers -> handlers.switchOut(update);
    }
    static PmonUpdate by(PmonUpdateByMove      update) {
        return handlers -> handlers.move     (update);
    }
}
