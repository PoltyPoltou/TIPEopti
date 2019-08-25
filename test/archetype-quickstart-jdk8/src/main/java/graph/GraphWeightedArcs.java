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