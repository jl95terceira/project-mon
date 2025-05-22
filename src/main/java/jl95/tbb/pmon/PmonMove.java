package jl95.tbb.pmon;

import jl95.tbb.pmon.attrs.PmonMoveAttributes;
import jl95.tbb.pmon.attrs.PmonType;
import jl95.tbb.pmon.status.PmonMoveStatus;

public class PmonMove {

    public final String id;
    public PmonMoveAttributes attrs;
    public PmonMoveStatus status = new PmonMoveStatus();

    public PmonMove(String id,
                    PmonType pmonType) {
        this.id = id;
        this.attrs = new PmonMoveAttributes(pmonType);
    }
}
