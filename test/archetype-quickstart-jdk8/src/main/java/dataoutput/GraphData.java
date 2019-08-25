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