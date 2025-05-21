package jl95.tbb.pmon;

public enum PmonDecisionPriorityType {
    MOVE(0),
    SWITCH_IN(1);

    public final Integer value;
    PmonDecisionPriorityType(Integer value) {this.value = value;}
}
