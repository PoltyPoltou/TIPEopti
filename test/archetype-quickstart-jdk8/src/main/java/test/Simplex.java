package test;

import java.util.Arrays;
import java.util.LinkedList;

public class Simplex {
    Graph graph;
    FonctObj fun;

    public Simplex(Graph g, FonctObj f) {
        this.graph = g;
        this.fun = f;
    }

    // there are conditions on route, route[i] and route[i+1] must be connected

    public int[] solve() {
        int[] route = new int[graph.getLength() * graph.getLength()];
        int[] coefObj = graph.getNodes();
        linkListIntoArray(route, fun.genRoute());
        int[] resVect = getVector(route);
        return new int[0];
    }

    private int[] linkListIntoArray(int[] route, LinkedList<Integer> list) {
        Arrays.fill(route, 0);
        for (int i = 0; i < list.size(); i++) {
            route[i] = list.get(i);
        }
        return route;
    }

    private int[][] getArray(int[] route) {
        int[][] result = new int[route.length][graph.getLength()];
        for (int i = 0; i < result.length; i++) {
            Arrays.fill(result[i], 0);
        }
        for (int i = 0; i < route.length; i++) {
            result[i][route[i]] = 1;
        }
        return result;
    }

    private int[] getVector(int[] route) {
        int[] result = new int[graph.getLength()];
        Arrays.fill(result, 0);
        for (int i = 0; i < route.length; i++) {
            result[route[i]] = 1;
        }
        return result;
    }
}