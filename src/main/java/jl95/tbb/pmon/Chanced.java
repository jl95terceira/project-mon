package jl95.tbb.pmon;

public class Chanced<T> {
    public Integer chance = 100;
    public T value;

    public Chanced(T value) {
        this.value = value;
    }
}
