File GraphData
package dataoutput;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import graph.Graph;
import standard.SimplexLib;

public class GraphData {
    private ArrayList<Graph> list;
    private int step;

    public GraphData(int nbGraphsPerSize, int minSize, int maxSize, int step) {
        list = new ArrayList<>();
        this.step = step;
        for (int nodes = minSize; nodes <= maxSize; nodes += step) {
            for (int j = 0; j < nbGraphsPerSize; j++) {
                list.add(new Graph(nodes, 100));
            }
        }
    }

    private GraphData(ArrayList<Graph> list) {
        this.list = list;
        this.step = 1;
        for (int i = 1; i < list.size(); i++) {
            if(list.get(i-1).getLength() != list.get(i).getLength()){
                this.step = i;
                break;
            }
        }
    }

    public int getStep() {
        return this.step;
    }

    public void runBestScores() {
        for (Graph g : list) {
            if (g.getBestScore() == -1)
                g.setBestScore((int) SimplexLib.solveNoArcs(1, g).getObjectiveValue());
        }
    }

    public void runBestScores(String filePath) {
        for (Graph g : list) {
            if (g.getBestScore() == -1) {
                g.setBestScore((int) SimplexLib.solveNoArcs(1, g).getObjectiveValue());
                this.saveGraph(filePath);
            }
        }
    }

    public ArrayList<Graph> getList() {
        return list;
    }

    public void reGenGraph(int index) {
        Graph g = list.get(index);
        list.set(index, new Graph(g.getLength(), 100));
    }

