package standard;

import java.time.Duration;
import java.time.Instant;
import graph.*;
import scpsolver.problems.LPSolution;

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
        Instant begin, end;
        GraphWeigthedArcs gwa = new GraphWeigthedArcs(35, 10, 10);
        begin = Instant.now();
        LPSolution simplexScore = SimplexLib.solveWithArcs(1, gwa);
        end = Instant.now();
        Duration d = Duration.between(begin, end);
        double simplexTime = d.getNano() * Math.pow(10, -9) + d.getSeconds();
        Recuit recuit = Recuit.getInstance(100, 0.95);
        Timer timer2Opt = new Timer("2opt", 20);
        Timer timerSubroute = new Timer("Subroute", 20);
        timer2Opt.timeFunction(recuit, gwa, gwa::genRoute2Opt, 100);
        timerSubroute.timeFunction(recuit, gwa, gwa::genRandWithBestSubRoute, 100);
        System.out.println(timerSubroute);
        System.out.println(timer2Opt);
        System.out.println(simplexTime);
        System.out.println(gwa);
        System.out.println(simplexScore);
        System.out.println("end");
    }

    public static double[] complexiteSimplex() {
        Instant begin, end;
        double[] times = new double[15];
        for (int i = 7; i < 8; i++) {
            begin = Instant.now();
            for (int j = 0; j < 5; j++) {
                Graph g = new Graph(5 * i, 15);
                for (int k = 0; k < 2; k++) {
                    SimplexLib.solveNoArcs(1, g);
                }
            }
            end = Instant.now();
            Duration d = Duration.between(begin, end);
            times[i] = (d.getNano() * Math.pow(10, -9) + d.getSeconds()) / 15;
        }
        return times;
    }
}
