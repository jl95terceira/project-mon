package jl95.tbb.pmon;

import static jl95.lang.SuperPowers.*;

import jl95.tbb.pmon.status.PmonStatus;
import jl95.util.StrictMap;

import static jl95.lang.SuperPowers.strict;

public class PmonPartyPublicView {

    public final Pmon.Id id;
    public StrictMap<PmonType.Id, PmonType> types = strict(Map());
    public PmonStatus status = new PmonStatus();

    public PmonPartyPublicView(Pmon.Id id) {this.id = id;}
}