    public void saveGraph(String filePath) {
        ObjectOutputStream stream;
        try {
            stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filePath))));
            stream.writeInt(list.size());
            for (Graph g : list) {
                stream.writeObject(g);
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public GraphData loadGraphs(String filePath) {
        ObjectInputStream stream;
        ArrayList<Graph> lst = new ArrayList<>();
        try {
            stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(filePath))));
            int size = stream.readInt();
            for (int i = 0; i < size; i++) {
                lst.add((Graph) stream.readObject());
            }
            stream.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new GraphData(lst);
    }
}
File Timer
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

    private static void writeToFile(String filePath, String[] names, double[]... data) {
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
File Graph

package graph;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class Graph implements Serializable {

    private static final long serialVersionUID = 1L;
    protected int[] nodes;// Store the data of each nodes
    protected int[][] neighbourCheck;// matrice to access availibilty of each vertices
    protected int[][] neighbourTab;// list the available nodes from one
    protected final int RANGECST = 3;// how far can we look to get a new
    protected final int SCALE = 1;// > how big can be the routes
    protected int bestScore = -1;

    public Graph() {
    }

    public Graph(int[] nodes, Paire[] vertices) {// warning complexity heavy
        this.nodes = nodes;
        this.neighbourCheck = createNeighbourCheck(vertices);
        this.neighbourTab = createNeighbour(this.neighbourCheck);
    }

    public Graph(int[] nodes, int[] paires) {// paires.length must be even !
        Paire[] vertices = new Paire[paires.length / 2];
        for (int i = 0; i < paires.length; i += 2) {
            vertices[i / 2] = new Paire(paires[i], paires[i + 1]);
        }
        this.nodes = nodes;
        this.neighbourCheck = createNeighbourCheck(vertices);
        this.neighbourTab = createNeighbour(this.neighbourCheck);
    }

    public Graph(int size, int valueBound) {// randomly generated with a defined size and maximum value
        Random rand = new Random();
        this.nodes = new int[size];
        for (int i = 0; i < size; i++) {
            this.nodes[i] = rand.nextInt(valueBound * 2) - valueBound;
        }
        this.neighbourCheck = new int[this.getLength()][this.getLength()];
        for (int i = 0; i < size; i++) {
            Arrays.fill(this.neighbourCheck[i], -1);
        }
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (rand.nextBoolean()) {
                    this.neighbourCheck[i][j] = 1;
                    this.neighbourCheck[j][i] = 1;
                }
            }
        }
        this.neighbourTab = createNeighbour(this.neighbourCheck);
    }

    protected int[][] createNeighbour(int[][] neighbourChk) {
        int[][] res = new int[this.getLength()][this.getLength()];
        int cursor;
        for (int i = 0; i < res.length; i++) {
            Arrays.fill(res[i], -1);
            cursor = 0;
            for (int j = 0; j < res.length; j++) {
                if (neighbourChk[i][j] != -1)
                    res[i][cursor++] = j;
            }
            res[i] = Arrays.copyOf(res[i], cursor);
        }
        return res;
    }

    protected int[][] createNeighbourCheck(Paire[] vert) {// no side effect
        int[][] neighbourChk = new int[this.getLength()][this.getLength()];
        for (int i = 0; i < this.getLength(); i++) {
            Arrays.fill(neighbourChk[i], -1);
        }
        for (Paire p : vert) {
            neighbourChk[p.getA()][p.getB()] = 1;
            neighbourChk[p.getB()][p.getA()] = 1;
        }
        return neighbourChk;
    }

    public int[] genRoute() {// Ici On génère aléatoirement un nouveau chemin
        Random rand = new Random();
        int[] list = new int[rand.nextInt(this.getLength() * SCALE) + 1];
        list[0] = rand.nextInt(this.getLength());
        for (int i = 1; i < list.length; i++) {
            int[] node = Arrays.copyOf(this.getNeighbourTab(list[i - 1]), this.getNeighbourTab(list[i - 1]).length);
            list[i] = node[rand.nextInt(node.length)];
        }
        return list;
    }

    public int[] genRoute(int firstNode, int lengthMax) {
        Random rand = new Random();
        int[] list = new int[rand.nextInt(lengthMax) + 1];
        list[0] = firstNode;
        for (int i = 1; i < list.length; i++) {
            int[] node = Arrays.copyOf(this.getNeighbourTab(list[i - 1]), this.getNeighbourTab(list[i - 1]).length);
            list[i] = node[rand.nextInt(node.length)];
        }
        return list;
    }

    public int[] genRouteRdDist(int[] list) {
        float mouvement = this.getLength() / RANGECST;
        int[] route;
        do {
            route = genRoute();
        } while (Graph.distanceBetweenRoutes(route, list) > mouvement);
        return route;
    }

    public int[] genRoute2Opt(int[] route) {
        // we choose randomly within the possibles solutions, with a chance of
        // adding or removing first or last node
        // if no solution random gen is used
        int[] optRoute;
        int[] newRoute = null;// if null stays it is wrong
        LinkedList<int[]> allowedRoutes = new LinkedList<>();
        Random rand = new Random();
        for (int i = 0; i < route.length - 1; i++) {
            for (int j = i + 1; j < route.length; j++) {
                optRoute = swap2Opt(route, i, j);
                if (isAllowed(optRoute)) {
                    int[] node;
                    switch (rand.nextInt(5)) {
                    case 0:
                        newRoute = optRoute;
                        break;
                    case 1:
                        newRoute = Arrays.copyOf(optRoute, optRoute.length + 1);
                        node = this.getNeighbourTab(optRoute[optRoute.length - 1]);
                        newRoute[newRoute.length - 1] = node[rand.nextInt(node.length)];
                        break;
                    case 2:
                        newRoute = new int[optRoute.length + 1];
                        for (int k = 0; k < optRoute.length; k++) {
                            newRoute[k + 1] = optRoute[k];
                        }
                        node = this.getNeighbourTab(optRoute[0]);
                        newRoute[0] = node[rand.nextInt(node.length)];
                        break;
                    case 3:
                        newRoute = Arrays.copyOf(optRoute, optRoute.length - 1);
                        break;
                    case 4:
                        newRoute = new int[optRoute.length - 1];
                        for (int k = 0; k < optRoute.length - 1; k++) {
                            newRoute[k] = optRoute[k + 1];
                        }
                        break;
                    default:
                        break;
                    }
                    allowedRoutes.add(newRoute);
                }
            }
        }
        if (allowedRoutes.isEmpty()) {
            return genRoute(route[0], route.length);
        } else
            return allowedRoutes.get(rand.nextInt(allowedRoutes.size()));
    }

    public int[] genMixtRoute(int[] route) {
        int[] optRoute;
        LinkedList<int[]> allowedRoutes = new LinkedList<>();
        Random rand = new Random();
        for (int i = 0; i < route.length - 1; i++) {
            for (int j = i + 1; j < route.length; j++) {
                optRoute = swap2Opt(route, i, j);
                if (isAllowed(optRoute)) {
                    allowedRoutes.add(optRoute);
                }
            }
        }
        if (allowedRoutes.isEmpty()) {
            return genBestSubRoute(route);
        } else
            return allowedRoutes.get(rand.nextInt(allowedRoutes.size()));
    }

    public int[] genMixt2Route(int[] route) {
        // we choose randomly within the possibles solutions, with a chance of
        // adding or removing first or last node
        // if no solution random gen is used
        int[] optRoute;
        int[] newRoute = null;// if null stays it is wrong
        LinkedList<int[]> allowedRoutes = new LinkedList<>();
        Random rand = new Random();
        for (int i = 0; i < route.length - 1; i++) {
            for (int j = i + 1; j < route.length; j++) {
                optRoute = swap2Opt(route, i, j);
                if (isAllowed(optRoute)) {
                    int[] node;
                    switch (rand.nextInt(5)) {
                    case 0:
                        newRoute = optRoute;
                        break;
                    case 1:
                        newRoute = Arrays.copyOf(optRoute, optRoute.length + 1);
                        node = this.getNeighbourTab(optRoute[optRoute.length - 1]);
                        newRoute[newRoute.length - 1] = node[rand.nextInt(node.length)];
                        break;
                    case 2:
                        newRoute = new int[optRoute.length + 1];
                        for (int k = 0; k < optRoute.length; k++) {
                            newRoute[k + 1] = optRoute[k];
                        }
                        node = this.getNeighbourTab(optRoute[0]);
                        newRoute[0] = node[rand.nextInt(node.length)];
                        break;
                    case 3:
                        newRoute = Arrays.copyOf(optRoute, optRoute.length - 1);
                        break;
                    case 4:
                        newRoute = new int[optRoute.length - 1];
                        for (int k = 0; k < optRoute.length - 1; k++) {
                            newRoute[k] = optRoute[k + 1];
                        }
                        break;
                    default:
                        break;
                    }
                    allowedRoutes.add(newRoute);
                }
            }
        }
        if (allowedRoutes.isEmpty()) {
            return genBestSubRoute(route);
        } else
            return allowedRoutes.get(rand.nextInt(allowedRoutes.size()));
    }

    public int[] genRoute2OptGreed(int[] route) {
        // we choose the best solution within the 2opt swaps
        int[] optRoute;
        int[] bestRoute = route;
        for (int i = 0; i < route.length - 1; i++) {
            for (int j = i + 1; j < route.length; j++) {
                optRoute = swap2Opt(route, i, j);
                if (isAllowed(optRoute) && this.evaluate(optRoute) > this.evaluate(bestRoute)) {
                    bestRoute = optRoute;
                }
            }
        }
        return bestRoute;
    }

    public int[] genBestSubRoute(int[] route) {
        int[] subRoute = getBestSubRoute(route);
        int totalLength = subRoute.length;
        Random rand = new Random();
        if (totalLength == this.getLength())
            return route;
        int maxSize = rand.nextInt(this.getLength() - totalLength) + totalLength + 1;
        int[] beforeRoute = genRoute(subRoute[0], maxSize - totalLength);
        totalLength += beforeRoute.length - 1;
        int[] afterRoute = genRoute(subRoute[subRoute.length - 1], maxSize - totalLength);
        totalLength += afterRoute.length - 1;
        int[] newRoute = new int[totalLength];
        for (int i = 0; i < beforeRoute.length - 1; i++) {
            newRoute[i] = beforeRoute[beforeRoute.length - i - 1];
        }
        for (int i = 0; i < subRoute.length; i++) {
            newRoute[beforeRoute.length - 1 + i] = subRoute[i];
        }
        for (int i = 1; i < afterRoute.length; i++) {
            newRoute[beforeRoute.length + subRoute.length - 2 + i] = afterRoute[i];
        }
        return newRoute;
    }

    protected int[] getBestSubRoute(int[] route) {
        // simply evaluate every subroute and return the one with the best value
        // complexity route.length²
        int[] bestRoute = route;
        int bestScore = evaluate(bestRoute), newScore;
        HashSet<Integer> map = new HashSet<Integer>(); // we must only add nodes score when they are not already in the
                                                       // route
        for (int i = 0; i < route.length - 1; i++) {
            newScore = this.getValue(route[i]);
            map.clear();
            for (int j = i + 1; j < route.length; j++) {
                if (map.add(route[j]))
                    newScore += this.getValue(route[j]);
                if (newScore > bestScore) {
                    bestRoute = Arrays.copyOfRange(route, i, j + 1);
                    bestScore = newScore;
                }
            }
        }
        return bestRoute;
    }

    protected boolean isAllowed(int[] route) {
        for (int i = 0; i < route.length - 1; i++) {
            if (!this.isAccessible(route[i], route[i + 1]))
                return false;
        }
        return true;
    }

    static protected int[] swap2Opt(int[] route, int begin, int end) {
        int[] swapList = new int[route.length];
        for (int i = 0; i < route.length; i++) {
            if (i < begin)
                swapList[i] = route[i];
            else if (i > end)
                swapList[i] = route[i];
            else
                swapList[i] = route[end + begin - i];
        }
        return swapList;
    }

    static protected int distanceBetweenRoutes(int[] l1, int[] l2) {
        // levenshtein distance between two routes
        int[][] tab = new int[l1.length][2];
        int cost = 0;
        for (int i = 0; i < l1.length; i++) {
            tab[i] = new int[] { i, 0 };
        }
        for (int j = 1; j < l2.length; j++) {
            tab[0][j % 2] = j;
            for (int i = 1; i < l1.length; i++) {
                if (l1[i] == l2[j])
                    cost = 0;
                else
                    cost = 1;
                tab[i][j % 2] = Math.min(tab[i - 1][j % 2] + 1,
                        Math.min(tab[i][(j + 1) % 2] + 1, tab[i - 1][(j + 1) % 2] + cost));
            }
        }
        return tab[l1.length - 1][(l2.length - 1) % 2];
    }

    public int evaluate(int[] l) {// sum of the values of the nodes visited once
        int s = 0;
        HashSet<Integer> map = new HashSet<Integer>();
        for (int elmt : l) {
            if (map.add(elmt))
                s += this.getValue(elmt);
        }
        return s;
    }

    public boolean isAccessible(int x, int y) {// check connection between x and y, graph can be oriented
        return neighbourCheck[x][y] != -1;
    }

    public int[] getNeighbourTab(int node) {
        return neighbourTab[node];
    }

    public int getLength() {
        return nodes.length;
    }

    public int getValue(int index) {
        return nodes[index];
    }

    public int[] getNodes() {
        return nodes;
    }

    public void setBestScore(int a) {
        bestScore = a;
    }

    public int getBestScore() {
        return bestScore;
    }

    public String toString() {
        String result = "";
        for (int i = 0; i < this.nodes.length; i++) {
            String array = "[";
            for (int elmt : neighbourTab[i]) {
                array += elmt + " ,";
            }
            array += "]";
            result = result + getValue(i) + "," + array + "\n";
        }
        return result;
    }
}

