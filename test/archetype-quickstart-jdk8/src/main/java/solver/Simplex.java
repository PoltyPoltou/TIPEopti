package solver;

import java.util.HashMap;

public class Simplex{
    HashMap<String, Integer> variables;
    int varNb;
    int constraintsNb;
    HashMap<String, Integer> constraints;
    public Simplex(){
        this.varNb=0;
        this.constraintsNb= 0;
        this.variables = new HashMap<>();
        this.constraints = new HashMap<>();
    }
    public void addVariable(String name){
        variables.put(name,varNb++);
    }
    public void addConstraintLessThan(String name, int a){
        
    }
}