import java.util.*;

public class Semantic {

    private static Map<String, Symbol> symbolTable = new HashMap<>();

    public static Map<String, Symbol> getSymbolTable() {
        return symbolTable;
    }

    public static void reset() {
        symbolTable.clear();
    }

    public static void checkSemantics(List<String> tokens) {
        // IMPORTANT: Clear symbolTable ONLY at start of checkSemantics()
        symbolTable.clear();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            // On declaration (int/float)
            if (token.equals("int") || token.equals("float")) {
                String type = token;
                String name = tokens.get(i + 1);
                symbolTable.put(name, new Symbol(name, type, 0.0));
            }

            // On assignment check
            if (token.equals("=") && i > 0) {
                String var = tokens.get(i - 1);

                if (!symbolTable.containsKey(var)) {
                    System.out.println("Semantic Error: " + var + " not declared");
                    return;
                }
            }
        }
    }
}