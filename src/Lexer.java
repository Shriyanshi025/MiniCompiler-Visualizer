//import java.io.*;
//import java.util.*;
//
//public class Lexer {
//
//    public static List<String> runLexer() {
//        List<String> tokens = new ArrayList<>();
//
//        try {
//            File file = new File("data/test.txt");
//            Scanner sc = new Scanner(file);
//
//            while (sc.hasNextLine()) {
//                String line = sc.nextLine();
//
//                // Protect multi-character operators
//                line = line.replace("&&", " @AND@ ");
//                line = line.replace("||", " @OR@ ");
//                line = line.replace("==", " @EQ@ ");
//                line = line.replace("!=", " @NEQ@ ");
//                line = line.replace(">=", " @GE@ ");
//                line = line.replace("<=", " @LE@ ");
//
//                // Single operators
//                line = line.replace("=", " = ");
//                line = line.replace("+", " + ");
//                line = line.replace("-", " - ");
//                line = line.replace("*", " * ");
//                line = line.replace("/", " / ");
//                line = line.replace(">", " > ");
//                line = line.replace("<", " < ");
//                line = line.replace(";", " ; ");
//
//                // Restore
//                line = line.replace("@AND@", "&&");
//                line = line.replace("@OR@", "||");
//                line = line.replace("@EQ@", "==");
//                line = line.replace("@NEQ@", "!=");
//                line = line.replace("@GE@", ">=");
//                line = line.replace("@LE@", "<=");
//
//                String[] words = line.split("\\s+");
//
//                Set<String> ops = Set.of(
//                        "=", "+", "-", "*", "/", ">", "<",
//                        "==", "!=", ">=", "<=", "&&", "||"
//                );
//
//                for (String word : words) {
//
//                    word = word.replaceAll("[()]", "");
//
//                    if (word.isEmpty()) continue;
//
//                    tokens.add(word);
//
//                    if (word.equals("int") || word.equals("float") ||
//                            word.equals("if") || word.equals("else") || word.equals("print")){
//                        System.out.println("KEYWORD: " + word);
//                    }
//                    else if (word.matches("\\d+(\\.\\d+)?")) {
//                        System.out.println("NUMBER: " + word);
//                    }
//                    else if (ops.contains(word)) {
//                        System.out.println("OPERATOR: " + word);
//                    }
//                    else if (word.equals(";")) {
//                        // ignore printing ;
//                    }
//                    else {
//                        System.out.println("IDENTIFIER: " + word);
//                    }
//                }
//            }
//
//            sc.close();
//
//        } catch (Exception e) {
//            System.out.println("Error: " + e);
//        }
//
//        return tokens;
//    }
//}


import java.io.*;
import java.util.*;

public class Lexer {

    private static List<Token> detailedTokens = new ArrayList<>();

    public static List<Token> getTokens() {
        return detailedTokens;
    }

    public static void reset() {
        detailedTokens.clear();
    }

    public static List<String> runLexer() {
        List<String> tokens = new ArrayList<>();
        detailedTokens.clear();

        try {
            File file = new File("data/test.txt");
            Scanner sc = new Scanner(file);
            int lineNumber = 0;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineNumber++;

                // handle multi-character operators first
                line = line.replace(">=", " @GTE@ ")
                        .replace("<=", " @LTE@ ")
                        .replace("==", " @EQ@ ")
                        .replace("!=", " @NEQ@ ")
                        .replace("&&", " @AND@ ")
                        .replace("||", " @OR@ ");


                // handle single-character operators
                line = line.replace("(", " ( ")
                        .replace(")", " ) ")
                        .replace("{", " { ")
                        .replace("}", " } ")
                        .replace(";", " ; ")
                        .replace("+", " + ")
                        .replace("-", " - ")
                        .replace("*", " * ")
                        .replace("/", " / ")
                        .replace("%", " % ")
                        .replace("=", " = ")
                        .replace(">", " > ")
                        .replace("<", " < ");

                String[] words = line.trim().split("\\s+");

                for (String word : words) {

                    word = word.replace("@GTE@", ">=")
                            .replace("@LTE@", "<=")
                            .replace("@EQ@", "==")
                            .replace("@NEQ@", "!=")
                            .replace("@AND@", "&&")
                            .replace("@OR@", "||");

                    if (word.isEmpty()) continue;

                    String type = "";

                    // ===== IDENTIFY TOKEN TYPE =====
                    if (word.equals("int") || word.equals("float") ||
                            word.equals("if") || word.equals("else") ||
                            word.equals("print") || word.equals("while") ||
                            word.equals("for")) {

                        type = "KEYWORD";

                    } else if (word.matches("\\d+(\\.\\d+)?")) {

                        type = "NUMBER";

                    } else if (word.equals("=") || word.equals("+") || word.equals("-") ||
                            word.equals("*") || word.equals("/") || word.equals("%") ||
                            word.equals(">") || word.equals("<") ||
                            word.equals(">=") || word.equals("<=") ||
                            word.equals("==") || word.equals("!=") ||
                            word.equals("&&") || word.equals("||")) {

                        type = "OPERATOR";

                    } else if (word.equals("(") || word.equals(")") ||
                            word.equals("{") || word.equals("}") ||
                            word.equals(";")) {

                        type = "SYMBOL";

                    } else {

                        type = "IDENTIFIER";
                    }

                    // System.out.println(type + ": " + word);
                    tokens.add(word);
                    detailedTokens.add(new Token(word, type, lineNumber));
                }
            }

            sc.close();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return tokens;
    }
}