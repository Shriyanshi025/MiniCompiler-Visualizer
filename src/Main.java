

import java.util.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("Running Compiler...\n");

        List<String> tokens = Lexer.runLexer();

        Parser.checkSyntax(tokens);
        Semantic.checkSemantics(tokens);

//        List<String> intermediate = Intermediate.generate(tokens);
//        List<String> optimized = Optimizer.optimize(intermediate);

        Map<String, Double> values = new HashMap<>();
        Map<String, String> types = new HashMap<>();

        Executor.execute(tokens, values, types,true);
    }
}