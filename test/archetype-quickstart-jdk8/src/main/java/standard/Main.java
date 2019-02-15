package standard;

import java.time.Duration;
import java.time.Instant;

import dataoutput.Timer;
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
        Timer.writeToFile("test", new String[] { "azd" }, new double[] { -1, 10 }, new double[] { 3, 4, 5 });
        Timer.graphStats(false, RecuitMethod.OPT, "gwatest.csv");
    }

    public static void gwaStats() {
        GraphWeightedArcs gwa;
        int iterations = 20;
        Recuit recuit = Recuit.getInstance(100, 0.95);
        Timer timer2Opt = new Timer(recuit, "2opt", iterations);
        Timer timerSubroute = new Timer(recuit, "Subroute", iterations);
        for (int nbNoeuds = 10; nbNoeuds < 200; nbNoeuds += 10) {
            gwa = new GraphWeightedArcs(nbNoeuds, 100, 100);
            timer2Opt.recordFunction(gwa, RecuitMethod.OPT, 100, "gwa2Opt" + nbNoeuds + ".csv");
            timerSubroute.recordFunction(gwa, RecuitMethod.BESTSUBROUTE, 100, "gwaSubRoute" + nbNoeuds + ".csv");
        }
    }

    public static void test() {
        Instant begin, end;
        GraphWeightedArcs gwa = new GraphWeightedArcs(10, 10, 5);
        begin = Instant.now();
        LPSolution simplexScore = SimplexLib.solveWithArcs(1, gwa);
        end = Instant.now();
        Duration d = Duration.between(begin, end);
        double simplexTime = d.getNano() * Math.pow(10, -9) + d.getSeconds();
        Recuit recuit = Recuit.getInstance(100, 0.95);
        Timer timer2Opt = new Timer(recuit, "2opt", 20);
        Timer timerSubroute = new Timer(recuit, "Subroute", 20);
        timer2Opt.timeFunction(gwa, RecuitMethod.OPT, 100);
        timerSubroute.timeFunction(gwa, RecuitMethod.BESTSUBROUTE, 100);
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
        for (int i = 1; i < 16; i++) {
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