File GraphWeightedArcs

package graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class GraphWeightedArcs extends Graph {
    private static final long serialVersionUID = 1L;

    // neighbourCheck is the matrice of costs ie mat[i][j] is the cost to get from i
    // to j
    public GraphWeightedArcs(int size, int valueBound, int arcBound) {
        Random rand = new Random();
        this.nodes = new int[size];
        for (int i = 0; i < size; i++) {
            this.nodes[i] = rand.nextInt(valueBound);
        }
        this.neighbourCheck = new int[this.getLength()][this.getLength()];
        for (int i = 0; i < size; i++) {
            Arrays.fill(this.neighbourCheck[i], -1);
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (rand.nextBoolean() && i != j) {
                    this.neighbourCheck[i][j] = rand.nextInt(arcBound);
                    this.neighbourCheck[j][i] = this.neighbourCheck[i][j];
                }
            }
        }
        this.neighbourTab = createNeighbour(this.neighbourCheck);
    }

    public int getArcWeight(int i, int j) {
        return neighbourCheck[i][j];
    }

    @Override
    public int evaluate(int[] l) {
        // sum of the values of the nodes visited once
        // and substract every time you go through an arc
        int s = 0;
        HashSet<Integer> map = new HashSet<Integer>();
        for (int i = 0; i < l.length; ++i) {
            int elmt = l[i];
            if (map.add(elmt))
                s += this.getValue(elmt);
            if (i != 0)
                s -= neighbourCheck[l[i - 1]][l[i]];
        }
        return s;
    }

    /**
     * Must be overidden because the way we calculate the score is integrated in
     * this method. Simply evaluate every subroute and return the one with the best
     * value complexity route.length²
     * @param route Array of which we extract the subroute
     */
    @Override
    protected int[] getBestSubRoute(int[] route) {
        int[] bestRoute = route;
        int bestScore = evaluate(bestRoute), newScore;
        HashSet<Integer> map = new HashSet<Integer>();
        for (int i = 0; i < route.length - 1; i++) {
            newScore = this.getValue(route[i]);
            map.clear();
            for (int j = i + 1; j < route.length; j++) {
                if (map.add(route[j]))
                    newScore += this.getValue(route[j]);
                newScore -= neighbourCheck[route[j - 1]][route[j]];
                if (newScore > bestScore) {
                    bestRoute = Arrays.copyOfRange(route, i, j + 1);
                    bestScore = newScore;
                }
            }
        }
        return bestRoute;
    }

    public String toString() {
        String result = "";
        for (int i = 0; i < this.nodes.length; i++) {
            String array = "[";
            for (int elmt : neighbourTab[i]) {
                array += elmt + "(" + neighbourCheck[i][elmt] + ")" + ", ";
            }
            array = array.substring(0, array.length() - 2) + "]";
            result = result + getValue(i) + "," + array + "\n";
        }
        return result;
    }
}

