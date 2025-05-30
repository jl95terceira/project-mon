package jl95.tbb.pmon.attrs;

public interface PmonMovePower {

    interface Handlers {

        void typed(Integer power);
        void constant(Integer damage);
        void byHp(Double percent);
        void byMaxHp(Double percent);
    }

    void call(Handlers handlers);

    static PmonMovePower typed(Integer power) { return handlers -> handlers.typed(power); }
    static PmonMovePower constant(Integer damage) { return handlers -> handlers.constant(damage); }
    static PmonMovePower byHp(Double percent) { return handlers -> handlers.byHp(percent); }
    static PmonMovePower byMaxHp(Double percent) { return handlers -> handlers.byMaxHp(percent); }
}
