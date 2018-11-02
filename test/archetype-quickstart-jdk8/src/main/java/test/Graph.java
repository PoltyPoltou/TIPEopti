package test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class Graph {
    // it is a static graph, can't be changed that much

    private int[] nodes;// Store the data of each nodes
    private int[][] neighbourCheck;// matrice to access availibilty of each vertices
    private int[][] neighbourTab;// list the available nodes from one
    private final int RANGECST = 3;// how far can we look to get a new
    private final int SCALE = 1;// how big can be the routes

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
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (rand.nextBoolean() && i != j) {
                    this.neighbourCheck[i][j] = 1;
                    this.neighbourCheck[j][i] = 1;
                }
            }
        }
        this.neighbourTab = createNeighbour(this.neighbourCheck);
    }

    private int[][] createNeighbour(int[][] neighbourChk) {
        int[][] res = new int[this.getLength()][this.getLength()];
        int cursor;
        for (int i = 0; i < res.length; i++) {
            Arrays.fill(res[i], -1);
            cursor = 0;
            for (int j = 0; j < res.length; j++) {
                if (neighbourChk[i][j] == 1)
                    res[i][cursor++] = j;
            }
            res[i] = Arrays.copyOf(res[i], cursor);
        }
        return res;
    }

    private int[][] createNeighbourCheck(Paire[] vert) {// no side effect
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

    public int[] genRoute() {// Ici On génère à l'aveugle un nouveau chemin
        Random rand = new Random();
        int[] list = new int[rand.nextInt(this.getLength() * SCALE) + 1];
        list[0] = rand.nextInt(this.getLength());
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
        } while (Graph.distanceBetweenRoutes(route, list) > mouvement);// 3 seems to be very efficient
        return route;
    }

    public int[] genRoute2Opt(int[] route) {
        // we choose randomly within the possibles solutions, with a chance of
        // adding or removing first or last node
        // if no solution random gen is used
        int[] optRoute = Arrays.copyOf(route, route.length);
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
        if (allowedRoutes.isEmpty())
            return genRouteRdDist(route);
        else {
            return allowedRoutes.get(rand.nextInt(allowedRoutes.size()));
        }
    }

    private boolean isAllowed(int[] route) {
        for (int i = 0; i < route.length - 1; i++) {
            if (!this.isAccessible(route[i], route[i + 1]))
                return false;
        }
        return true;
    }

    private int[] swap2Opt(int[] route, int begin, int end) {
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

    static private int distanceBetweenRoutes(int[] l1, int[] l2) {
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
/*
 * not working very well :/ public LinkedList<Integer>
 * genRouteNear(LinkedList<Integer> list, double ratioExp) {
 * 
 * LinkedList<Integer> trip = new LinkedList<Integer>(list);
 * ArrayList<LinkedList<Integer>> detourOk = new ArrayList<>(); Random rand =
 * new Random(); int r; if (list.size() == 1) { trip = new LinkedList<>();
 * trip.add(rand.nextInt(graph.getLength())); return trip; } do { r =
 * rand.nextInt(trip.size()); if (r == 0 || r == trip.size() - 1) {
 * LinkedList<Integer> temp; switch (r) { case 0: temp =
 * graph.getNeighbour(trip.get(r + 1)); trip.remove(r); trip.add(r,
 * temp.get(rand.nextInt(temp.size()))); break;
 * 
 * default: temp = graph.getNeighbour(trip.get(r - 1)); trip.remove(r);
 * trip.add(r, temp.get(rand.nextInt(temp.size()))); break; } return trip; }
 * else if (trip.get(r + 1) == trip.get(r - 1)) { trip.remove(r);
 * trip.remove(r); } else if (!graph.isAccessible(trip.get(r + 1), trip.get(r -
 * 1))) { // we take the node before the one // choosen randomly and try to go
 * the // next one of the route trip.remove(r); LinkedList<Integer> start =
 * graph.getNeighbour(trip.get(r - 1)); ArrayDeque<LinkedList<Integer>>
 * listeDetour = new ArrayDeque<LinkedList<Integer>>(); detourOk = new
 * ArrayList<>(); for (int i = 0; i < start.size(); i++) { LinkedList<Integer> l
 * = new LinkedList<>(); l.add(start.get(i)); listeDetour.add(l); } int i = 0;
 * // On explore dans toutes les directions le graphe // pour trouver un nouveau
 * chemin qu'on ajoute à la liste detourOk while (!listeDetour.isEmpty() && i <
 * ratioExp * graph.getLength()) { LinkedList<Integer> elmt =
 * listeDetour.poll(); LinkedList<Integer> neighbour = new
 * LinkedList<>(graph.getNeighbour(elmt.getLast())); if (elmt.size() != 1)
 * neighbour.remove(elmt.get(elmt.size() - 2)); for (int nb : neighbour) {
 * LinkedList<Integer> temp = new LinkedList<>(elmt); if (nb == trip.get(r))
 * detourOk.add(temp); else { temp.add(nb); listeDetour.add(temp); } } ++i; } }
 * } while (detourOk.size() == 0); trip.addAll(r,
 * detourOk.get(rand.nextInt(detourOk.size()))); return trip; }
 */