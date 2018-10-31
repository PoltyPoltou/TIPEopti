package test;

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
        // Graph g = new Graph(new int[] { 1, 3, -10, 5, 7, -2, 4, 5, -8, 3, -5, 2 },
        // new int[] { 0, 1, 1, 3, 1, 11, 2, 10, 9, 10, 8, 11, 10, 11, 10, 4, 4, 7, 11,
        // 6, 6, 5, 8, 9 });

        Graph g = new Graph(50, 10);
        FonctObj fun = new FonctObj(g);
        Recuit recuit = new Recuit(100);
        Simplex s = new Simplex(g, fun);
        System.out.println(s.solve(3).getObjectiveValue());
        Integer[] tab = recuit.solve(g, fun, 0.95f);
        System.out.println(recuit.getobjFunctValue());
    }
}
