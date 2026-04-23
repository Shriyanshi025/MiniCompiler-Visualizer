import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(8080);
        System.out.println("Server running at http://localhost:8080");

        while (true) {

            Socket socket = server.accept();

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
            //  Remove Java wrapper if present
            if (code.contains("public class")) {
                int start = code.indexOf("{");
                int end = code.lastIndexOf("}");

                if (start != -1 && end != -1) {
                    code = code.substring(start + 1, end);
                }
            }

            // Save code
            FileWriter fw = new FileWriter("data/test.txt");
            fw.write(code);
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
                    "\"output\":\"" + escapeJson(output) + "\"" +
                    "}";

            String response =
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "\r\n" +
                            finalJson;

            out.write(response.getBytes());
            socket.close();
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
}