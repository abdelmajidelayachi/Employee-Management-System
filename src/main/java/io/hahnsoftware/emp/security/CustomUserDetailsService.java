package io.hahnsoftware.emp.security;

import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.Employee;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeDAO employeeDAO;

    public CustomUserDetailsService() throws SQLException {
        this.employeeDAO = new EmployeeDAO();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Employee employee = employeeDAO.findByUsername(username);
            if (employee == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return new User(employee.getUsername(), 
                          employee.getPasswordHash(),
                          Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())));
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Error loading user", e);
        }
    }
}