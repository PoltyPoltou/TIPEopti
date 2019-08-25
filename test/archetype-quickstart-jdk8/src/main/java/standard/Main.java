package standard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import dataoutput.GraphData;
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

        System.out.println("Hello world!");
        GraphData gData = GraphData.loadGraphs("gdataWithScore10_10_100_10.txt");
        testSituationReelle();
    }

    public static double mean(double[] tab) {
        double mean = 0;
        for (double i : tab) {
            mean += i;
        }
        return mean / tab.length;
    }

    public static double variance(double[] tab) {
        double var = 0;
        double mean = mean(tab);
        for (double i : tab) {
            var += Math.pow(i - mean, 2);
        }
        return var / tab.length;
    }

    public static void testSituationReelle() {
        int n = 500;
        GraphWeightedArcs graph = new GraphWeightedArcs(n, 100, 100);
        Recuit r = Recuit.getInstance();
        double t = System.currentTimeMillis();
        r.solveGen(1000, 0.95, graph, RecuitMethod.MIXT2, 100);
        System.out.println(
                "Resultat : score " + r.getobjFunctValue() + " temps " + (System.currentTimeMillis() - t) / 1000);
    }

    public static void gwaStats() {
        GraphWeightedArcs gwa;
        int iterations = 20;
        Recuit recuit = Recuit.getInstance();
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
        Recuit recuit = Recuit.getInstance();
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

    public static double[] complexiteSimplexe() {
        Instant begin, end;
        double[] times = new double[15];
        for (int i = 1; i < 16; i++) {
            GraphWeightedArcs g = new GraphWeightedArcs(i + 1, 100, 100);
            begin = Instant.now();
            SimplexLib.solveWithArcs(1, g);
            end = Instant.now();
            Duration d = Duration.between(begin, end);
            times[i] = (d.getNano() * Math.pow(10, -9) + d.getSeconds());
        }
        return times;
    }
}
