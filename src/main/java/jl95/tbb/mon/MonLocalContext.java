package jl95.tbb.mon;

public class MonLocalContext<Mon> {

    public final MonParty<Mon> ownParty;

    public MonLocalContext(MonParty<Mon> ownParty) {

        this.ownParty = ownParty;
    }
}
