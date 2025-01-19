package io.hahnsoftware.emp.security;

import io.hahnsoftware.emp.dao.AuditDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.sql.SQLException;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private final EmployeeDAO employeeDAO;

    public UserContextInterceptor() throws SQLException {
        this.employeeDAO = new EmployeeDAO();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip for login endpoint
        if (request.getRequestURI().equals("/api/auth/login")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            AuditDAO.setActionUser(employeeDAO.findByUsername(username));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clear the user context after the request is complete
        AuditDAO.setActionUser(null);
    }
}