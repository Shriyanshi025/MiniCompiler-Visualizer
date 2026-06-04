import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(8080);
        System.out.println("Server running at http://localhost:8080");

        while (true) {

            Socket socket = server.accept();
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream();

            String firstLine = in.readLine(); // VERY IMPORTANT

            // Handle browser preflight / GET
            if (firstLine == null || firstLine.startsWith("GET") || firstLine.startsWith("OPTIONS")) {

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "Access-Control-Allow-Methods: POST, GET, OPTIONS\r\n" +
                                "Access-Control-Allow-Headers: *\r\n" +
                                "\r\n" +
                                "Server Running";

                out.write(response.getBytes());
                socket.close();
                continue;
            }

            String line;
            int contentLength = 0;

            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            char[] body = new char[contentLength];
            in.read(body);
            String code = new String(body);

            // Reset all visualization states explicitly
            Lexer.reset();
            Semantic.reset();
            Executor.reset();

            String braceError = validateBraces(code);
            if (braceError != null) {
                String finalJson = "{" +
                        "\"output\":\"\"," +
                        "\"error\":\"" + escapeJson(braceError) + "\"," +
                        "\"tokens\":[]," +
                        "\"symbolTable\":[]," +
                        "\"executionTrace\":[]" +
                        "}";

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "\r\n" +
                                finalJson;

                out.write(response.getBytes());
                socket.close();
                continue;
            }

            // Preprocess Java-like syntax
            String preprocessedCode = preprocessJava(code);

            String semicolonError = validateSemicolons(preprocessedCode);
            if (semicolonError != null) {
                String finalJson = "{" +
                        "\"output\":\"\"," +
                        "\"error\":\"" + escapeJson(semicolonError) + "\"," +
                        "\"tokens\":[]," +
                        "\"symbolTable\":[]," +
                        "\"executionTrace\":[]" +
                        "}";

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "\r\n" +
                                finalJson;

                out.write(response.getBytes());
                socket.close();
                continue;
            }

            String expressionError = validateExpressions(preprocessedCode);
            if (expressionError != null) {
                String finalJson = "{" +
                        "\"output\":\"\"," +
                        "\"error\":\"" + escapeJson(expressionError) + "\"," +
                        "\"tokens\":[]," +
                        "\"symbolTable\":[]," +
                        "\"executionTrace\":[]" +
                        "}";

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "\r\n" +
                                finalJson;

                out.write(response.getBytes());
                socket.close();
                continue;
            }

            String semanticError = validateVariables(preprocessedCode);

            if (semanticError != null) {
                String finalJson = "{" +
                        "\"output\":\"\"," +
                        "\"error\":\"" + escapeJson(semanticError) + "\"," +
                        "\"tokens\":[]," +
                        "\"symbolTable\":[]," +
                        "\"executionTrace\":[]" +
                        "}";

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "\r\n" +
                                finalJson;

                out.write(response.getBytes());
                socket.close();
                continue;
            }

            String typeError = validateTypes(preprocessedCode);
            if (typeError != null) {
                String finalJson = "{" +
                        "\"output\":\"\"," +
                        "\"error\":\"" + escapeJson(typeError) + "\"," +
                        "\"tokens\":[]," +
                        "\"symbolTable\":[]," +
                        "\"executionTrace\":[]" +
                        "}";

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "\r\n" +
                                finalJson;

                out.write(response.getBytes());
                socket.close();
                continue;
            }

            // Save code
            FileWriter fw = new FileWriter("data/test.txt");
            fw.write(preprocessedCode);
            fw.close();

            // --- EXECUTION PIPELINE ---
            
            // 1. Lexing
            List<String> tokensList = Lexer.runLexer();
            
            // 2. Parsing
            Parser.checkSyntax(tokensList);
            
            // 3. Semantic Analysis
            Semantic.checkSemantics(tokensList);

            // 4. Execution
            Map<String, Double> values = new HashMap<>();
            Map<String, String> types = new HashMap<>();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream old = System.out;
            System.setOut(new PrintStream(baos));

            try {
                Executor.execute(tokensList, values, types, true);
            } catch (Exception e) {
                // Silently handle runtime errors to keep output clean
            }

            System.setOut(old);

            String output = baos.toString();

            // --- DATA COLLECTION ---

            // Collect tokens
            List<Token> detailedTokens = Lexer.getTokens();

            // Collect symbol table and sync final values
            Map<String, Symbol> tableMap = Semantic.getSymbolTable();
            for (String name : values.keySet()) {
                if (tableMap.containsKey(name)) {
                    tableMap.get(name).value = values.get(name);
                }
            }

            // Collect execution trace
            List<ExecutionStep> trace = Executor.getExecutionTrace();

            // --- JSON BUILDING ---
            
            StringBuilder jsonTokens = new StringBuilder("[");
            for (int i = 0; i < detailedTokens.size(); i++) {
                Token t = detailedTokens.get(i);
                jsonTokens.append("{")
                        .append("\"value\":\"").append(escapeJson(t.value)).append("\",")
                        .append("\"type\":\"").append(escapeJson(t.type)).append("\",")
                        .append("\"line\":").append(t.line)
                        .append("}");
                if (i < detailedTokens.size() - 1) jsonTokens.append(",");
            }
            jsonTokens.append("]");

            StringBuilder jsonSymbols = new StringBuilder("[");
            List<Symbol> symbolsList = new ArrayList<>(tableMap.values());
            for (int i = 0; i < symbolsList.size(); i++) {
                Symbol s = symbolsList.get(i);
                jsonSymbols.append("{")
                        .append("\"name\":\"").append(escapeJson(s.name)).append("\",")
                        .append("\"type\":\"").append(escapeJson(s.type)).append("\",")
                        .append("\"value\":").append(s.value)
                        .append("}");
                if (i < symbolsList.size() - 1) jsonSymbols.append(",");
            }
            jsonSymbols.append("]");

            StringBuilder jsonTrace = new StringBuilder("[");
            for (int i = 0; i < trace.size(); i++) {
                ExecutionStep step = trace.get(i);
                jsonTrace.append("{")
                        .append("\"step\":").append(step.step).append(",")
                        .append("\"action\":\"").append(escapeJson(step.action)).append("\",")
                        .append("\"variables\":{");
                
                List<String> keys = new ArrayList<>(step.variables.keySet());
                for (int k = 0; k < keys.size(); k++) {
                    String key = keys.get(k);
                    jsonTrace.append("\"").append(escapeJson(key)).append("\":").append(step.variables.get(key));
                    if (k < keys.size() - 1) jsonTrace.append(",");
                }
                
                jsonTrace.append("}}");
                if (i < trace.size() - 1) jsonTrace.append(",");
            }
            jsonTrace.append("]");

            String finalJson = "{" +
                    "\"tokens\":" + jsonTokens.toString() + "," +
                    "\"symbolTable\":" + jsonSymbols.toString() + "," +
                    "\"executionTrace\":" + jsonTrace.toString() + "," +
                    "\"output\":\"" + escapeJson(output) + "\"," +
                    "\"error\":\"\"" +
                    "}";

            String response =
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "\r\n" +
                            finalJson;

            out.write(response.getBytes());
            socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String preprocessJava(String code) {
        if (code == null) return "";

        StringBuilder sb = new StringBuilder(code);
        
        String wrapper1 = "public class Main {";
        int idx1 = code.indexOf(wrapper1);
        
        String wrapper2 = "public static void main(String[] args) {";
        int idx2 = code.indexOf(wrapper2);
        
        boolean found1 = idx1 != -1;
        boolean found2 = idx2 != -1;
        
        if (found1) {
            for (int i = idx1; i < idx1 + wrapper1.length(); i++) {
                char c = sb.charAt(i);
                if (c != '\n' && c != '\r') {
                    sb.setCharAt(i, ' ');
                }
            }
        }
        
        if (found2) {
            for (int i = idx2; i < idx2 + wrapper2.length(); i++) {
                char c = sb.charAt(i);
                if (c != '\n' && c != '\r') {
                    sb.setCharAt(i, ' ');
                }
            }
        }
        
        // Find closing braces not in comments or strings
        int n = code.length();
        boolean inString = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        java.util.List<Integer> closeBraces = new java.util.ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            char c = code.charAt(i);
            
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && i + 1 < n && code.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '"' && i > 0 && code.charAt(i - 1) != '\\') {
                    inString = false;
                }
                continue;
            }
            
            if (c == '/' && i + 1 < n && code.charAt(i + 1) == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && i + 1 < n && code.charAt(i + 1) == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            
            if (c == '}') {
                closeBraces.add(i);
            }
        }
        
        int numBracesToRemove = 0;
        if (found1) numBracesToRemove++;
        if (found2) numBracesToRemove++;
        
        int bracesSize = closeBraces.size();
        for (int k = 0; k < numBracesToRemove && k < bracesSize; k++) {
            int braceIdx = closeBraces.get(bracesSize - 1 - k);
            sb.setCharAt(braceIdx, ' ');
        }
        
        String transformed = sb.toString();
        transformed = transformed.replaceAll("System\\.out\\.println\\s*\\(\\s*(.*?)\\s*\\)\\s*;", "print $1;");
        transformed = transformed.replaceAll("System\\.out\\.print\\s*\\(\\s*(.*?)\\s*\\)\\s*;", "print $1;");
        
        return transformed;
    }

    public static String validateBraces(String originalCode) {
        if (originalCode == null) return null;

        int n = originalCode.length();
        boolean inString = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        Stack<Integer> openBraces = new Stack<>();
        int currentLine = 1;

        for (int i = 0; i < n; i++) {
            char c = originalCode.charAt(i);

            if (c == '\n') {
                currentLine++;
            }

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && i + 1 < n && originalCode.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '"' && i > 0 && originalCode.charAt(i - 1) != '\\') {
                    inString = false;
                }
                continue;
            }

            if (c == '/' && i + 1 < n && originalCode.charAt(i + 1) == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && i + 1 < n && originalCode.charAt(i + 1) == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }

            if (c == '{') {
                openBraces.push(currentLine);
            } else if (c == '}') {
                if (openBraces.isEmpty()) {
                    return "Syntax Error: Extra closing brace '}' at line " + currentLine;
                }
                openBraces.pop();
            }
        }

        if (!openBraces.isEmpty()) {
            int openLine = openBraces.peek();
            return "Syntax Error: Missing closing brace '}' for opening brace at line " + openLine;
        }

        return null;
    }

    public static String validateSemicolons(String preprocessedCode) {
        String cleanCode = stripCommentsAndPreserveNewlines(preprocessedCode);
        String[] lines = cleanCode.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            // Skip lines ending with '{' or containing only '}'
            if (trimmed.endsWith("{") || trimmed.equals("}")) {
                continue;
            }
            // Skip control flow structures
            if (trimmed.startsWith("while ") || trimmed.startsWith("while(") ||
                trimmed.startsWith("if ") || trimmed.startsWith("if(") ||
                trimmed.startsWith("for ") || trimmed.startsWith("for(") ||
                trimmed.equals("else") || trimmed.startsWith("else ")) {
                continue;
            }

            if (isStatementRequiringSemicolon(trimmed)) {
                if (!trimmed.endsWith(";")) {
                    return "Syntax Error: Missing semicolon at line " + (i + 1);
                }
            }
        }
        return null;
    }

    private static boolean isStatementRequiringSemicolon(String trimmed) {
        if (trimmed.startsWith("int ") || trimmed.startsWith("int\t")) {
            return true;
        }
        if (trimmed.startsWith("float ") || trimmed.startsWith("float\t")) {
            return true;
        }
        if (trimmed.startsWith("print ") || trimmed.startsWith("print(") || trimmed.equals("print")) {
            return true;
        }
        if (trimmed.endsWith("++") || trimmed.endsWith("--") || trimmed.contains("++") || trimmed.contains("--")) {
            return true;
        }
        if (trimmed.contains("=")) {
            if (!trimmed.startsWith("if ") && !trimmed.startsWith("while ") && !trimmed.startsWith("for ") &&
                !trimmed.startsWith("if(") && !trimmed.startsWith("while(") && !trimmed.startsWith("for(")) {
                
                String temp = trimmed.replace("==", "").replace("!=", "").replace("<=", "").replace(">=", "");
                if (temp.contains("=")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String stripCommentsAndPreserveNewlines(String code) {
        if (code == null) return "";
        StringBuilder sb = new StringBuilder(code);
        boolean inBlockComment = false;
        boolean inLineComment = false;
        boolean inString = false;
        int n = code.length();
        for (int i = 0; i < n; i++) {
            char c = code.charAt(i);
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                } else if (c != '\r') {
                    sb.setCharAt(i, ' ');
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && i + 1 < n && code.charAt(i + 1) == '/') {
                    sb.setCharAt(i, ' ');
                    sb.setCharAt(i + 1, ' ');
                    inBlockComment = false;
                    i++;
                } else if (c != '\n' && c != '\r') {
                    sb.setCharAt(i, ' ');
                }
                continue;
            }
            if (inString) {
                if (c == '"' && i > 0 && code.charAt(i - 1) != '\\') {
                    inString = false;
                }
                continue;
            }
            if (c == '/' && i + 1 < n && code.charAt(i + 1) == '/') {
                sb.setCharAt(i, ' ');
                sb.setCharAt(i + 1, ' ');
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && i + 1 < n && code.charAt(i + 1) == '*') {
                sb.setCharAt(i, ' ');
                sb.setCharAt(i + 1, ' ');
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"') {
                inString = true;
            }
        }
        return sb.toString();
    }

    public static String validateExpressions(String preprocessedCode) {
        String cleanCode = stripCommentsAndPreserveNewlines(preprocessedCode);
        String[] lines = cleanCode.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            // Skip lines ending with '{' or containing only '}'
            if (trimmed.endsWith("{") || trimmed.equals("}")) {
                continue;
            }
            // Skip control flow structures
            if (trimmed.startsWith("while ") || trimmed.startsWith("while(") ||
                trimmed.startsWith("if ") || trimmed.startsWith("if(") ||
                trimmed.startsWith("for ") || trimmed.startsWith("for(") ||
                trimmed.equals("else") || trimmed.startsWith("else ")) {
                continue;
            }

            // If it is a declaration or assignment:
            if (isDeclarationOrAssignment(trimmed)) {
                String stmt = trimmed;
                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length() - 1).trim();
                }
                
                int eqIdx = stmt.indexOf("=");
                if (eqIdx != -1) {
                    String expr = stmt.substring(eqIdx + 1).trim();
                    if (!isValidExpression(expr)) {
                        return "Syntax Error: Invalid expression at line " + (i + 1);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isDeclarationOrAssignment(String trimmed) {
        if (trimmed.startsWith("int ") || trimmed.startsWith("int\t")) {
            return true;
        }
        if (trimmed.startsWith("float ") || trimmed.startsWith("float\t")) {
            return true;
        }
        if (trimmed.contains("=")) {
            String temp = trimmed.replace("==", "").replace("!=", "").replace("<=", "").replace(">=", "");
            if (temp.contains("=")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidExpression(String expr) {
        List<String> tokens = tokenizeExpression(expr);
        if (tokens.isEmpty()) {
            return false;
        }

        boolean expectingOperand = true;
        int parenDepth = 0;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("(")) {
                if (!expectingOperand) {
                    return false;
                }
                parenDepth++;
            } else if (token.equals(")")) {
                if (expectingOperand) {
                    return false;
                }
                parenDepth--;
                if (parenDepth < 0) {
                    return false;
                }
                expectingOperand = false;
            } else if (isOperator(token)) {
                if (expectingOperand) {
                    return false;
                }
                expectingOperand = true;
            } else if (isOperand(token)) {
                if (!expectingOperand) {
                    return false;
                }
                expectingOperand = false;
            } else {
                return false;
            }
        }

        return !expectingOperand && parenDepth == 0;
    }

    private static List<String> tokenizeExpression(String expr) {
        String spaced = expr.replace("(", " ( ")
                            .replace(")", " ) ")
                            .replace("+", " + ")
                            .replace("-", " - ")
                            .replace("*", " * ")
                            .replace("/", " / ")
                            .replace("%", " % ")
                            .replace(";", " ; ");
        String[] parts = spaced.trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty() && !p.equals(";")) {
                tokens.add(p);
            }
        }
        return tokens;
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("%");
    }

    private static boolean isOperand(String token) {
        return token.matches("\\d+(\\.\\d+)?") || token.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    public static String validateVariables(String preprocessedCode) {
        String cleanCode = stripCommentsAndPreserveNewlines(preprocessedCode);
        String[] lines = cleanCode.split("\n", -1);
        Set<String> declaredVars = new HashSet<>();

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            // Skip lines containing only '}'
            if (trimmed.equals("}")) {
                continue;
            }
            if (trimmed.endsWith(";")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
            }

            int lineNum = i + 1;

            if (trimmed.startsWith("int ") || trimmed.startsWith("int\t") ||
                trimmed.startsWith("float ") || trimmed.startsWith("float\t")) {
                
                // Declaration
                boolean isFloat = trimmed.startsWith("float");
                int skipLen = isFloat ? 5 : 3;
                String rest = trimmed.substring(skipLen).trim();
                String[] parts = rest.split("[=\\s;]");
                if (parts.length == 0 || parts[0].trim().isEmpty()) {
                    continue;
                }
                String varName = parts[0].trim();

                // Extract identifiers on the RHS (after the first '=')
                int eqIdx = trimmed.indexOf("=");
                if (eqIdx != -1) {
                    String rhs = trimmed.substring(eqIdx + 1);
                    List<String> rhsIdents = extractIdentifiers(rhs);
                    for (String id : rhsIdents) {
                        if (!declaredVars.contains(id)) {
                            return "Semantic Error: Variable '" + id + "' is not declared at line " + lineNum;
                        }
                    }
                }
                declaredVars.add(varName);

            } else if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) {
                // For loop
                String loopVar = "";
                int intIdx = trimmed.indexOf("int ");
                int floatIdx = trimmed.indexOf("float ");
                int declIdx = -1;
                int skipLen = 0;
                if (intIdx != -1) {
                    declIdx = intIdx;
                    skipLen = 4;
                } else if (floatIdx != -1) {
                    declIdx = floatIdx;
                    skipLen = 6;
                }

                if (declIdx != -1) {
                    String afterDecl = trimmed.substring(declIdx + skipLen).trim();
                    String[] parts = afterDecl.split("[=\\s;]");
                    if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                        loopVar = parts[0].trim();
                        declaredVars.add(loopVar);
                    }
                }

                // Check other identifiers in the line
                List<String> idents = extractIdentifiers(trimmed);
                for (String id : idents) {
                    if (id.equals(loopVar)) {
                        continue;
                    }
                    if (!declaredVars.contains(id)) {
                        return "Semantic Error: Variable '" + id + "' is not declared at line " + lineNum;
                    }
                }

            } else {
                // Other statement (assignment, print, condition, loop, increment/decrement)
                List<String> idents = extractIdentifiers(trimmed);
                for (String id : idents) {
                    if (!declaredVars.contains(id)) {
                        return "Semantic Error: Variable '" + id + "' is not declared at line " + lineNum;
                    }
                }
            }
        }
        return null;
    }

    private static List<String> extractIdentifiers(String expr) {
        List<String> tokens = tokenizeExpression(expr);
        List<String> idents = new ArrayList<>();
        for (String t : tokens) {
            if (t.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                if (!t.equals("int") && !t.equals("float") && !t.equals("print") &&
                    !t.equals("while") && !t.equals("if") && !t.equals("for") &&
                    !t.equals("else") && !t.equals("System") && !t.equals("out") &&
                    !t.equals("println")) {
                    idents.add(t);
                }
            }
        }
        return idents;
    }

    public static String validateTypes(String preprocessedCode) {
        String cleanCode = stripCommentsAndPreserveNewlines(preprocessedCode);
        String[] lines = cleanCode.split("\n", -1);
        Map<String, String> types = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.equals("}")) {
                continue;
            }
            if (trimmed.endsWith(";")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
            }

            int lineNum = i + 1;

            if (trimmed.startsWith("int ") || trimmed.startsWith("int\t") ||
                trimmed.startsWith("float ") || trimmed.startsWith("float\t")) {
                
                boolean isFloat = trimmed.startsWith("float");
                String varType = isFloat ? "float" : "int";
                int skipLen = isFloat ? 5 : 3;
                String rest = trimmed.substring(skipLen).trim();
                String[] parts = rest.split("[=\\s;]");
                if (parts.length == 0 || parts[0].trim().isEmpty()) {
                    continue;
                }
                String varName = parts[0].trim();

                int eqIdx = trimmed.indexOf("=");
                if (eqIdx != -1) {
                    String rhs = trimmed.substring(eqIdx + 1).trim();
                    if (varType.equals("int")) {
                        if (expressionContainsFloat(rhs, types)) {
                            return "Type Error: Cannot assign float value to int variable at line " + lineNum;
                        }
                    }
                }
                types.put(varName, varType);

            } else if (trimmed.startsWith("for ") || trimmed.startsWith("for(")) {
                String loopVar = "";
                int intIdx = trimmed.indexOf("int ");
                int floatIdx = trimmed.indexOf("float ");
                int declIdx = -1;
                int skipLen = 0;
                String varType = "";
                if (intIdx != -1) {
                    declIdx = intIdx;
                    skipLen = 4;
                    varType = "int";
                } else if (floatIdx != -1) {
                    declIdx = floatIdx;
                    skipLen = 6;
                    varType = "float";
                }

                if (declIdx != -1) {
                    String afterDecl = trimmed.substring(declIdx + skipLen).trim();
                    String[] parts = afterDecl.split("[=\\s;]");
                    if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                        loopVar = parts[0].trim();
                        
                        int eqIdx = afterDecl.indexOf("=");
                        if (eqIdx != -1) {
                            String initRest = afterDecl.substring(eqIdx + 1).trim();
                            int scIdx = initRest.indexOf(";");
                            String rhs = (scIdx != -1) ? initRest.substring(0, scIdx).trim() : initRest;
                            if (varType.equals("int") && expressionContainsFloat(rhs, types)) {
                                return "Type Error: Cannot assign float value to int variable at line " + lineNum;
                            }
                        }
                        types.put(loopVar, varType);
                    }
                }

            } else {
                if (isAssignment(trimmed)) {
                    int eqIdx = trimmed.indexOf("=");
                    if (eqIdx != -1) {
                        String lhs = trimmed.substring(0, eqIdx).trim();
                        String rhs = trimmed.substring(eqIdx + 1).trim();
                        if (types.containsKey(lhs) && types.get(lhs).equals("int")) {
                            if (expressionContainsFloat(rhs, types)) {
                                return "Type Error: Cannot assign float value to int variable at line " + lineNum;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isAssignment(String trimmed) {
        if (trimmed.contains("=")) {
            if (!trimmed.startsWith("if ") && !trimmed.startsWith("while ") && !trimmed.startsWith("for ") &&
                !trimmed.startsWith("if(") && !trimmed.startsWith("while(") && !trimmed.startsWith("for(")) {
                
                String temp = trimmed.replace("==", "").replace("!=", "").replace("<=", "").replace(">=", "");
                if (temp.contains("=")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean expressionContainsFloat(String expr, Map<String, String> types) {
        List<String> tokens = tokenizeExpression(expr);
        for (String t : tokens) {
            if (t.matches("\\d+\\.\\d+")) {
                return true;
            }
            if (types.containsKey(t) && types.get(t).equals("float")) {
                return true;
            }
        }
        return false;
    }
}