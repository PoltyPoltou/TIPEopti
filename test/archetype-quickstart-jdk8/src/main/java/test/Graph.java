package test;

import java.util.Arrays;
import java.util.LinkedList;

public class Graph {
    // it is a static graph, can't be changed that much

    private int[] nodes;// Store the data of each nodes
    private Paire[] vertices;// Store the conections of the graph for construction only
    private int[][] neighbourCheck;// matrice to access availibilty of each vertices
    private LinkedList<LinkedList<Integer>> neighbour;// list the available
                                                      // nodes from one

    public Graph(int[] nodes, Paire[] vertices) {// warning complexity heavy
        this.nodes = nodes;
        this.vertices = vertices;
        neighbour = new LinkedList<LinkedList<Integer>>();
        neighbourCheck = new int[this.getLength()][this.getLength()];
        for (int i = 0; i < this.getLength(); i++) {
            Arrays.fill(neighbourCheck[i], -1);
        }
        for (Paire p : this.vertices) {
            neighbourCheck[p.getA()][p.getB()] = 1;
            neighbourCheck[p.getB()][p.getA()] = 1;
        }

        for (int i = 0; i < this.getLength(); i++) {
            neighbour.add(new LinkedList<Integer>());
            for (int j = 0; j < this.getLength(); j++) {
                if (neighbourCheck[i][j] == 1)
                    neighbour.get(i).add(j);
            }
        }
    }

    public Graph(int[] nodes, int[] paires) {// paires.length must be even !
        Paire[] vertices = new Paire[paires.length / 2];
        for (int i = 0; i < paires.length; i += 2) {
            vertices[i / 2] = new Paire(paires[i], paires[i + 1]);
        }

        this.nodes = nodes;
        this.vertices = vertices;
        neighbour = new LinkedList<LinkedList<Integer>>();
        neighbourCheck = new int[this.getLength()][this.getLength()];
        for (int i = 0; i < this.getLength(); i++) {
            Arrays.fill(neighbourCheck[i], -1);
        }
        for (Paire p : this.vertices) {
            neighbourCheck[p.getA()][p.getB()] = 1;
            neighbourCheck[p.getB()][p.getA()] = 1;
        }

        for (int i = 0; i < this.getLength(); i++) {
            neighbour.add(new LinkedList<Integer>());
            for (int j = 0; j < this.getLength(); j++) {
                if (neighbourCheck[i][j] == 1)
                    neighbour.get(i).add(j);
            }
        }
    }

    public boolean isAccessible(int x, int y) {// check connection between x and y, graph can be oriented
        return neighbourCheck[x][y] != -1;
    }

    public LinkedList<Integer> getNeighbour(int node) {
        return neighbour.get(node);
    }

    public int getLength() {
        return nodes.length;
    }

    public int getValue(int index) {
        return nodes[index];
    }
}