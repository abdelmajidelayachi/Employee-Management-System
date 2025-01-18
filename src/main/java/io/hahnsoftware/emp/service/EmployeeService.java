package io.hahnsoftware.emp.service;

import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.SearchCriteria;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeDAO employeeDAO;

    public EmployeeService() throws SQLException {
        this.employeeDAO = new EmployeeDAO();
    }

    public Employee createEmployee(Employee employee, String password) throws SQLException {
        List<String> validationErrors = employeeDAO.validateEmployee(employee);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + String.join(", ", validationErrors));
        }
        return employeeDAO.create(employee, password, true);
    }

    public Employee getEmployeeById(Long id) throws SQLException {
        Employee employee = employeeDAO.findById(id);
        if (employee == null) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        return employee;
    }

    public Employee getEmployeeByEmployeeId(String employeeId) throws SQLException {
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        if (employee == null) {
            throw new RuntimeException("Employee not found with employee id: " + employeeId);
        }
        return employee;
    }

    public List<Employee> getAllEmployees() throws SQLException {
        return employeeDAO.findAll();
    }

    public List<Employee> getAllManagers() throws SQLException {
        return employeeDAO.findAllManagers();
    }

    public List<Employee> searchEmployees(SearchCriteria criteria) throws SQLException {
        return employeeDAO.search(criteria);
    }

    public Employee updateEmployee(Employee employee) throws SQLException {
        List<String> validationErrors = employeeDAO.validateEmployee(employee);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + String.join(", ", validationErrors));
        }
        return employeeDAO.update(employee);
    }

    public void updatePassword(Long employeeId, String newPassword) throws SQLException {
        Employee employee = getEmployeeById(employeeId);
        employeeDAO.updatePassword(employee, newPassword);
    }

    public void deleteEmployee(String employeeId) throws SQLException {
        employeeDAO.delete(employeeId);
    }

    public Employee validateCredentials(String username, String password) throws SQLException {
        return employeeDAO.validateCredentials(username, password);
    }
}