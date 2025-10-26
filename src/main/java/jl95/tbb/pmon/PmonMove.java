package jl95.tbb.pmon;

import jl95.lang.variadic.Function1;
import jl95.lang.variadic.Tuple2;
import jl95.tbb.pmon.effect.PmonEffects;
import jl95.tbb.pmon.status.PmonMoveStatus;

import static jl95.lang.SuperPowers.tuple;

public class PmonMove {

    public static class Id {}
    public enum Type {
        NORMAL,
        SPECIAL;
    }
    public interface Power {

        interface Handler {

            void none();
            void typed(Integer power);
            void constant(Integer damage);
            void byHp(Double percent);
            void byMaxHp(Double percent);
            void byOther(Function1<Integer, Pmon> damageFunction);
        }

        void call(Handler handler);

        static Power none() { return handler -> handler.none(); }
        static Power typed(Integer power) { return handler -> handler.typed(power); }
        static Power constant(Integer damage) { return handler -> handler.constant(damage); }
        static Power byHp(Double percent) { return handler -> handler.byHp(percent); }
        static Power byMaxHp(Double percent) { return handler -> handler.byMaxHp(percent); }
        static Power other(Function1<Integer, Pmon> damageFunction) { return handler -> handler.byOther(damageFunction); }
    }
    public enum TargettingType {

        NONE,
        SELF,
        FRIEND_SINGLE,
        FRIEND_ALL,
        FOE_SINGLE_MON,
        FOE_SINGLE_PARTY,
        FOE_ALL
    }
    public enum EffectivenessType {

        NORMAL,
        SUPER_EFFECTIVE,
        NOT_VERY_EFFECTIVE,
        DOES_NOT_AFFECT;
    }

    public final Id id;
    public PmonMove.TargettingType targetType = PmonMove.TargettingType.FOE_SINGLE_MON; //TODO: validate move target(s) against targeting type, in PmonRuleToValidateDecision
    public Integer accuracy = 100;
    public Integer priorityModifier = 0;
    public Boolean interceptsSwitch = false;
    public PmonEffects effects = new PmonEffects();
    public Tuple2<Integer, Integer> hitNrTimesRange = tuple(1,1);
    public PmonMoveStatus status = new PmonMoveStatus();

    public PmonMove(Id id,
                    PmonType pmonType) {
        this.id = id;
    }
}
