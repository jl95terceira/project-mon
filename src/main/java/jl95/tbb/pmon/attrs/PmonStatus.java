package jl95.tbb.pmon.attrs;

import jl95.util.StrictMap;

public class PmonStatus {

    public Integer hp = 0;
    public StrictMap<PmonStatModifierType, Integer> statModifierStages;
    public StrictMap<String, PmonStatusProblem> statusProblems;
    public StrictMap<String, PmonLingeringEffect> lingeringEffects;
}
