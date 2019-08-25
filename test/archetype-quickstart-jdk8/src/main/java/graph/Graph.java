package graph;

import java.io.Serializable;
import java.lang.reflect.Array;
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
