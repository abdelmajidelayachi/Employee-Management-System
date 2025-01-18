package io.hahnsoftware.emp.config;

import com.sun.net.httpserver.HttpExchange;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.User;

import java.util.Base64;
import java.sql.SQLException;

public class AuthenticationUtil {
    private static final EmployeeDAO employeeDAO;
    
    static {
        try {
            employeeDAO = new EmployeeDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserDAO", e);
        }
    }
    
    public static Employee authenticate(HttpExchange exchange) {
        // Get Authorization header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }
        
        try {
            // Decode Basic Auth credentials
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);
            
            if (values.length != 2) {
                return null;
            }
            
            String username = values[0];
            String password = values[1];
            
            // Validate credentials
            return employeeDAO.validateCredentials(username, password);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
