package test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class FonctObj {// it is the function utility based on a graph
    Graph graph;

    public FonctObj(Graph g) {
        this.graph = g;
    }

    public LinkedList<Integer> genJourney() {
        Random rand = new Random();
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.add(rand.nextInt(graph.getLength()));
        int len = rand.nextInt(graph.getLength());// on choisit la longueur du chemin

        for (int i = 1; i < len; i++) {
            LinkedList<Integer> node = new LinkedList<Integer>(graph.getNeighbour(list.getLast()));
            list.add(node.get(rand.nextInt(node.size())));
        }
        return list;
    }

    public int evaluate(int[] l) {// for now it just add the nodes' value of the journey
        int s = 0;
        HashSet<Integer> map = new HashSet<Integer>();
        for (int elmt : l) {
            if (map.add(elmt))
                s += graph.getValue(elmt);
        }
        return s;
    }

    public int evaluate(LinkedList<Integer> l) {
        int s = 0;
        HashSet<Integer> map = new HashSet<Integer>();
        for (int elmt : l) {
            if (map.add(elmt))
                s += graph.getValue(elmt);
        }
        return s;
    }
}