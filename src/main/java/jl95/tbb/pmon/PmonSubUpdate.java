package jl95.tbb.pmon;

import jl95.tbb.pmon.attrs.PmonMoveEffectivenessType;
import jl95.tbb.pmon.attrs.PmonLingeringEffect;
import jl95.tbb.pmon.attrs.PmonStatModifierType;
import jl95.tbb.pmon.attrs.PmonStatusProblem;
import jl95.util.StrictMap;

import java.util.List;

import static jl95.lang.SuperPowers.*;

public class PmonSubUpdate {

    public Integer damage = null;
    public PmonMoveEffectivenessType effectivenessType = PmonMoveEffectivenessType.NORMAL;
    public StrictMap<PmonStatModifierType, Integer> statModifiers = strict(Map());
    public List<PmonStatusProblem> statusProblems = List();
    public Boolean disableLastMove = false;
    public Boolean exhaustLastMove = false;
    public List<PmonLingeringEffect> lingeringEffects = List();
    public Integer switchIn = null;
}
