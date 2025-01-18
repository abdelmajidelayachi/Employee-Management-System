package io.hahnsoftware.emp.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.hahnsoftware.emp.config.SwaggerConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class OpenAPIHandler implements HttpHandler {
    private final String openAPISpec;

    public OpenAPIHandler() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        openAPISpec = gson.toJson(SwaggerConfig.createOpenAPI());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Add CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = openAPISpec.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}