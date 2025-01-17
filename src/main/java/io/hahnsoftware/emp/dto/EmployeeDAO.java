package io.hahnsoftware.emp.dto;


import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.EmploymentStatus;
import io.hahnsoftware.emp.model.SearchCriteria;
import io.hahnsoftware.emp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private final Connection connection;
    private final DepartmentDAO departmentDAO;

    public EmployeeDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.departmentDAO = new DepartmentDAO();
    }

    public Employee create(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (employee_id, full_name, job_title, department_id, " +
                "hire_date, status, email, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"ID"})) {  // Specify the ID column
            stmt.setString(1, employee.getEmployeeId());
            stmt.setString(2, employee.getFullName());
            stmt.setString(3, employee.getJobTitle());
            stmt.setLong(4, employee.getDepartment().getId());
            stmt.setDate(5, Date.valueOf(employee.getHireDate()));
            stmt.setString(6, employee.getStatus().name());
            stmt.setString(7, employee.getEmail());
            stmt.setString(8, employee.getPhone());
            stmt.setString(9, employee.getAddress());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    employee.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }

            connection.commit();
            return employee;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    public Employee findById(Long id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }

        return null;
    }

    public Employee findByEmployeeId(String employeeId) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }

        return null;
    }

    public List<Employee> findAll() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }

        return employees;
    }

    public List<Employee> search(SearchCriteria criteria) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM employees WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            sql.append(" AND LOWER(full_name) LIKE ?");
            params.add("%" + criteria.getName().toLowerCase() + "%");
        }

        if (criteria.getDepartment() != null) {
            sql.append(" AND department_id = ?");
            params.add(Long.parseLong(criteria.getDepartment()));
        }

        if (criteria.getStatus() != null) {
            sql.append(" AND status = ?");
            params.add(criteria.getStatus());
        }

        if (criteria.getStartDate() != null) {
            sql.append(" AND hire_date >= ?");
            params.add(Date.valueOf(criteria.getStartDate()));
        }

        if (criteria.getEndDate() != null) {
            sql.append(" AND hire_date <= ?");
            params.add(Date.valueOf(criteria.getEndDate()));
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
                }
            }
        }

        return employees;
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setEmployeeId(rs.getString("employee_id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setJobTitle(rs.getString("job_title"));
        employee.setHireDate(rs.getDate("hire_date").toLocalDate());
        employee.setStatus(EmploymentStatus.valueOf(rs.getString("status")));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));

        // Load department
        Long departmentId = rs.getLong("department_id");
        if (!rs.wasNull()) {
            employee.setDepartment(departmentDAO.findById(departmentId));
        }

        return employee;
    }
    public Employee update(Employee employee) throws SQLException {
        String sql = "UPDATE employees SET " +
                "employee_id = ?, " +
                "full_name = ?, " +
                "job_title = ?, " +
                "department_id = ?, " +
                "hire_date = ?, " +
                "status = ?, " +
                "email = ?, " +
                "phone = ?, " +
                "address = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employee.getEmployeeId());
            stmt.setString(2, employee.getFullName());
            stmt.setString(3, employee.getJobTitle());
            stmt.setLong(4, employee.getDepartment().getId());
            stmt.setDate(5, Date.valueOf(employee.getHireDate()));
            stmt.setString(6, employee.getStatus().name());
            stmt.setString(7, employee.getEmail());
            stmt.setString(8, employee.getPhone());
            stmt.setString(9, employee.getAddress());
            stmt.setLong(10, employee.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }

            connection.commit();
            return findById(employee.getId());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<String> validateEmployee(Employee employee) {
        List<String> errors = new ArrayList<>();

        // Check for null employee
        if (employee == null) {
            errors.add("Employee cannot be null");
            return errors;
        }

        // Validate Employee ID
        if (employee.getEmployeeId() == null || employee.getEmployeeId().trim().isEmpty()) {
            errors.add("Employee ID is required");
        } else if (employee.getEmployeeId().length() > 20) {
            errors.add("Employee ID cannot be longer than 20 characters");
        }

        // Validate Full Name
        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            errors.add("Full name is required");
        } else if (employee.getFullName().length() > 100) {
            errors.add("Full name cannot be longer than 100 characters");
        }

        // Validate Job Title
        if (employee.getJobTitle() == null || employee.getJobTitle().trim().isEmpty()) {
            errors.add("Job title is required");
        } else if (employee.getJobTitle().length() > 100) {
            errors.add("Job title cannot be longer than 100 characters");
        }

        // Validate Department
        if (employee.getDepartment() == null) {
            errors.add("Department is required");
        } else if (employee.getDepartment().getId() == null) {
            errors.add("Invalid department");
        }

        // Validate Hire Date
        if (employee.getHireDate() == null) {
            errors.add("Hire date is required");
        } else if (employee.getHireDate().isAfter(LocalDate.now())) {
            errors.add("Hire date cannot be in the future");
        }

        // Validate Employment Status
        if (employee.getStatus() == null) {
            errors.add("Employment status is required");
        }

        // Validate Email
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!isValidEmail(employee.getEmail())) {
            errors.add("Invalid email format");
        } else if (employee.getEmail().length() > 100) {
            errors.add("Email cannot be longer than 100 characters");
        }

        // Validate Phone (optional but must be valid if provided)
        if (employee.getPhone() != null && !employee.getPhone().trim().isEmpty()) {
            if (!isValidPhone(employee.getPhone())) {
                errors.add("Invalid phone number format");
            } else if (employee.getPhone().length() > 20) {
                errors.add("Phone number cannot be longer than 20 characters");
            }
        }

        // Validate Address (optional but check length if provided)
        if (employee.getAddress() != null && employee.getAddress().length() > 200) {
            errors.add("Address cannot be longer than 200 characters");
        }

        return errors;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        // Allow digits, spaces, dashes, and parentheses
        // Example formats: (123) 456-7890, 123-456-7890, 1234567890
        String phoneRegex = "^[\\d\\s\\(\\)\\-]+$";
        return phone.matches(phoneRegex) &&
                phone.replaceAll("[^\\d]", "").length() >= 10;
    }
}