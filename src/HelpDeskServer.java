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
import java.util.LinkedHashMap;
import java.util.Map;

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

        Path csv = Files.createTempFile("helpdesk-", ".csv");
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
                "csv=" + csv
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output;
            try (InputStream input = process.getInputStream()) {
                output = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            int exitCode = process.waitFor();

            String json = escapeJson(output);
            byte[] body = ("{\"exitCode\":" + exitCode + ",\"output\":\"" + json + "\"}")
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
            Files.deleteIfExists(csv);
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
}
