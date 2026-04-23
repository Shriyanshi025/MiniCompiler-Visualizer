import java.util.*;

public class ExecutionStep {
    public int step;
    public String action;
    public Map<String, Double> variables;

    public ExecutionStep(int step, String action, Map<String, Double> variables) {
        this.step = step;
        this.action = action;
        this.variables = new HashMap<>(variables); // Deep copy of values
    }
}
