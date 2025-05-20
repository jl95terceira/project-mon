package jl95.tbb.pmon;

import java.util.List;
import java.util.Set;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.Set;

public class Pmon {

    public static class BaseStats {

        public Integer hp = 0;
        public Integer attack = 0;
        public Integer defense = 0;
        public Integer specialAttack = 0;
        public Integer specialDefense = 0;
        public Integer speed = 0;
    }
    public static class Status {

        public static abstract class Problem {

            public final String name;
            public Problem(String name) {
                this.name = name;
            }
        }
        public static class StatModifier {
            public enum Type {
                ATTACK,
                DEFENSE,
                SPECIAL_ATTACK,
                SPECIAL_DEFENSE,
                SPEED,
                ACCURACY,
                EVASION;
            }
        }

        public Integer hp = 0;
    }

    public final String name;
    public final Set<PmonType> types = Set();
    public final BaseStats baseStats = new BaseStats();
    public final Set<PmonAbility> abilities = Set();
    public final List<PmonMove> moves = List();

    public Pmon(String name) {this.name = name;}
}
