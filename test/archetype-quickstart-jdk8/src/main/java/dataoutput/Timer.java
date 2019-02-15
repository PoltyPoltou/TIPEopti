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
        writeDataToFile(times, scores, filePath);

    }

    private static void writeDataToFile(double[] times, int[] scores, String filePath) {
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
            str += s + ",";
        }
        str = str.substring(0, str.length() - 1);
        str += "\n";
        try {
            writer = new FileWriter(file);
            for (int dataIndex = 0; dataIndex < data[0].length; dataIndex++) {
                for (int type = 0; type < data.length; type++) {
                    str += data[type][dataIndex] + ",";
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

    /** 
     * record for random graph with different number of nodes the score and time of a method
     * if it is without arcs simplex will be used to find the best score
     * @param arcs decides if <code>Graph</code> has weighted arcs
     * @param method which local search method is used
     * @param filePath name of the file where data is written
     */

    public static void graphStats(boolean arcs, RecuitMethod method, String filePath) {
        int nbNoeudsPas = 10;
        int noeudsMax = 100;
        int iterations = 20;
        int retryCap = 100;
        int temperature = 100;
        float speed = 0.95f;

        double[] uncertaintyTime = new double[(int) noeudsMax / nbNoeudsPas];
        double[] timeMean = new double[(int) noeudsMax / nbNoeudsPas];
        int[][] scores = new int[(int) noeudsMax / nbNoeudsPas][iterations];
        Recuit recuit = Recuit.getInstance(temperature, speed);
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
                tempUncertainty += (tempTime[i] - timeMean[index]) / (iterations - 1);
            }
            uncertaintyTime[index] = 2 * Math.sqrt(tempUncertainty / iterations);
        }
        File file = new File(filePath);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            String str = "nbNoeuds,temps moyen,incertitude,best,scores\n";
            for (int nbNoeuds = nbNoeudsPas; nbNoeuds <= noeudsMax; nbNoeuds += nbNoeudsPas) {
                int index = nbNoeuds / nbNoeudsPas - 1;
                str += nbNoeuds + "," + timeMean[index] + "," + uncertaintyTime[index] + ","
                        + list.get(index).getValue();
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