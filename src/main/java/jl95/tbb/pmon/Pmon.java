package jl95.tbb.pmon;

import jl95.tbb.pmon.attrs.*;

import java.util.List;
import java.util.Set;

import static jl95.lang.SuperPowers.List;
import static jl95.lang.SuperPowers.Set;

public class Pmon {

    public final String id;
    public final Set<PmonType> types = Set();
    public final PmonBaseStats baseStats = new PmonBaseStats();
    public final Set<PmonAbility> abilities = Set();
    public final List<PmonMove> moves = List();
    public final PmonStatus status = new PmonStatus();

    public Pmon(String id) {this.id = id;}
}
