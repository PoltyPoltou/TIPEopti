package standard;

import java.util.Arrays;
import java.util.HashMap;
import graph.*;
public class Taboo {
    private double result;
    private static Taboo singleInstance = null;

    public Taboo getInstance(Graph g) {
        if (singleInstance == null)
            singleInstance = new Taboo();
        return singleInstance;
    }

    public int[] solve(Graph g, int rememberTime) {
        int[] bestSol, actualSol, newSol;
        HashMap<Integer, int[]> forbidenPath = new HashMap<>();
        bestSol = g.genRoute();
        actualSol = Arrays.copyOf(bestSol, bestSol.length);
        for (int step = 0; step < 10000; step++) {

        }
        return bestSol;
    }

    private int[] gen2Opt(Graph g, HashMap forbidenPath) {

        return null;
    }
}