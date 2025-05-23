package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.tbb.pmon.attrs.*;
import jl95.tbb.pmon.status.PmonStatus;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.strict;

public class PmonFoeView {

    public final Pmon.Id id;
    public StrictMap<PmonType.Id, PmonType> types = strict(Map());
    public PmonStatus status = new PmonStatus();

    public PmonFoeView(Pmon.Id id) {this.id = id;}
}
