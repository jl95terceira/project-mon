package jl95.tbb.mon;

public class MonLocalContext<Mon, Party extends MonParty<Mon>> {

    public final Party ownParty;

    public MonLocalContext(Party ownParty) {

        this.ownParty = ownParty;
    }
}
