package standard;

import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import graph.*;

public class SimplexLib {
    Graph graph;

    public SimplexLib(Graph g) {
        this.graph = g;
    }

    public LPSolution solveNoArcs(int multiplier) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            // zi stands for does the node i was visited

            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1).setAllVariablesBoolean();
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j
                  // (there are vertices not allowed at all)
            }

            if (graph.getValue(i) > 0) {
                LPWizardConstraint maxZConstraint = lpw.addConstraint("maxz" + i, 0, "<=");
                maxZConstraint.plus("z" + i, -1).setAllVariablesBoolean();
                for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                    maxZConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
                }
            } else {
                for (int j = 0; j < maxSize; j++) {
                    lpw.addConstraint("maxz" + i + "," + j, 0, "<=").plus("z" + i).plus("x" + i + "," + j, -1);
                }
            }
            // depending of the sign of zi in the objective function the constraint is not
            // the same

        }
        for (int j = 0; j < maxSize - 1; j++) {
            // if the route is ended you can't add new points to the route
            // you can't go in 2 directions idest for j fixed only one xi,j is
            // equals to one
            LPWizardConstraint endConstraint = lpw.addConstraint("end" + j, 0, "<=");
            LPWizardConstraint routeConstraint = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                endConstraint.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1)
                        .setAllVariablesBoolean();
                routeConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }
        }
        lpw.setAllVariablesInteger();// PLNE
        return lpw.solve();
    }

    public LPSolution solveWithArcs(int multiplier) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            // zi stands for does the node i was visited

            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1).setAllVariablesBoolean();
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j
                  // (there are vertices not allowed at all)
            }

            LPWizardConstraint maxZConstraint = lpw.addConstraint("maxz" + i, 0, "<=");
            maxZConstraint.plus("z" + i, -1).setAllVariablesBoolean();
            for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                maxZConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }

        }
        for (int j = 0; j < maxSize - 1; j++) {
            // if the route is ended you can't add new points to the route
            // you can't go in 2 directions idest for j fixed only one xi,j is
            // equals to one
            LPWizardConstraint endConstraint = lpw.addConstraint("end" + j, 0, "<=");
            LPWizardConstraint routeConstraint = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                endConstraint.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1)
                        .setAllVariablesBoolean();
                routeConstraint.plus("x" + i + "," + j).setAllVariablesBoolean();
            }
        }
        lpw.setAllVariablesInteger();// PLNE
        return lpw.solve();
    }

}

/*
 * for (int j = 0; j < graph.getLength(); j++) { if (!graph.isAccessible(i, j))
 * for (int k = 0; k < sizeFactor - 1; k++) { lpw.addConstraint("arc" + i + ","
 * + i, 1, ">=").plus("x" + i + "," + k) .plus("x" + j + "," +
 * Integer.toString(k + 1)); } }
 */