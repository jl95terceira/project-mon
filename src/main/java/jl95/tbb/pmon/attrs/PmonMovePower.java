package jl95.tbb.pmon.attrs;

public interface PmonMovePower {

    interface Handler {

        void none();
        void typed(Integer power);
        void constant(Integer damage);
        void byHp(Double percent);
        void byMaxHp(Double percent);
    }

    void call(Handler handler);

    static PmonMovePower none() { return handler -> handler.none(); }
    static PmonMovePower typed(Integer power) { return handler -> handler.typed(power); }
    static PmonMovePower constant(Integer damage) { return handler -> handler.constant(damage); }
    static PmonMovePower byHp(Double percent) { return handler -> handler.byHp(percent); }
    static PmonMovePower byMaxHp(Double percent) { return handler -> handler.byMaxHp(percent); }
}
