package standard;

import java.util.Arrays;
import java.util.Random;

public class Recuit {// one method to solve the problem
    double tempInit;
    double objFunctValue;

    public Recuit(float temp) {
        this.tempInit = temp;
    }

    public int[] solve2Opt(Graph g, double speedRate) {
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        int retry = 0;
        while (retry < 100) {
            double r = rand.nextDouble();
            int[] newSol;
            if (i == 100 || temp < Math.pow(1, -1)) {
                ++retry;
                i = 0;
                newSol = g.genRoute();
                temp = tempInit;
            } else
                newSol = g.genRoute2Opt(actualSol);
            double newScore = g.evaluate(newSol);
            if (r < Math.exp((newScore - actualScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
            } else
                ++i;
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = Arrays.copyOf(newSol, newSol.length);
            }
            temp *= speedRate;
        }
        objFunctValue = bestScore;
        return bestSol;
    }

    public int[] solveRand(Graph g, double speedRate) {
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

    public int[] solve2OptBis(Graph g, double speedRate) {
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        while (i < 100000) {
            double r = rand.nextDouble();
            int[] newSol = g.genRoute2OptBis(actualSol);
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

    public int[] solveBestSubRoute(Graph g, double speedRate) {
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        int retry = 0;
        while (retry < 100) {
            double r = rand.nextDouble();
            int[] newSol;
            if (i == 100 || temp < Math.pow(1, -1)) {
                ++retry;
                i = 0;
                newSol = g.genRoute();
                temp = tempInit;
            } else
                newSol = g.genRandWithBestSubRoute(actualSol);
            double newScore = g.evaluate(newSol);
            if (r < Math.exp((newScore - actualScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
            } else
                ++i;
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = newSol;
                // System.out.println(newScore + "," + retry);
            }
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