package jl95.tbb.pmon;

import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonType;
import jl95.tbb.pmon.status.PmonMoveStatus;
import jl95.util.AutoHashcoded;

public class PmonMove {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public PmonMoveAttributes attrs;
    public PmonMoveStatus status = new PmonMoveStatus();

    public PmonMove(Id id,
                    PmonType pmonType) {
        this.id = id;
        this.attrs = new PmonMoveAttributes(pmonType);
    }
}
