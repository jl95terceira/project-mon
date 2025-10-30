package jl95.tbb.pmon.update;

public interface PmonUpdate {

    void get(Handler handler);

    interface Handler {

        void pass     (PmonUpdateByPass      update);
        void switchOut(PmonUpdateBySwitchOut update);
        void move     (PmonUpdateByMove      update);
        void other    (PmonUpdateByOther     update);
    }

    static PmonUpdate by(PmonUpdateByPass      update) { return handler -> handler.pass     (update); }
    static PmonUpdate by(PmonUpdateBySwitchOut update) {
        return handler -> handler.switchOut(update);
    }
    static PmonUpdate by(PmonUpdateByMove      update) {
        return handler -> handler.move     (update);
    }
    static PmonUpdate by(PmonUpdateByOther     update) {
        return handler -> handler.other    (update);
    }
}
