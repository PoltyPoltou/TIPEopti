package standard;

public class Paire<T, U> {
    private T a;
    private U b;

    public Paire(T a, U b) {
        this.a = a;
        this.b = b;
    }

    public boolean equals(Paire<T, U> p) {// equality is when both elements are in both pairs
        return (this.a == p.getA() && this.b == p.getB()) || (this.a == p.getB() && this.b == p.getA());
    }

    public T getA() {
        return a;
    }

    public U getB() {
        return b;
    }
}