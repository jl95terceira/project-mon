package jl95.tbb.pmon;

import jl95.lang.SuperPowers;
import jl95.tbb.pmon.attrs.*;

import java.util.Set;

public class PmonFoeView {

    public final String id;
    public Set<PmonType> types = SuperPowers.Set();
    public PmonStatus status = new PmonStatus();

    public PmonFoeView(String id) {this.id = id;}
}
