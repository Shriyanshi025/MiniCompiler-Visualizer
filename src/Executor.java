import java.util.*;

public class Executor {

    private static List<ExecutionStep> executionTrace = new ArrayList<>();
    private static int stepCounter = 0;

    public static List<ExecutionStep> getExecutionTrace() {
        return executionTrace;
    }

    private static void addTrace(String action, Map<String, Double> values) {
        executionTrace.add(
            new ExecutionStep(++stepCounter, action, new HashMap<>(values))
        );
    }

    public static void execute(List<String> tokens, Map<String, Double> values,
                                Map<String, String> types, boolean isMain) {

        if (isMain) {
            executionTrace.clear();
            stepCounter = 0;
        }

        for (int i = 0; i < tokens.size(); i++) {

            // ================= FOR LOOP =================
            if (tokens.get(i).equals("for")) {
                i++; // skip for
                List<String> init = new ArrayList<>();
                List<String> condition = new ArrayList<>();
                List<String> update = new ArrayList<>();

                i++; // skip (
                while (!tokens.get(i).equals(";")) {
                    init.add(tokens.get(i));
                    i++;
                }
                i++; // skip ;
                while (!tokens.get(i).equals(";")) {
                    condition.add(tokens.get(i));
                    i++;
                }
                i++; // skip ;
                while (!tokens.get(i).equals(")")) {
                    update.add(tokens.get(i));
                    i++;
                }
                i++; // skip )
                i++; // skip {
                List<String> block = new ArrayList<>();
                while (!tokens.get(i).equals("}")) {
                    block.add(tokens.get(i));
                    i++;
                }

                execute(init, values, types, false);
                int safe = 1000;
                while (safe-- > 0) {
                    if (!evaluateCondition(condition, values)) break;
                    execute(new ArrayList<>(block), values, types, false);
                    execute(update, values, types, false);
                }
                continue;
            }

            // ================= PRINT =================
            if (tokens.get(i).equals("print")) {
                i++;
                if (tokens.get(i).startsWith("\"")) {
                    StringBuilder msg = new StringBuilder();
                    while (i < tokens.size() && !tokens.get(i).endsWith("\"")) {
                        msg.append(tokens.get(i)).append(" ");
                        i++;
                    }
                    msg.append(tokens.get(i));
                    String finalMsg = msg.toString().replace("\"", "");
                    System.out.println(finalMsg);
                    addTrace("print \"" + finalMsg + "\"", values);
                } else {
                    String var = tokens.get(i);
                    double val = values.getOrDefault(var, 0.0);
                    if (types.getOrDefault(var, "int").equals("int")) {
                        System.out.println((int) val);
                    } else {
                        System.out.println(val);
                    }
                    addTrace("print " + var, values);
                    i++;
                }
                continue;
            }

            // ================= IF-ELSE =================
            if (tokens.get(i).equals("if")) {
                List<String> condition = new ArrayList<>();
                i++;
                while (!tokens.get(i).equals("{")) {
                    if (!tokens.get(i).equals("(") && !tokens.get(i).equals(")"))
                        condition.add(tokens.get(i));
                    i++;
                }
                boolean result = evaluateCondition(condition, values);
                i++; // skip {
                List<String> ifBlock = new ArrayList<>();
                while (!tokens.get(i).equals("}")) {
                    ifBlock.add(tokens.get(i));
                    i++;
                }
                i++; // skip }
                List<String> elseBlock = new ArrayList<>();
                if (i < tokens.size() && tokens.get(i).equals("else")) {
                    i += 2; // skip else {
                    while (!tokens.get(i).equals("}")) {
                        elseBlock.add(tokens.get(i));
                        i++;
                    }
                }
                if (result) {
                    execute(ifBlock, values, types, false);
                } else {
                    execute(elseBlock, values, types, false);
                }
                continue;
            }

            // ================= WHILE LOOP =================
            if (tokens.get(i).equals("while")) {
                List<String> condition = new ArrayList<>();
                i++;
                while (!tokens.get(i).equals("{")) {
                    if (!tokens.get(i).equals("(") && !tokens.get(i).equals(")"))
                        condition.add(tokens.get(i));
                    i++;
                }
                i++; // skip {
                List<String> block = new ArrayList<>();
                while (!tokens.get(i).equals("}")) {
                    block.add(tokens.get(i));
                    i++;
                }
                int safe = 1000;
                while (safe-- > 0) {
                    boolean result = evaluateCondition(condition, values);
                    if (!result) break;
                    execute(new ArrayList<>(block), values, types, false);
                }
                continue;
            }

            // ================= DECLARATION =================
            if (tokens.get(i).equals("int") || tokens.get(i).equals("float")) {
                String type = tokens.get(i);
                String var = tokens.get(i + 1);
                types.put(var, type);
                double value = getValue(tokens.get(i + 3), values);
                values.put(var, value);
                addTrace("declare " + var, values);
            }

            // ================= INCREMENT (++) =================
            if (i + 1 < tokens.size() && tokens.get(i + 1).equals("+") &&
                    i + 2 < tokens.size() && tokens.get(i + 2).equals("+")) {
                String var = tokens.get(i);
                double value = values.getOrDefault(var, 0.0);
                value = value + 1;
                values.put(var, value);
                addTrace("update " + var, values);
                i += 2;
                continue;
            }

            // ================= ASSIGNMENT =================
            if (tokens.get(i).equals("=") && i > 0) {
                String var = tokens.get(i - 1);
                List<String> expr = new ArrayList<>();
                int j = i + 1;
                while (j < tokens.size() && !tokens.get(j).equals(";")) {
                    expr.add(tokens.get(j));
                    j++;
                }
                double result = evaluateExpression(expr, values);
                values.put(var, result);
                addTrace("update " + var, values);
            }
        }
    }

