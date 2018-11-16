package standard;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class Recuit {
    // one method to solve the problem
    // singleton class
    double tempInit;
    double speedRate;
    private static Recuit singleInstance = null;

    public static Recuit getInstance(double temp) {
        if (singleInstance == null)
            singleInstance = new Recuit();
        singleInstance.setInitialTemp(temp);
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

    public Route solveMethod(Graph g, Function<Route, Route> fun) {
        // not safe but still useful
        // fun must a be a route gen linked to g
        Random rand = new Random();
        double temp = tempInit;
        Route actualSol = g.genRoute();
        Route bestSol = new Route(Arrays.copyOf(actualSol.getTab(), actualSol.length), g);
        int i = 0;
        int retry = 0;
        while (retry < 100) {
            double r = rand.nextDouble();
            Route newSol;
            if (i == 100 || temp < Math.pow(1, -1) || actualSol.length == g.getLength()) {
                ++retry;
                i = 0;
                newSol = g.genRoute();
                temp = tempInit;
            } else
                newSol = fun.apply(actualSol);
            if (r < Math.exp((newSol.getScore() - actualSol.getScore()) / temp)) {
                actualSol = newSol;
            } else
                ++i;
            if (newSol.getScore() > actualSol.getScore()) {
                bestSol = newSol;
            }
            temp *= speedRate;
        }
        return bestSol;
    }

    public Route solveRand(Graph g) {
        Random rand = new Random();
        double temp = tempInit;
        Route actualSol = g.genRoute();
        Route bestSol;
        bestSol = new Route(Arrays.copyOf(actualSol.getTab(), actualSol.length),g);
        int i = 0;
        while (i < 1000) {
            double r = rand.nextDouble();
            Route newSol = g.genRoute();
            if (r < Math.exp((newSol.getScore() - actualSol.getScore()) / temp)) {
                actualSol = newSol;
            }
            if (newSol.getScore() > bestSol.getScore()) {
                bestSol = newSol;
                i = 0;
            }
            ++i;
            temp *= speedRate;
        }
        return bestSol;
    }

    public Route solveGreed(Graph g) {
        Route route = g.genRoute();
        Route actualRoute;
        Route bestRoute = route;
        for (int i = 0; i < 50; i++) {
            actualRoute = g.genRoute2OptGreed(route.getTab());
            if (!Arrays.equals(actualRoute.getTab(), route.getTab())) {
                --i;
                route = actualRoute;
            } else {
                if (bestRoute.getScore() < actualRoute.getScore())
                    bestRoute = actualRoute;
                route = g.genRoute();
            }
        }
        return bestRoute;
    }

}