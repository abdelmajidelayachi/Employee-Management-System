package io.hahnsoftware.emp.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.hahnsoftware.emp.config.AuthenticationUtil;
import io.hahnsoftware.emp.config.AuthorizationUtil;
import com.google.gson.Gson;
import io.hahnsoftware.emp.dto.AuditDAO;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final EmployeeDAO employeeDAO;
    private final AuditDAO auditDAO;

    public EmployeeHandler() {
        try {
            this.employeeDAO = new EmployeeDAO();
            this.auditDAO = new AuditDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize handler", e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            User currentUser = AuthenticationUtil.authenticate(exchange);
//            if (currentUser == null) {
//                sendResponse(exchange, 401, "Unauthorized");
//                return;
//            }

            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGet(exchange, currentUser);
                    break;
                case "POST":
                    handlePost(exchange, currentUser);
                    break;
                case "PUT":
                    handlePut(exchange, currentUser);
                    break;
                case "DELETE":
                    handleDelete(exchange, currentUser);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    @Operation(
            summary = "Get all employees or a specific employee",
            description = "Retrieves either all employees or a specific employee by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = Employee.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    private void handleGet(HttpExchange exchange, User currentUser) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length == 3) {
//            if (!AuthorizationUtil.canViewEmployees(currentUser)) {
//                sendResponse(exchange, 403, "Forbidden");
//                return;
//            }

            List<Employee> employees = employeeDAO.findAll();
            sendResponse(exchange, 200, gson.toJson(employees));

        } else if (parts.length == 4) {
            Long employeeId = Long.parseLong(parts[3]);
            Employee employee = employeeDAO.findById(employeeId);

            if (employee == null) {
                sendResponse(exchange, 404, "Employee not found");
                return;
            }

            if (!AuthorizationUtil.canViewEmployee(currentUser, employee)) {
                sendResponse(exchange, 403, "Forbidden");
                return;
            }

            sendResponse(exchange, 200, gson.toJson(employee));
        }
    }

    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee record",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = Employee.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Employee created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    private void handlePost(HttpExchange exchange, User currentUser) throws IOException, SQLException {
        if (!AuthorizationUtil.canCreateEmployee(currentUser)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        Employee employee = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody()),
                Employee.class
        );

        List<String> validationErrors = validateEmployee(employee);
        if (!validationErrors.isEmpty()) {
            sendResponse(exchange, 400, gson.toJson(validationErrors));
            return;
        }

        Employee created = employeeDAO.create(employee);
        auditDAO.logAction("CREATE", "EMPLOYEE", created.getId(), currentUser.getId());
        sendResponse(exchange, 201, gson.toJson(created));
    }

    @Operation(
            summary = "Update an employee",
            description = "Updates an existing employee record",
            parameters = {
                    @Parameter(name = "id", description = "Employee ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Employee updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    private void handlePut(HttpExchange exchange, User currentUser) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Long employeeId = Long.parseLong(parts[3]);

        Employee existing = employeeDAO.findById(employeeId);
        if (existing == null) {
            sendResponse(exchange, 404, "Employee not found");
            return;
        }

        if (!AuthorizationUtil.canUpdateEmployee(currentUser, existing)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        Employee updated = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody()),
                Employee.class
        );
        updated.setId(employeeId);

        List<String> validationErrors = validateEmployee(updated);
        if (!validationErrors.isEmpty()) {
            sendResponse(exchange, 400, gson.toJson(validationErrors));
            return;
        }

        Employee result = employeeDAO.update(updated);
        auditDAO.logAction("UPDATE", "EMPLOYEE", result.getId(), currentUser.getId());
        sendResponse(exchange, 200, gson.toJson(result));
    }

    @Operation(
            summary = "Delete an employee",
            description = "Deletes an existing employee record",
            parameters = {
                    @Parameter(name = "id", description = "Employee ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Employee deleted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    private void handleDelete(HttpExchange exchange, User currentUser) throws IOException, SQLException {
        if (!AuthorizationUtil.canDeleteEmployee(currentUser)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Long employeeId = Long.parseLong(parts[3]);

        Employee employee = employeeDAO.findById(employeeId);
        if (employee == null) {
            sendResponse(exchange, 404, "Employee not found");
            return;
        }

        employeeDAO.delete(employee.getEmployeeId());
        auditDAO.logAction("DELETE", "EMPLOYEE", employeeId, currentUser.getId());
        sendResponse(exchange, 204, "");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private List<String> validateEmployee(Employee employee) {
        List<String> errors = new ArrayList<>();

        if (employee.getEmployeeId() == null || employee.getEmployeeId().trim().isEmpty()) {
            errors.add("Employee ID is required");
        }

        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            errors.add("Full name is required");
        }

        if (employee.getEmail() == null || !employee.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.add("Valid email is required");
        }

        return errors;
    }
}