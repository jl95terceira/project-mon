package jl95.tbb.pmon.attrs;

import jl95.lang.SuperPowers;
import jl95.lang.variadic.Function0;
import jl95.tbb.pmon.Chanced;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonMove {

    public final String id;
    public PmonType type;
    public Integer damage = 0;
    public Integer accuracy = 0;
    public StrictMap<PmonStatModifierType, Chanced<Integer>> statModifiers = strict(Map());
    public List<Chanced<Function0<PmonStatusProblem>>> statusProblems = List();
    public List<Function0<PmonLingeringEffect>> lingeringEffects = List();
    public PmonMoveStatus status = new PmonMoveStatus();

    public PmonMove(String id,
                    PmonType type) {
        this.id = id;
        this.type = type;
    }
}
