package test;

import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

public class Simplex {
    Graph graph;
    FonctObj fun;

    public Simplex(Graph g, FonctObj f) {
        this.graph = g;
        this.fun = f;
    }

    // there are conditions on route, route[i] and route[i+1] must be connected

    public LPSolution solve(int multiplier) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();
        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            ;// zi stands for does the node i was visited

            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint tmp = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                tmp.plus("x" + i + "," + j, -1).setAllVariablesBoolean();
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        tmp.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j (there are vertices
                  // not allowed at all)
            }
            LPWizardConstraint tmp = lpw.addConstraint("maxz" + i, 0, "<=");
            int signe = 1;
            if (graph.getValue(i) < 0)
                signe = -1;
            else
                signe = 1;
            // we must change the sign of the equation depending of the effect of zi on the
            // objective functions

            tmp.plus("z" + i, -1 * signe).setAllVariablesBoolean();
            for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                tmp.plus("x" + i + "," + j, signe).setAllVariablesBoolean();
            }
        }
        for (int j = 0; j < maxSize - 1; j++) {
            // if the route is ended you can't add new points to the route
            // you can't go in 2 directions idest for j fixed only one xi,j is
            // equals to one
            LPWizardConstraint tmp = lpw.addConstraint("end" + j, 0, "<=");
            LPWizardConstraint tmp2 = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                tmp.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1).setAllVariablesBoolean();
                tmp2.plus("x" + i + "," + j).setAllVariablesBoolean();
            }
        }
        lpw.setAllVariablesInteger();// PLNE
        System.out.println(lpw.getLP().convertToCPLEX().toString());
        return lpw.solve();
    }
}

/*
 * for (int j = 0; j < graph.getLength(); j++) { if (!graph.isAccessible(i, j))
 * for (int k = 0; k < sizeFactor - 1; k++) { lpw.addConstraint("arc" + i + ","
 * + i, 1, ">=").plus("x" + i + "," + k) .plus("x" + j + "," +
 * Integer.toString(k + 1)); } }
 */