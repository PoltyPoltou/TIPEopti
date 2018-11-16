package standard;

import java.util.Arrays;

public class Route {
    public int length;
    private int[] route;
    private int score;
    private Graph g;

    public Route(int[] route, Graph g) {
        this.route = route;
        this.length = route.length;
        this.g = g;
        this.evaluate();
    }

    public Route(int size, Graph g) {
        route = new int[size];
        this.length = size;
        Arrays.fill(route, -1);
        this.g = g;
    }

    public int getStop(int index) {
        return route[index];
    }

    public void setStop(int index, int stop) {
        if (route[index] != -1)
            score -= g.getValue(route[index]);
        route[index] = stop;
        score += g.getValue(stop);
    }

    public int evaluate() {
        if (g.evaluate(route) == score)
            throw (new AssertionError());
        return score;
    }

    public int[] getTab() {
        return route;
    }

    public int getScore() {
        return score;
    }
}