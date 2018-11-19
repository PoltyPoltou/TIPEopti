package standard;

import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import graph.*;

public class SimplexLib {
    static public LPSolution solveNoArcs(int multiplier, Graph graph) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            lpw.plus("z" + i, graph.getValue(i));
            // zi stands for does the node i was visited

            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1);
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1)).setAllVariablesBoolean();
                } // setup for can you access i in j+1 depending of xi,j
                  // (there are vertices not allowed at all)
            }

            if (graph.getValue(i) > 0) {
                LPWizardConstraint maxZConstraint = lpw.addConstraint("maxz" + i, 0, "<=");
                maxZConstraint.plus("z" + i, -1);
                for (int j = 0; j < maxSize; j++) {// zi is the max for j of xi,j
                    maxZConstraint.plus("x" + i + "," + j);
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
                endConstraint.plus("x" + i + "," + j).plus("x" + i + "," + Integer.toString(j + 1), -1);
                routeConstraint.plus("x" + i + "," + j);
            }
        }
        return lpw.solve();
    }

    static public LPSolution solveWithArcs(int multiplier, GraphWeigthedArcs graph) {
        int maxSize = graph.getLength() * multiplier;
        LPWizard lpw = new LPWizard();

        lpw.setMinProblem(false);
        for (int i = 0; i < graph.getLength(); i++) {
            // zi stands for does the node i was visited
            lpw.plus("z" + i, graph.getValue(i));
            // weight of arcs taken in account
            for (int k = i + 1; k < graph.getLength(); k++) {
                if (graph.isAccessible(i, k))
                    lpw.plus("arc" + i + "," + k, -graph.getArcWeight(i, k));
            }
            // on calcule le nombre de fois qu'on est passé sur un arc
            for (int k = i + 1; k < graph.getLength(); k++) {
                LPWizardConstraint arcCountConstraint = lpw.addConstraint("arc count" + i + "," + k, maxSize - 1, ">=");
                arcCountConstraint.plus("arc" + i + "," + k, -1);
                for (int j = 0; j < maxSize - 1; j++) {
                    arcCountConstraint.plus("x" + i + "," + j).plus("x" + k + "," + Integer.toString(j + 1));
                    arcCountConstraint.plus("x" + k + "," + j).plus("x" + i + "," + Integer.toString(j + 1));
                }
                lpw.addConstraint("arc cap" + i + "," + k, 0, "<=").plus("arc" + i + "," + k);
            }
            for (int j = 0; j < graph.getLength(); j++) {
                lpw.setBoolean("x" + i + "," + j);
            } // PLNE

            // setup for can you access i in j+1 depending of xi,j
            // (there are vertices not allowed at all)
            for (int j = 1; j < maxSize; j++) {
                LPWizardConstraint accesConstraint = lpw.addConstraint("access" + i + "," + j, 0, "<=");
                accesConstraint.plus("x" + i + "," + j, -1);
                for (int k = 0; k < graph.getLength(); k++) {
                    if (graph.isAccessible(i, k))
                        accesConstraint.plus("x" + k + "," + Integer.toString(j - 1));
                }
            }
            // zi is the max for j of xi,j
            // and zi can't be superior to one
            LPWizardConstraint zDefinition = lpw.addConstraint("maxz" + i, 0, "<=");
            lpw.addConstraint("zCap" + i, 1, ">=").plus("z" + i);// on borne zi (zi<=1)
            zDefinition.plus("z" + i, -1);
            for (int j = 0; j < maxSize; j++) {
                zDefinition.plus("x" + i + "," + j);
            }

        }
        // you can only go one way
        for (int j = 0; j < maxSize - 1; j++) {
            LPWizardConstraint oneWayConstraint = lpw.addConstraint("one route" + j, 1, ">=");
            for (int i = 0; i < graph.getLength(); i++) {
                oneWayConstraint.plus("x" + i + "," + j);
            }
        }
        return lpw.solve();
    }

    static public String toString(LPSolution sol, Graph g) {
        String str = "Avec un score de " /* + sol.getObjectiveValue() */ + ", le trajet de la solution est :\n";
        for (int step = 0; step < g.getLength(); step++) {
            for (int i = 0; i < g.getLength(); i++) {
                if (sol.getBoolean("x" + i + "," + step))
                    str += i + "(" + g.getValue(i) + ")";
            }
        }
        return str;
    }
}

/*
 * for (int j = 0; j < graph.getLength(); j++) { if (!graph.isAccessible(i, j))
 * for (int k = 0; k < sizeFactor - 1; k++) { lpw.addConstraint("arc" + i + ","
 * + i, 1, ">=").plus("x" + i + "," + k) .plus("x" + j + "," +
 * Integer.toString(k + 1)); } }
 */