    private static boolean evaluateCondition(List<String> cond, Map<String, Double> memory) {
        double a = getValue(cond.get(0), memory);
        String op = cond.get(1);
        double b = getValue(cond.get(2), memory);
        switch (op) {
            case ">": return a > b;
            case "<": return a < b;
            case ">=": return a >= b;
            case "<=": return a <= b;
            case "==": return a == b;
            case "!=": return a != b;
        }
        return false;
    }

    private static double evaluateExpression(List<String> expr, Map<String, Double> memory) {
        Stack<Double> values = new Stack<>();
        Stack<String> ops = new Stack<>();
        for (int i = 0; i < expr.size(); i++) {
            String token = expr.get(i);
            if (token.matches("\\d+(\\.\\d+)?") || Character.isLetter(token.charAt(0))) {
                values.push(getValue(token, memory));
            } else if (token.equals("(")) {
                ops.push(token);
            } else if (token.equals(")")) {
                while (!ops.peek().equals("(")) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop();
            } else {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(token);
            }
        }
        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }
        return values.pop();
    }

    private static int precedence(String op) {
        if (op.equals("+") || op.equals("-")) return 1;
        if (op.equals("*") || op.equals("/") || op.equals("%")) return 2;
        return 0;
    }

    private static double applyOp(String op, double b, double a) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            case "%": return a % b;
            case ">": return (a > b) ? 1 : 0;
            case "<": return (a < b) ? 1 : 0;
            case ">=": return (a >= b) ? 1 : 0;
            case "<=": return (a <= b) ? 1 : 0;
            case "==": return (a == b) ? 1 : 0;
            case "!=": return (a != b) ? 1 : 0;
            case "&&": return (a != 0 && b != 0) ? 1 : 0;
            case "||": return (a != 0 || b != 0) ? 1 : 0;
        }
        return 0;
    }

    private static double getValue(String token, Map<String, Double> memory) {
        if (token.matches("\\d+(\\.\\d+)?")) {
            return Double.parseDouble(token);
        } else {
            return memory.getOrDefault(token, 0.0);
        }
    }
}