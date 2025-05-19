package jl95.tbb.pmon;

import java.util.List;
import java.util.Set;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.Set;

public class Pmon {

    public static class BaseStats {

        public Integer hp = 0;
        public Integer atk = 0;
        public Integer def = 0;
        public Integer spAtk = 0;
        public Integer spDef = 0;
        public Integer speed = 0;
    }
    public static class Status {

        public static abstract class Problem {

            public final String name;
            public Problem(String name) {
                this.name = name;
            }
        }
        public static class StatModifier {}

        public Integer hp = 0;
    }

    public final Set<PmonType> types = Set();
    public final BaseStats baseStats = new BaseStats();
    public final Set<PmonAbility> abilities = Set();
    public final List<PmonMove> moves = List();
}
