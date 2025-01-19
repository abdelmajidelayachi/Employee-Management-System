package io.hahnsoftware.emp.service;

import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dto.EmployeeDto;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.SearchCriteria;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeDAO employeeDAO;
    private final DepartmentDAO departmentDAO;

    public EmployeeService() throws SQLException {
        this.employeeDAO = new EmployeeDAO();
        this.departmentDAO = new DepartmentDAO(employeeDAO);
    }

    public Employee createEmployee(EmployeeDto dto, String password) throws SQLException {
        List<String> validationErrors = dto.validateEmployeeDto();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + String.join(", ", validationErrors));
        }
        Employee employee = dto.convertToEntity();
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


    public Employee updateEmployee(Long id, EmployeeDto dto) throws SQLException {
        List<String> validationErrors = dto.validateEmployeeDto();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + String.join(", ", validationErrors));
        }
        Employee employee = dto.convertToEntity();
        employee.setId(id);
        return employeeDAO.update(employee);
    }

    public void updatePassword(Long id, String password) throws SQLException {
        Employee employee = employeeDAO.findById(id);
        if (employee == null) {
            throw new RuntimeException("Employee not found with employee id: " + id);
        }
        employeeDAO.updatePassword(employee, password);
    }

    public void deleteEmployee(String employeeId) throws SQLException {
        employeeDAO.delete(employeeId);
    }

    public Employee validateCredentials(String username, String password) throws SQLException {
        return employeeDAO.validateCredentials(username, password);
    }
}