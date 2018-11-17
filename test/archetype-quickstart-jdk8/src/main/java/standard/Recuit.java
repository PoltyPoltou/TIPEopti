package standard;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import graph.*;
public class Recuit {// one method to solve the problem
    private double tempInit;
    private double objFunctValue;
    private double speedRate;
    private static Recuit singleInstance = null;

    public static Recuit getInstance(double temp, double speed) {
        if (singleInstance == null)
            singleInstance = new Recuit();
        singleInstance.setInitialTemp(temp);
        singleInstance.setSpeedRate(speed);
        return singleInstance;
    }

    public static Recuit getInstance() {
        if (singleInstance == null)
            singleInstance = new Recuit();
        return singleInstance;
    }

    public void setInitialTemp(double temperature) {
        this.tempInit = temperature;
    }

    public void setSpeedRate(double rate) {
        this.speedRate = rate;
    }

    public int[] solveGen(Graph g, Function<int[], int[]> generation, int retryCap) {
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        int retry = 0;
        while (retry < retryCap) {
            double r = rand.nextDouble();
            int[] newSol;
            if (i == 100 || temp < Math.pow(1, -1) || actualSol.length == g.getLength()) {
                ++retry;
                i = 0;
                newSol = g.genRoute();
                temp = tempInit;
            } else
                newSol = generation.apply(actualSol);
            double newScore = g.evaluate(newSol);
            if (r < Math.exp((newScore - actualScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
                temp *= speedRate;
            } else
                ++i;
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = Arrays.copyOf(newSol, newSol.length);
            }
        }
        objFunctValue = bestScore;
        return bestSol;
    }

    public int[] solveRand(Graph g) {
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        while (i < 1000) {
            double r = rand.nextDouble();
            int[] newSol = g.genRoute();
            double newScore = g.evaluate(newSol);
            if (r < Math.exp((newScore - actualScore) / temp)) {
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

    public int[] solveGreed(Graph g) {
        int[] route = g.genRoute();
        int[] actualRoute;
        int[] bestRoute = route;
        for (int i = 0; i < 50; i++) {
            actualRoute = g.genRoute2OptGreed(route);
            if (!Arrays.equals(actualRoute, route)) {
                --i;
                route = actualRoute;
            } else {
                if (g.evaluate(bestRoute) < g.evaluate(actualRoute))
                    bestRoute = actualRoute;
                route = g.genRoute();
            }
        }
        objFunctValue = g.evaluate(bestRoute);
        return bestRoute;
    }

    public double getobjFunctValue() {
        return this.objFunctValue;
    }
}