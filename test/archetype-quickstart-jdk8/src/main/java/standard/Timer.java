package standard;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import graph.*;

public class Timer {
    private double time, scoreMean, scoreMax;
    private String functName;
    private int iterations;

    public Timer(String functName, int iterations) {
        time = 0;
        scoreMean = Double.NEGATIVE_INFINITY;
        scoreMax = Double.NEGATIVE_INFINITY;
        this.functName = functName;
        this.iterations = iterations;
    }

    public void timeFunction(Recuit r, Graph g, Function<int[], int[]> funct, int retryCap) {
        Instant begin, end;
        double scoreSum = 0;
        begin = Instant.now();
        for (int i = 0; i < iterations; i++) {
            r.solveGen(g, funct, retryCap);
            scoreSum += r.getobjFunctValue();
            if (r.getobjFunctValue() > scoreMax)
                scoreMax = r.getobjFunctValue();
        }
        end = Instant.now();
        Duration d = Duration.between(begin, end);
        time = (d.getSeconds() + d.getNano() * Math.pow(10, -9)) / iterations;
        scoreMean = scoreSum / iterations;
    }

    public String toString() {
        return "La fonction " + functName + " a été éxécuté " + iterations + " fois.\n"
                + "Les résultats sont : score Max " + scoreMax + "\n" + "Score Moyen " + scoreMean + "\n"
                + "Temps moyen d'éxécution " + time;
    }

}