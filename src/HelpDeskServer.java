import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpDeskServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", HelpDeskServer::handleRoot);
        server.createContext("/api/run", HelpDeskServer::handleRun);
        server.setExecutor(null);
        server.start();
        System.out.println("Help Desk simulation available at http://localhost:" + port);
    }

    private static void handleRoot(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Path index = Paths.get("web", "index.html");
        byte[] body = Files.readAllBytes(index);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private static void handleRun(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
        int rodadas = bounded(params, "rodadas", 20, 1, 1000);
        int atendentes = bounded(params, "atendentes", 3, 1, 100);
        int classificadores = bounded(params, "classificadores", 2, 1, 100);
        int capacidade = bounded(params, "capacidade", 20, 1, 100000);
        int maxChegada = bounded(params, "maxchegada", 3, 0, 100000);
        long seed = seed(params);

        Path csvPath = Files.createTempFile("helpdesk-", ".csv");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", "src",
                "SimuladorHelpDesk",
                "seed=" + seed,
                "rodadas=" + rodadas,
                "atendentes=" + atendentes,
                "classificadores=" + classificadores,
                "capacidade=" + capacidade,
                "maxchegada=" + maxChegada,
                "csv=" + csvPath
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output;
            try (InputStream input = process.getInputStream()) {
                output = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            int exitCode = process.waitFor();

            String csv = Files.readString(csvPath, StandardCharsets.UTF_8);
            byte[] body = ("{\"exitCode\":" + exitCode
                + ",\"seed\":\"" + escapeJson(String.valueOf(seed))
                + "\",\"summary\":" + extractSummary(output)
                + ",\"history\":" + extractHistory(csv)
                + ",\"output\":\"" + escapeJson(output) + "\"}")
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(exitCode == 0 ? 200 : 500, body.length);
            try (OutputStream response = exchange.getResponseBody()) {
                response.write(body);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Execution interrupted", exception);
        } finally {
            Files.deleteIfExists(csvPath);
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return params;

        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private static int bounded(Map<String, String> params, String name, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(params.getOrDefault(name, String.valueOf(fallback)));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static long seed(Map<String, String> params) {
        try {
            return Long.parseLong(params.getOrDefault("seed", "42"));
        } catch (NumberFormatException exception) {
            return 42L;
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static String extractSummary(String output) {
        Map<String, String> values = new LinkedHashMap<>();
        addSummaryValue(values, "chegaram", output, "Chamados chegaram\\s*:\\s*(\\d+)");
        addSummaryValue(values, "recusados", output, "Chamados recusados\\s*:\\s*(\\d+)\\s*\\(([^%)]+)%?\\)");
        addSummaryValue(values, "classificados", output, "Chamados classificados\\s*:\\s*(\\d+)");
        addSummaryValue(values, "concluidos", output, "Chamados conclu[^:]*dos\\s*:\\s*(\\d+)");
        addSummaryValue(values, "esperaEntrada", output, "Espera[^:]*fila entrada\\s*:\\s*([\\d,\\.]+)");
        addSummaryValue(values, "esperaPosClassificacao", output, "Espera[^:]*p.s-classifica[^:]*\\s*:\\s*([\\d,\\.]+)");
        addSummaryValue(values, "ocupacao", output, "Ocupa[^:]*atendentes\\s*:\\s*([\\d,\\.]+)");

        String result = "{";
        boolean first = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!first) result += ",";
            result += "\"" + entry.getKey() + "\":\"" + escapeJson(entry.getValue()) + "\"";
            first = false;
        }
        return result + "}";
    }

    private static void addSummaryValue(Map<String, String> values, String name, String output, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(output);
        if (matcher.find()) {
            values.put(name, matcher.group(1).replace(',', '.'));
            if (matcher.groupCount() > 1) {
                values.put(name + "Percent", matcher.group(2).replace(',', '.'));
            }
        }
    }

    private static String extractHistory(String csv) {
        String[] lines = csv.split("\\R");
        if (lines.length < 2) return "[]";

        List<String> rows = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] columns = lines[i].split(",");
            if (columns.length < 6) continue;
            try {
                rows.add("{\"rodada\":" + Integer.parseInt(columns[0])
                    + ",\"filaEntrada\":" + Integer.parseInt(columns[1])
                    + ",\"filaCritico\":" + Integer.parseInt(columns[2])
                    + ",\"filaAlto\":" + Integer.parseInt(columns[3])
                    + ",\"filaNormal\":" + Integer.parseInt(columns[4])
                    + ",\"concluidos\":" + Integer.parseInt(columns[5]) + "}");
            } catch (NumberFormatException ignored) {
            }
        }
        return "[" + String.join(",", rows) + "]";
    }
}
