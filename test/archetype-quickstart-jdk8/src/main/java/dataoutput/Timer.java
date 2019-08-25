package dataoutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import graph.*;
import standard.Recuit;
import standard.RecuitMethod;
import standard.SimplexLib;

public class Timer {
    private double time, scoreMean, scoreMax;
    private String functName;
    private int iterations;
    private Recuit r;

    public Timer(Recuit r, String functName, int iterations) {
        time = 0;
        this.functName = functName;
        this.iterations = iterations;
        this.r = r;
    }

    public void timeFunction(Graph g, RecuitMethod method, int retryCap) {
        Instant begin, end;
        double scoreSum = 0;
        begin = Instant.now();
        for (int i = 0; i < iterations; i++) {
            r.solveGen(g, method, retryCap);
            scoreSum += r.getobjFunctValue();
            if (r.getobjFunctValue() > scoreMax)
                scoreMax = r.getobjFunctValue();
        }
        end = Instant.now();
        Duration d = Duration.between(begin, end);
        time = (d.getSeconds() + d.getNano() * Math.pow(10, -9)) / iterations;
        scoreMean = scoreSum / iterations;
    }

    public void recordFunction(Graph g, RecuitMethod method, int retryCap, String filePath) {
        Instant begin, end;
        int[] scores = new int[iterations];
        double[] times = new double[iterations];
        for (int i = 0; i < iterations; i++) {
            begin = Instant.now();
            r.solveGen(g, method, retryCap);
            end = Instant.now();
            Duration d = Duration.between(begin, end);
            times[i] = d.getSeconds() + d.getNano() * Math.pow(10, -9);
            scores[i] = (int) r.getobjFunctValue();
        }
        File file = new File(filePath);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            String str = "id,time,score\n";
            for (int i = 0; i < times.length; i++) {
                str += i + "," + times[i] + "," + scores[i] + "\n";
            }
            writer.write(str);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeToFile(String filePath, String[] names, double[]... data) {
        File file = new File(filePath);
        FileWriter writer;
        String str = "";
        for (String s : names) {
            str += s + ";";
        }
        str = str.substring(0, str.length() - 1);
        str += "\n";
        try {
            writer = new FileWriter(file);
            for (int col = 0; col < data.length; col++) {
                for (int lig = 0; lig < data[0].length; lig++) {
                    str += data[col][lig] + ";";
                }
                str = str.substring(0, str.length() - 1);
                str += "\n";
            }
            writer.write(str);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Graph the effects of the number of retries of the method to see improvement,
    // is there a cap?
    public static void graphRetry(GraphData gData, RecuitMethod method, String filePath) {
        int min = 25, max = 250, step = 25;
        ArrayList<Graph> list = gData.getList();
        double[][] scores = new double[list.size() / gData.getStep() + 1][(max - min) / step + 2];
        Recuit r = Recuit.getInstance();
        for (int retries = min; retries <= max; retries += step) {
            for (int index = 0; index < list.size(); index++) {
                if (scores[index / gData.getStep() + 1][0] == 0)
                    scores[index / gData.getStep() + 1][0] = list.get(index).getLength();
                if (scores[0][(retries - min) / step + 1] == 0)
                    scores[0][(retries - min) / step + 1] = retries;
                r.solveGen(list.get(index), method, retries);
                scores[index / gData.getStep() + 1][(retries - min) / step + 1] += r.getobjFunctValue()
                        / list.get(index).getBestScore() / gData.getStep();

            }
        }
        writeToFile(filePath + ".csv", new String[] { "retry\\noeuds" }, scores);
    }

    public static void graphTemp(GraphData gData, RecuitMethod method, String filePath) {
        int min = 50, max = 500, step = 50;
        ArrayList<Graph> list = gData.getList();
        double[][] scores = new double[list.size() / gData.getStep() + 1][(max - min) / step + 2];
        Recuit r = Recuit.getInstance();
        for (int temp = min; temp <= max; temp += step) {
            for (int index = 0; index < list.size(); index++) {
                if (scores[index / gData.getStep() + 1][0] == 0)
                    scores[index / gData.getStep() + 1][0] = list.get(index).getLength();
                if (scores[0][(temp - min) / step + 1] == 0)
                    scores[0][(temp - min) / step + 1] = temp;
                r.solveGen(temp, 0.95, list.get(index), method, 100);
                scores[index / gData.getStep() + 1][(temp - min) / step + 1] += r.getobjFunctValue()
                        / list.get(index).getBestScore() / gData.getStep();
            }
        }
        writeToFile(filePath + ".csv", new String[] { "temp\\noeuds" }, scores);
    }

    public static void graphDecay(GraphData gData, RecuitMethod method, String filePath) {
        int min = 900, max = 940, step = 5; // attention ce sont des pour mille
        ArrayList<Graph> list = gData.getList();
        double[][] scores = new double[list.size() / gData.getStep() + 1][(max - min) / step + 2];
        Recuit r = Recuit.getInstance();
        for (int decay = min; decay <= max; decay += step) {
            for (int index = 0; index < list.size(); index++) {
                if (scores[index / gData.getStep() + 1][0] == 0)
                    scores[index / gData.getStep() + 1][0] = list.get(index).getLength();
                if (scores[0][(decay - min) / step + 1] == 0)
                    scores[0][(decay - min) / step + 1] = (double) decay / 1000;
                r.solveGen(100, (double) decay / 1000, list.get(index), method, 100);
                scores[index / gData.getStep() + 1][(decay - min) / step + 1] += r.getobjFunctValue()
                        / list.get(index).getBestScore() / gData.getStep();
            }
        }
        writeToFile(filePath + ".csv", new String[] { "temp\\noeuds" }, scores);
    }

    /** 
     * record for random graph with different number of nodes the score and time of a method
     * if it is without weighted arcs simplex will be used to find the best score
     * @param arcs decides if <code>Graph</code> has weighted arcs
     * @param method which local search method is used
     * @param filePath name of the file where data is written
     */

    public static void graphStats(boolean arcs, RecuitMethod method, String filePath) {
        int nbNoeudsPas = 10;
        int noeudsMax = 100;
        int iterations = 20;
        int retryCap = 100;

        double[] uncertaintyTime = new double[(int) noeudsMax / nbNoeudsPas];
        double[] timeMean = new double[(int) noeudsMax / nbNoeudsPas];
        int[][] scores = new int[(int) noeudsMax / nbNoeudsPas][iterations];
        Recuit recuit = Recuit.getInstance();
        Graph g;
        Instant begin, end;
        ArrayList<SimplexLib> list = new ArrayList<>();
        for (int nbNoeuds = nbNoeudsPas; nbNoeuds <= noeudsMax; nbNoeuds += nbNoeudsPas) {
            int index = nbNoeuds / nbNoeudsPas - 1;
            double tempTime[] = new double[iterations];

            if (arcs) {
                g = new GraphWeightedArcs(nbNoeuds, 100, 100);
            } else {
                g = new Graph(nbNoeuds, 100);
                list.add(new SimplexLib(g));
                list.get(list.size() - 1).solveNoArcs(1);
            }

            for (int loop = 0; loop < iterations; loop++) {
                begin = Instant.now();
                recuit.solveGen(g, method, retryCap);
                end = Instant.now();
                Duration d = Duration.between(begin, end);
                tempTime[loop] = d.getSeconds() + d.getNano() * Math.pow(10, -9);
                scores[index][loop] = (int) recuit.getobjFunctValue();
            }

            double tempUncertainty = 0;
            for (int i = 0; i < iterations; i++) {
                timeMean[index] += tempTime[i] / iterations;
            }
            for (int i = 0; i < iterations; i++) {
                tempUncertainty += Math.pow(tempTime[i] - timeMean[index], 2) / (iterations - 1);
            }
            uncertaintyTime[index] = 2 * Math.sqrt(tempUncertainty / iterations);
        }
        File file = new File(filePath);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            String str;
            if (arcs)
                str = "nbNoeuds,temps moyen,incertitude,scores\n";
            else
                str = "nbNoeuds,temps moyen,incertitude,best,scores\n";
            for (int nbNoeuds = nbNoeudsPas; nbNoeuds <= noeudsMax; nbNoeuds += nbNoeudsPas) {
                int index = nbNoeuds / nbNoeudsPas - 1;
                str += nbNoeuds + "," + timeMean[index] + "," + uncertaintyTime[index];
                if (!arcs)
                    str += "," + list.get(index).getValue();
                for (int i = 0; i < iterations; i++) {
                    str += "," + scores[index][i];
                }
                str += "\n";
            }
            writer.write(str);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "La fonction " + functName + " a été éxécuté " + iterations + " fois.\n"
                + "Les résultats sont : score Max " + scoreMax + "\n" + "Score Moyen " + scoreMean + "\n"
                + "Temps moyen d'éxécution " + time + "\n";
    }

}