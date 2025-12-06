package JavaServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/single", new SingleHandler());

        server.createContext("/", exchange -> {
            String msg = "Servidor rodando. Use GET /single\n";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, msg.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(msg.getBytes(StandardCharsets.UTF_8));
            }
        });

        server.setExecutor(null); 
        server.start();
        System.out.println("Servidor iniciado na porta " + port + " - rota: http://localhost:" + port + "/single");
    }

    static class SingleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String methodNotAllowed = "Método não permitido\n";
                exchange.sendResponseHeaders(405, methodNotAllowed.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(methodNotAllowed.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String email = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("email=")) {
                        email = param.substring("email=".length());
                    }
                }
            }

            if (email == null) {
                String json = "{\"error\": \"Parâmetro 'email' é obrigatório\"}";
                sendJson(exchange, 400, json);
                return;
            }

            boolean isValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

            String json;
            if (isValid) {
                json = "{\"valid\": true, \"email\": \"" + email + "\"}";
            } else {
                json = "{\"valid\": false, \"message\": \"Email inválido\"}";
            }

            sendJson(exchange, 200, json);
        }

        // Função auxiliar para enviar JSON
        private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

            byte[] resp = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        }
    }
}
