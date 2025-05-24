package jl95.tbb.pmon.attrs;

public interface PmonMovePower {

    interface Callbacks {

        void typed(Integer power);
        void constant(Integer damage);
        void byHp(Double percent);
        void byMaxHp(Double percent);
    }

    void call(Callbacks cbs);

    static PmonMovePower typed(Integer power) { return cb -> cb.typed(power); }
    static PmonMovePower constant(Integer damage) { return cb -> cb.constant(damage); }
    static PmonMovePower byHp(Double percent) { return cb -> cb.byHp(percent); }
    static PmonMovePower byMaxHp(Double percent) { return cb -> cb.byMaxHp(percent); }
}
