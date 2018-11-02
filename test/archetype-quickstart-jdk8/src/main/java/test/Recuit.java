package test;

import java.util.Arrays;
import java.util.Random;

public class Recuit {// one method to solve the problem
    float tempInit;
    float objFunctValue;

    public Recuit(float temp) {
        this.tempInit = temp;
    }

    public int[] solve2Opt(Graph g, float speedRate) {
        Random rand = new Random();
        float temp = tempInit;
        int[] actualSol = g.genRoute();
        float actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        float bestScore = new Float(actualScore);
        int i = 0;
        while (i < 1000) {
            double r = rand.nextDouble();
            int[] newSol = g.genRoute2Opt(actualSol);
            float newScore = g.evaluate(newSol);
            if (r < Math.exp((actualScore - newScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
            }
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = Arrays.copyOf(newSol, newSol.length);
                i = 0;
            }
            ++i;
            temp *= speedRate;
        }
        objFunctValue = bestScore;
        return bestSol;
    }

    public int[] solveRand(Graph g, float speedRate) {
        Random rand = new Random();
        float temp = tempInit;
        int[] actualSol = g.genRoute();
        float actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        float bestScore = new Float(actualScore);
        int i = 0;
        while (i < 1000) {
            double r = rand.nextDouble();
            int[] newSol = g.genRouteRdDist(actualSol);
            float newScore = g.evaluate(newSol);
            if (r < Math.exp((actualScore - newScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
            }
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = Arrays.copyOf(newSol, newSol.length);
                i = 0;
            }
            ++i;
            temp *= speedRate;
        }
        objFunctValue = bestScore;
        return bestSol;
    }

    public float getobjFunctValue() {
        return this.objFunctValue;
    }
}