File Paire
package graph;

    public class Paire {
        private int a;
        private int b;

        public Paire(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public boolean equals(Paire p) {// equality is when both elements are in both pairs
            return (this.a == p.getA() && this.b == p.getB()) || (this.a == p.getB() && this.b == p.getA());
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }
}

File Main
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

    public static void testSituationReelle() {
        int n = 500;
        GraphWeightedArcs graph = new GraphWeightedArcs(n, 100, 100);
        Recuit r = Recuit.getInstance();
        double t = System.currentTimeMillis();
        r.solveGen(1000, 0.95, graph, RecuitMethod.MIXT2, 500);
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

File Recuit
package standard;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import graph.*;

public class Recuit {// one method to solve the problem
    private double objFunctValue;
    private static Recuit singleInstance = null;

    public static Recuit getInstance() {
        if (singleInstance == null)
            singleInstance = new Recuit();
        return singleInstance;
    }

    public int[] solveGen(double tempInit, double speed, Graph g, RecuitMethod method, int retryCap) {
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        int retry = 0;
        while (retry < retryCap) {
            double r = rand.nextDouble();
            int[] newSol;
            if (i == 100 || temp < Math.pow(1, -1) || actualSol.length == g.getLength()) {
                ++retry;
                i = 0;
                newSol = g.genRoute();
                temp = tempInit;
            } else {
                Function<int[], int[]> generation;
                switch (method) {
                case OPT:
                    generation = g::genRoute2Opt;
                    break;
                case BESTSUBROUTE:
                    generation = g::genBestSubRoute;
                    break;
                case MIXT:
                    generation = g::genMixtRoute;
                    break;
                case MIXT2:
                    generation = g::genMixt2Route;
                    break;
                default:
                    generation = g::genMixtRoute;
                    break;
                }
                newSol = generation.apply(actualSol);
            }

            double newScore = g.evaluate(newSol);
            if (r < Math.exp((newScore - actualScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
                temp *= speed;
            } else
                ++i;
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = Arrays.copyOf(newSol, newSol.length);
            }
        }
        objFunctValue = bestScore;
        return bestSol;
    }

    public int[] solveGen(Graph g, RecuitMethod method, int retryCap) {
        double tempInit = 100, speed = 0.95;
        Random rand = new Random();
        double temp = tempInit;
        int[] actualSol = g.genRoute();
        double actualScore = g.evaluate(actualSol);
        int[] bestSol;
        bestSol = Arrays.copyOf(actualSol, actualSol.length);
        double bestScore = new Double(actualScore);
        int i = 0;
        int retry = 0;
        while (retry < retryCap) {
            double r = rand.nextDouble();
            int[] newSol;
            if (i == 100 || temp < Math.pow(1, -1) || actualSol.length == g.getLength()) {
                ++retry;
                i = 0;
                newSol = g.genRoute();
                temp = tempInit;
            } else {
                Function<int[], int[]> generation;
                switch (method) {
                case OPT:
                    generation = g::genRoute2Opt;
                    break;
                case BESTSUBROUTE:
                    generation = g::genBestSubRoute;
                    break;
                case MIXT:
                    generation = g::genMixtRoute;
                    break;
                case MIXT2:
                    generation = g::genMixt2Route;
                    break;
                default:
                    generation = g::genMixtRoute;
                    break;
                }
                newSol = generation.apply(actualSol);
            }

            double newScore = g.evaluate(newSol);
            if (r < Math.exp((newScore - actualScore) / temp)) {
                actualSol = newSol;
                actualScore = newScore;
                temp *= speed;
            } else
                ++i;
            if (newScore > bestScore) {
                bestScore = newScore;
                bestSol = Arrays.copyOf(newSol, newSol.length);
            }
        }
        objFunctValue = bestScore;
        return bestSol;
    }

    public int[] solveRand(Graph g) {
        double tempInit = 100, speedRate = 0.95;
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

File RecuitMethod
package standard;

    public enum RecuitMethod {
        OPT, BESTSUBROUTE, MIXT, MIXT2;
}

File SimplexLib
package standard;

import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import graph.*;

public class SimplexLib {
    private int simplexValue;
    private Graph graph;

    public SimplexLib(Graph g) {
        this.graph = g;
        simplexValue = -1;
    }

    public int getValue() {
        return simplexValue;
    }

    public LPSolution solveNoArcs(int multiplier) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            // zi stands for does the node i was visited

            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1).setAllVariablesBoolean();
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j
                  // (there are vertices not allowed at all)
            }

            if (graph.getValue(i) > 0) {
                LPWizardConstraint maxZConstraint = lpw.addConstraint("maxz" + i, 0, "<=");
                maxZConstraint.plus("z" + i, -1).setAllVariablesBoolean();
                for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                    maxZConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
                }
            } else {
                for (int j = 0; j < maxSize; j++) {
                    lpw.addConstraint("maxz" + i + "," + j, 0, "<=").plus("z" + i).plus("x" + i + "," + j, -1);
                }
            }
            // depending of the sign of zi in the objective function the constraint is not
            // the same

        }
        for (int j = 0; j < maxSize - 1; j++) {
            // if the route is ended you can't add new points to the route
            // you can't go in 2 directions idest for j fixed only one xi,j is
            // equals to one
            LPWizardConstraint endConstraint = lpw.addConstraint("end" + j, 0, "<=");
            LPWizardConstraint routeConstraint = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                endConstraint.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1)
                        .setAllVariablesBoolean();
                routeConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }
        }
        lpw.setAllVariablesInteger();// PLNE
        LPSolution sol = lpw.solve();
        simplexValue = (int) sol.getObjectiveValue();
        return sol;
    }

    static public LPSolution solveNoArcs(int multiplier, Graph graph) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            // zi stands for does the node i was visited

            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1).setAllVariablesBoolean();
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j
                  // (there are vertices not allowed at all)
            }

            if (graph.getValue(i) > 0) {
                LPWizardConstraint maxZConstraint = lpw.addConstraint("maxz" + i, 0, "<=");
                maxZConstraint.plus("z" + i, -1).setAllVariablesBoolean();
                for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                    maxZConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
                }
            } else {
                for (int j = 0; j < maxSize; j++) {
                    lpw.addConstraint("maxz" + i + "," + j, 0, "<=").plus("z" + i).plus("x" + i + "," + j, -1);
                }
            }
            // depending of the sign of zi in the objective function the constraint is not
            // the same

        }
        for (int j = 0; j < maxSize - 1; j++) {
            // if the route is ended you can't add new points to the route
            // you can't go in 2 directions idest for j fixed only one xi,j is
            // equals to one
            LPWizardConstraint endConstraint = lpw.addConstraint("end" + j, 0, "<=");
            LPWizardConstraint routeConstraint = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                endConstraint.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1)
                        .setAllVariablesBoolean();
                routeConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }
        }
        lpw.setAllVariablesInteger();// PLNE
        return lpw.solve();
    }

    static public LPSolution solveWithArcs(int multiplier, GraphWeightedArcs graph) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            // zi stands for does the node i was visited
            for (int k = i + 1; k < graph.getLength(); k++) {
                if (graph.isAccessible(i, k))
                    lpw.plus("arc" + i + "," + k, -graph.getArcWeight(i, k));
            } // weight of arcs taken in account

            for (int k = i + 1; k < graph.getLength(); k++) {
                LPWizardConstraint arcCountConstraint = lpw.addConstraint("arc count" + i + "," + k, 0, ">=");
                arcCountConstraint.plus("arc" + i + "," + k, -1).setAllVariablesInteger();
                for (int j = 0; j < maxSize; j++) {
                    arcCountConstraint.plus("passage" + i + "," + k + "en" + j).setAllVariablesInteger();
                }
            } // on calcule le nombre de fois qu'on est passé sur un arc (somme des passages)
            for (int k = i + 1; k < graph.getLength(); k++) {
                for (int j = 0; j < maxSize; j++) {
                    LPWizardConstraint passageConstraint = lpw.addConstraint("pass" + i + "," + k + "en" + j, 1, ">=");
                    passageConstraint.plus("passage" + i + "," + k + "en" + j, -1).setAllVariablesBoolean();
                    passageConstraint.plus("x" + i + "," + j).plus("x" + k + "," + Integer.toString(j + 1));
                    passageConstraint.plus("x" + k + "," + j).plus("x" + i + "," + Integer.toString(j + 1));
                    // On prend de i vers k mais aussi de k vers i cela divise le nombre de variable
                    // par deux
                }
            } // on pose plein de variables qui permettent de savoir si on est passé sur une
              // arrête à l'étape j
            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1).setAllVariablesBoolean();
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j
                  // (there are vertices not allowed at all)
            }

            LPWizardConstraint maxZConstraint = lpw.addConstraint("maxz" + i, 0, "<=");
            maxZConstraint.plus("z" + i, -1).setAllVariablesBoolean();
            for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                maxZConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }

        }
        for (int j = 0; j < maxSize - 1; j++) {
            // if the route is ended you can't add new points to the route
            // you can't go in 2 directions id est for j fixed only one xi,j is
            // equals to one
            LPWizardConstraint endConstraint = lpw.addConstraint("end" + j, 0, "<=");
            LPWizardConstraint routeConstraint = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                endConstraint.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1)
                        .setAllVariablesBoolean();
                routeConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }
        }
        lpw.setAllVariablesInteger();// PLNE
        return lpw.solve();
    }

    static public String toString(LPSolution sol, Graph g) {
        String str = "Avec un score de " + sol.getObjectiveValue() + ", le trajet de la solution est :\n";
        for (int step = 0; step < g.getLength(); step++) {
            for (int i = 0; i < g.getLength(); i++) {
                if (sol.getBoolean("x" + i + "," + step))
                    str += i + "(" + g.getValue(i) + ")";
            }
        }
        return str;
    }
}