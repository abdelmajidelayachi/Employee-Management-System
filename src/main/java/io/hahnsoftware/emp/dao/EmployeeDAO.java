package io.hahnsoftware.emp.dao;


import io.hahnsoftware.emp.dto.EmployeeDto;
import io.hahnsoftware.emp.model.*;
import io.hahnsoftware.emp.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private final Connection connection;
    private final DepartmentDAO departmentDAO;
    private final AuditDAO auditDAO;


    public EmployeeDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.auditDAO = new AuditDAO(this);
        this.departmentDAO = new DepartmentDAO(this);
    }

    public Employee create(Employee employee, String password, boolean isLogAuditEnabled) throws SQLException {
        String sql = "INSERT INTO employees (employee_id, full_name, username, password_hash, role, job_title, department_id, " +
                "hire_date, status, email, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"ID"})) {
            // Build changes log while setting parameters
            StringBuilder changes = new StringBuilder("Created employee with details:\n");

            stmt.setString(1, employee.getEmployeeId());
            changes.append("Employee ID: ").append(employee.getEmployeeId()).append("\n");

            stmt.setString(2, employee.getFullName());
            changes.append("Name: ").append(employee.getFullName()).append("\n");

            stmt.setString(3, employee.getUsername());
            changes.append("Username: ").append(employee.getUsername()).append("\n");

            stmt.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
            changes.append("Password: ").append("[Hidden]").append("\n");

            stmt.setString(5, employee.getRole().name());
            changes.append("Role: ").append(employee.getRole().name()).append("\n");

            stmt.setString(6, employee.getJobTitle());
            changes.append("Job Title: ").append(employee.getJobTitle()).append("\n");

            stmt.setLong(7, employee.getDepartment().getId());
            changes.append("Department: ").append(employee.getDepartment().getName()).append("\n");

            stmt.setDate(8, Date.valueOf(employee.getHireDate()));
            changes.append("Hire Date: ").append(employee.getHireDate()).append("\n");

            stmt.setString(9, employee.getStatus().name());
            changes.append("Status: ").append(employee.getStatus().name()).append("\n");

            stmt.setString(10, employee.getEmail());
            changes.append("Email: ").append(employee.getEmail()).append("\n");

            stmt.setString(11, employee.getPhone());
            changes.append("Phone: ").append(employee.getPhone()).append("\n");

            stmt.setString(12, employee.getAddress());
            changes.append("Address: ").append(employee.getAddress());

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

            // Log the action with the collected changes
            if (isLogAuditEnabled) {
                auditDAO.logAction(
                        AuditDAO.ActionAudit.CREATE.name(),
                        AuditDAO.Entities.EMPLOYEE.name(),
                        employee.getId(),
                        AuditDAO.getActionUser().getId(),
                        changes.toString()
                );
            }

            return employee;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }



    public Employee findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM employees WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }

        return null;
    }

    public Employee validateCredentials(String username, String password) throws SQLException {
        Employee employee = findByUsername(username);

        if (employee != null && BCrypt.checkpw(password, employee.getPasswordHash())) {
            return employee;
        }

        return null;
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

    public List<Employee> findAllManagers() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees where role='" + UserRole.MANAGER+"'" ;

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
        employee.setUsername(rs.getString("username"));
        employee.setPasswordHash(rs.getString("password_hash"));
        employee.setRole(UserRole.valueOf(rs.getString("role")));
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
            Employee manager = employee.getDepartment().getManager();
            // check if the current is manager
            if (AuditDAO.getActionUser() != null && manager != null && manager.getId() == AuditDAO.getActionUser().getId()) {
                employee.setCurrentUserManager(true);
            }
        }

        return employee;
    }
    public Employee update(Employee employee) throws SQLException {
        // First, get the old state of the employee
        Employee oldEmployee = findById(employee.getId());
        if (oldEmployee == null) {
            throw new SQLException("Employee not found for update.");
        }

        String sql = "UPDATE employees SET " +
                "employee_id = ?, " +
                "full_name = ?, " +
                "job_title = ?, " +
                "department_id = ?, " +
                "hire_date = ?, " +
                "status = ?, " +
                "email = ?, " +
                "phone = ?, " +
                "address = ?, " +
                "username = ?, " +
                "role = ? " +
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
            stmt.setString(10, employee.getUsername());
            stmt.setString(11, employee.getRole().name());
            stmt.setLong(12, employee.getId());

            // add log audit
            StringBuilder changes = new StringBuilder("Updated fields:\n");
            trackChange(changes, "Employee ID", oldEmployee.getEmployeeId(), employee.getEmployeeId());
            trackChange(changes, "Name", oldEmployee.getFullName(), employee.getFullName());
            trackChange(changes, "Job Title", oldEmployee.getJobTitle(), employee.getJobTitle());
            trackChange(changes, "Department", oldEmployee.getDepartment().getName(), employee.getDepartment().getName());
            trackChange(changes, "Hire Date", oldEmployee.getHireDate().toString(), employee.getHireDate().toString());
            trackChange(changes, "Status", oldEmployee.getStatus().name(), employee.getStatus().name());
            trackChange(changes, "Email", oldEmployee.getEmail(), employee.getEmail());
            trackChange(changes, "Phone", oldEmployee.getPhone(), employee.getPhone());
            trackChange(changes, "Address", oldEmployee.getAddress(), employee.getAddress());
            trackChange(changes, "Username", oldEmployee.getUsername(), employee.getUsername());
            trackChange(changes, "Role", oldEmployee.getRole().name(), employee.getRole().name());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Update failed, no rows affected.");
            }

            connection.commit();

            // Log changes only if there were actual changes
            if (changes.length() > "Updated fields:\n".length()) {
                auditDAO.logAction(
                        AuditDAO.ActionAudit.UPDATE.name(),
                        AuditDAO.Entities.EMPLOYEE.name(),
                        employee.getId(),
                        AuditDAO.getActionUser().getId(),
                        changes.toString()
                );
            }

            return findById(employee.getId());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void updatePassword(Employee employee, String newPassword) throws SQLException {
        String sql = "UPDATE employees SET password_hash = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            stmt.setLong(2, employee.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Password update failed, no rows affected.");
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    // try to log only the changed fields
    private void trackChange(StringBuilder changes, String fieldName, String oldValue, String newValue) {
        if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
            changes.append(fieldName).append(": ")
                    .append(oldValue)
                    .append(" → ")
                    .append(newValue)
                    .append("\n");
        }
    }

    public void delete(String id) throws SQLException {
        // Get employee details before deletion for audit log
        Employee employee = findByEmployeeId(id);
        if (employee == null) {
            throw new SQLException("Employee not found for deletion.");
        }

        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Build deletion details
            StringBuilder changes = new StringBuilder("Deleted employee details:\n")
                    .append("Employee ID: ").append(employee.getEmployeeId()).append("\n")
                    .append("Name: ").append(employee.getFullName()).append("\n")
                    .append("Job Title: ").append(employee.getJobTitle()).append("\n")
                    .append("Department: ").append(employee.getDepartment().getName()).append("\n")
                    .append("Status: ").append(employee.getStatus().name()).append("\n");

            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Delete failed, no rows affected.");
            }

            connection.commit();

            // Log the deletion with details
            auditDAO.logAction(
                    AuditDAO.ActionAudit.DELETE.name(),
                    AuditDAO.Entities.EMPLOYEE.name(),
                    employee.getId(),
                    AuditDAO.getActionUser().getId(),
                    changes.toString()
            );
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

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    // validate all the formate of of phone
    public static boolean isValidPhone(String phone) {
        String phoneRegex = "^[\\d\\s\\(\\)\\-]+$";
        return phone.matches(phoneRegex) &&
                phone.replaceAll("[^\\d]", "").length() >= 10;
    }

    public Employee findManagerById(Long managerId) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, managerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToManager(rs);
                }
            }
        }

        return null;
    }
    private Employee mapResultSetToManager(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setEmployeeId(rs.getString("employee_id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setUsername(rs.getString("username"));
        employee.setPasswordHash(rs.getString("password_hash"));
        employee.setRole(UserRole.valueOf(rs.getString("role")));
        employee.setJobTitle(rs.getString("job_title"));
        employee.setHireDate(rs.getDate("hire_date").toLocalDate());
        employee.setStatus(EmploymentStatus.valueOf(rs.getString("status")));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));

        return employee;
    }

}