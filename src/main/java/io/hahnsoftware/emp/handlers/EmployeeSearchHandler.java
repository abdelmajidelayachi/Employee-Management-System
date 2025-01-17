package io.hahnsoftware.emp.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import io.hahnsoftware.emp.config.AuthenticationUtil;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.SearchCriteria;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.UserRole;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeSearchHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final EmployeeDAO employeeDAO;
    
    public EmployeeSearchHandler() {
        try {
            this.employeeDAO = new EmployeeDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize handler", e);
        }
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Check authentication
            User currentUser = AuthenticationUtil.authenticate(exchange);
            if (currentUser == null) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }
            
            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            // Parse query parameters
            Map<String, String> queryParams = parseQueryString(exchange.getRequestURI().getQuery());
            
            // Build search criteria
            SearchCriteria criteria = new SearchCriteria();
            criteria.setName(queryParams.get("name"));
            criteria.setDepartment(queryParams.get("department"));
            criteria.setStatus(queryParams.get("status"));
            criteria.setJobTitle(queryParams.get("jobTitle"));
            criteria.setEmployeeId(queryParams.get("employeeId"));
            
            if (queryParams.containsKey("startDate")) {
                criteria.setStartDate(LocalDate.parse(queryParams.get("startDate")));
            }
            if (queryParams.containsKey("endDate")) {
                criteria.setEndDate(LocalDate.parse(queryParams.get("endDate")));
            }
            
            // Set pagination if provided
            if (queryParams.containsKey("page")) {
                criteria.setPage(Integer.parseInt(queryParams.get("page")));
            }
            if (queryParams.containsKey("size")) {
                criteria.setSize(Integer.parseInt(queryParams.get("size")));
            }
            
            // Apply department restriction for managers
            if (currentUser.getRole() == UserRole.MANAGER && currentUser.getDepartment() != null) {
                criteria.setDepartment(currentUser.getDepartment().getId().toString());
            }
            
            // Perform search
            List<Employee> results = employeeDAO.search(criteria);
            
            sendResponse(exchange, 200, gson.toJson(results));
            
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
    
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}