package jl95.tbb.pmon;

public class Chanced<T> {
    public Integer chance;
    public T value;

    public Chanced(T value, Integer chance) {
        this.value = value;
        this.chance = chance;
    }
    public static <T> Chanced<T> certain(T value) {
        return new Chanced<>(value, 100);
    }
}
