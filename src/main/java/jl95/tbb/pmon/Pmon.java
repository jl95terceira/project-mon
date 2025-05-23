package jl95.tbb.pmon;

import jl95.tbb.pmon.attrs.PmonAttributes;
import jl95.tbb.pmon.status.PmonStatus;
import jl95.util.AutoHashcoded;

import java.util.List;

import static jl95.lang.SuperPowers.List;

public class Pmon {

    public static class Id extends AutoHashcoded {}

    public final Id id;
    public final PmonAttributes attrs = new PmonAttributes();
    public final PmonStatus status = new PmonStatus();
    public final List<PmonMove> moves = List();

    public Pmon(Id id) {this.id = id;}
}
