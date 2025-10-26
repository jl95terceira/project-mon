package jl95.tbb.pmon;

import jl95.util.StrictList;
import jl95.tbb.pmon.status.PmonStatus;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.*;

public class Pmon {

    public static class Id {}

    public final Id id;
    public final StrictMap<PmonType.Id, PmonType> types = strict(Map());
    public final PmonStats baseStats = new PmonStats();
    public final StrictMap<PmonAbility.Id, PmonAbility> abilities = strict(Map());
    public final PmonStatus status = new PmonStatus();
    public final StrictList<PmonMove> moves = strict(List());

    public Pmon(Id id) {this.id = id;}

    public void restoreHp() {
        status.hp = baseStats.hp;
    }
}
