package test;

import java.util.LinkedList;
import java.util.Random;

public class Recuit {// one method to solve the problem
    float tempInit;

    public Recuit(float temp) {
        this.tempInit = temp;
    }

    public Integer[] solve(Graph g, FonctObj f, float speedRate) {
        Random rand = new Random();
        float temp = tempInit;
        LinkedList<Integer> actualSol = f.genRoute();
        float actualScore = f.evaluate(actualSol);
        LinkedList<Integer> bestSol = new LinkedList<Integer>();
        bestSol.add(actualSol.getFirst());
        float bestScore = new Float(actualScore);
        int i = 0;
        while (i < 100000) {
            double r = rand.nextDouble();
            LinkedList<Integer> newSol = f.genRouteRdDist(actualSol, temp / tempInit);
            float newScore = f.evaluate(newSol);
            if (r < Math.exp((actualScore - newScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
            }
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = new LinkedList<Integer>(newSol);
                i = 0;
            }
            ++i;
            temp *= speedRate;
        }
        return bestSol.toArray(new Integer[0]);
    }

}