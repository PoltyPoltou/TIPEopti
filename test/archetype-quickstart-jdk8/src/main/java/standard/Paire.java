package standard;

public class Paire {
    private int a;
    private int b;

    public Paire(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public boolean equals(Paire p) {// equality is when both elements are in both pairs
        return (this.a == p.getA() && this.b == p.getB()) || (this.a == p.getB() && this.b == p.getA());
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }
}