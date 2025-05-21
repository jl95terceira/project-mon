package jl95.tbb.pmon;

import java.util.List;

import static jl95.lang.SuperPowers.List;

public class Pmon {

    public final String id;
    public final PmonAttributes attrs = new PmonAttributes();
    public final PmonStatus status = new PmonStatus();
    public final List<PmonMove> moves = List();

    public Pmon(String id) {this.id = id;}
}
