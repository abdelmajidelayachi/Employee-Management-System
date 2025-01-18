package io.hahnsoftware.emp.api;

import com.sun.net.httpserver.HttpServer;
import io.hahnsoftware.emp.handlers.EmployeeHandler;
import io.hahnsoftware.emp.handlers.EmployeeSearchHandler;
import io.hahnsoftware.emp.handlers.OpenAPIHandler;
import io.hahnsoftware.emp.handlers.SwaggerUIHandler;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executors;

public class ApiServer {
    private static final int PORT = 8080;

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API endpoints
        server.createContext("/api/employees", new EmployeeHandler());
        server.createContext("/api/employees/search", new EmployeeSearchHandler());

        // Swagger documentation
        server.createContext("/swagger-ui", new SwaggerUIHandler());
        server.createContext("/openapi.json", new OpenAPIHandler());

        // Set executor
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Server started on port " + PORT);
        System.out.println("Swagger UI available at http://localhost:" + PORT + "/swagger-ui/");
        System.out.println("OpenAPI specification available at http://localhost:" + PORT + "/openapi.json");
    }
}