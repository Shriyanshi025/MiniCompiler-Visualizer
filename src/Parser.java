import java.util.*;

public class Parser {

    public static void checkSyntax(List<String> tokens) {

        for (int i = 0; i < tokens.size(); i++) {

            if (tokens.get(i).equals("int") || tokens.get(i).equals("float")) {

                if (i + 3 >= tokens.size()) {
                    System.out.println("Syntax Error");
                    return;
                }

                if (!tokens.get(i + 2).equals("=")) {
                    System.out.println("Syntax Error: Missing =");
                    return;
                }
            }
        }
    }
}
