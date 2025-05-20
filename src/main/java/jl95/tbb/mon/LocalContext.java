package jl95.tbb.mon;

public class LocalContext<Mon> {

    public final Party<Mon> ownParty;

    public LocalContext(Party<Mon> ownParty) {

        this.ownParty = ownParty;
    }
}
