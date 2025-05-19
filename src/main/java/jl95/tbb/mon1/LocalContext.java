package jl95.tbb.mon1;

public class LocalContext<Mon> {

    public final Party<Mon> ownParty;

    public LocalContext(Party<Mon> ownParty) {

        this.ownParty = ownParty;
    }
}
