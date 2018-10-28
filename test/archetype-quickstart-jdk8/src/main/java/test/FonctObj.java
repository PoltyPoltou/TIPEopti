package test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class FonctObj {// it is the function utility based on a graph
    Graph graph;

    public FonctObj(Graph g) {
        this.graph = g;
    }

    public LinkedList<Integer> genRoute() {// Ici On génère à l'aveugle un nouveau chemin
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

    public LinkedList<Integer> genRouteNear(LinkedList<Integer> list, double ratioExp) {
        LinkedList<Integer> trip = new LinkedList<Integer>(list);
        ArrayList<LinkedList<Integer>> detourOk = new ArrayList<>();
        Random rand = new Random();
        int r;
        do {
            r = rand.nextInt(trip.size());
            if (r == 0 || r == trip.size() - 1) {
                LinkedList<Integer> temp = graph.getNeighbour(trip.get(r));
                trip.remove(r);
                trip.add(r, temp.get(rand.nextInt(temp.size())));
                return trip;
            }
            trip.remove(r);
            if (trip.get(r) == trip.get(r - 1))
                trip.remove(r);
            else if (!graph.isAccessible(trip.get(r), trip.get(r - 1))) {
                // we take the node before the one
                // choosen randomly and try to go the
                // next one of the route
                LinkedList<Integer> start = graph.getNeighbour(trip.get(r - 1));
                ArrayDeque<LinkedList<Integer>> listeDetour = new ArrayDeque<LinkedList<Integer>>();
                detourOk = new ArrayList<>();
                for (int i = 0; i < start.size(); i++) {
                    LinkedList<Integer> l = new LinkedList<>();
                    l.add(start.get(i));
                    listeDetour.add(l);
                }
                int i = 0;
                // On explore dans toutes les directions le graphe
                // pour trouver un nouveau chemin qu'on ajoute à la liste detourOk
                while (!listeDetour.isEmpty() && i < ratioExp * graph.getLength()) {
                    LinkedList<Integer> elmt = listeDetour.poll();
                    LinkedList<Integer> neighbour = new LinkedList<>(graph.getNeighbour(elmt.getLast()));
                    if (elmt.size() != 1)
                        neighbour.remove(elmt.get(elmt.size() - 2));
                    for (int nb : neighbour) {
                        LinkedList<Integer> temp = new LinkedList<>(elmt);
                        if (nb == trip.get(r))
                            detourOk.add(temp);
                        else {
                            temp.add(nb);
                            listeDetour.add(temp);
                        }
                    }
                    ++i;
                }
            }
        } while (detourOk.size() == 0);
        trip.addAll(r, detourOk.get(rand.nextInt(detourOk.size())));
        return trip;

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