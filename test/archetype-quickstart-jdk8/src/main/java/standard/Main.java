package standard;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * Hello world!
 */
public final class Main {
    /**
     * Says hello to the world.
     * 
     * @param args
     *                 The arguments of the program.
     */
    public static void main(String[] args) {
        // Graph g = new Graph(new int[] { 1, 3, -10, 5, 7, -2, 4, 5, -8, 3, -5, 2 },
        // new int[] { 0, 1, 1, 3, 1, 11, 2, 10, 9, 10, 8, 11, 10, 11, 10, 4, 4, 7, 11,
        // 6, 6, 5, 8, 9 });
        double[] scores = Arrays.copyOf(new double[0], 1000);
        double mean = 0;
        double timeMean = 0;
        Instant begin, end;
        int dur2Opt, durSimplex;
        for (int i = 0; i < 500; i++) {
            Graph g = new Graph(50, 10);
            Recuit recuit = new Recuit(100);
            SimplexLib s = new SimplexLib(g);

            begin = Instant.now();
            recuit.solveGreed(g);
            scores[2 * i] = recuit.getobjFunctValue();
            end = Instant.now();
            dur2Opt = Duration.between(begin, end).getNano();

            begin = Instant.now();
            scores[2 * i + 1] = s.solve(1).getObjectiveValue();
            end = Instant.now();
            durSimplex = Duration.between(begin, end).getNano();

            timeMean = (timeMean * i + (double) dur2Opt / (double) durSimplex) / (i + 1);
            mean = (mean * i + scores[2 * i] / scores[2 * i + 1]) / (i + 1);
        }
    }
}
