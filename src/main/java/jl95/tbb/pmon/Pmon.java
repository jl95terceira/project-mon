package jl95.tbb.pmon;

import jl95.lang.StrictList;
import jl95.tbb.pmon.attrs.PmonAttributes;
import jl95.tbb.pmon.status.PmonStatus;
import jl95.util.AutoHashcoded;

import static jl95.lang.SuperPowers.*;

public class Pmon {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public final PmonAttributes attrs = new PmonAttributes();
    public final PmonStatus status = new PmonStatus();
    public final StrictList<PmonMove> moves = strict(List());

    public Pmon(Id id) {this.id = id;}

    public void restoreHp() {
        status.hp = attrs.baseStats.hp;
    }
}
