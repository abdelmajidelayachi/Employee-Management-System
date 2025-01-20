package io.hahnsoftware.emp.dao;

import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
    private final AuditDAO auditDAO;
    private final EmployeeDAO employeeDAO;

    public DepartmentDAO(EmployeeDAO employeeDAO) {
        try {
            this.employeeDAO = employeeDAO;
            this.auditDAO = new AuditDAO(employeeDAO);
        } catch (SQLException e) {
            throw new RuntimeException("error initialization of the UserDAO");
        }
    }

    public Department findById(Long id) throws SQLException {
        String sql = "SELECT id, name, manager_id FROM departments WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        }

        return null;
    }

    public List<Department> findAll() throws SQLException {
        String sql = "SELECT id, name, manager_id FROM departments";
        List<Department> departmentList = new ArrayList<>();

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Department department = mapResultSetToDepartment(rs);
                departmentList.add(department);
            }
        }

        return departmentList;
    }


    public Department createDepartment(Department department, boolean isAuditLogEnabled) throws SQLException {
        String sql = "INSERT INTO departments (name, manager_id) VALUES (?, ?)";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql, new String[] {"id"})) {
            // Build changes log
            StringBuilder changes = new StringBuilder("Created department with details:\n");

            stmt.setString(1, department.getName());
            changes.append("Name: ").append(department.getName()).append("\n");

            if (department.getManager() != null) {
                stmt.setLong(2, department.getManager().getId());
                changes.append("Manager: ").append(department.getManager().getFullName());
            } else {
                stmt.setNull(2, Types.BIGINT);
                changes.append("Manager: None");
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    department.setId(generatedKeys.getLong(1));
                }
            }

            DatabaseConnection.getConnection().commit();

            // Log the creation
            if (isAuditLogEnabled) {
                auditDAO.logAction(
                        AuditDAO.ActionAudit.CREATE.name(),
                        AuditDAO.Entities.DEPARTMENT.name(),
                        department.getId(),
                        AuditDAO.getActionUser().getId(),
                        changes.toString()
                );
            }

            return department;
        } catch (SQLException e) {
            DatabaseConnection.getConnection().rollback();
            throw e;
        }
    }

    public void updateDepartment(Department department) throws SQLException {
        // Get old state for comparison
        Department oldDepartment = findById(department.getId());
        if (oldDepartment == null) {
            throw new SQLException("Department not found for update.");
        }

        String sql = "UPDATE departments SET name = ?, manager_id = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, department.getName());
            if (department.getManager() != null) {
                stmt.setLong(2, department.getManager().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setLong(3, department.getId());

            // Track changes
            StringBuilder changes = new StringBuilder("Updated fields:\n");
            trackChange(changes, "Name", oldDepartment.getName(), department.getName());
            trackChange(changes, "Manager",
                    oldDepartment.getManager() != null ? oldDepartment.getManager().getFullName() : "None",
                    department.getManager() != null ? department.getManager().getFullName() : "None"
            );

            stmt.executeUpdate();
            DatabaseConnection.getConnection().commit();

            // Log changes only if there were actual changes
            if (changes.length() > "Updated fields:\n".length()) {
                auditDAO.logAction(
                        AuditDAO.ActionAudit.UPDATE.name(),
                        AuditDAO.Entities.DEPARTMENT.name(),
                        department.getId(),
                        AuditDAO.getActionUser().getId(),
                        changes.toString()
                );
            }
        } catch (SQLException e) {
            DatabaseConnection.getConnection().rollback();
            throw e;
        }
    }

    public void deleteDepartment(Long id) throws SQLException {
        // Get department details before deletion
        Department department = findById(id);
        if (department == null) {
            throw new SQLException("Department not found for deletion.");
        }

        String sql = "DELETE FROM departments WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            // Build deletion details
            StringBuilder changes = new StringBuilder("Deleted department details:\n")
                    .append("Name: ").append(department.getName()).append("\n")
                    .append("Manager: ").append(department.getManager() != null ?
                            department.getManager().getFullName() : "None");

            stmt.setLong(1, id);
            stmt.executeUpdate();
            DatabaseConnection.getConnection().commit();

            // Log the deletion
            auditDAO.logAction(
                    AuditDAO.ActionAudit.DELETE.name(),
                    AuditDAO.Entities.DEPARTMENT.name(),
                    department.getId(),
                    AuditDAO.getActionUser().getId(),
                    changes.toString()
            );
        } catch (SQLException e) {
            DatabaseConnection.getConnection().rollback();
            throw e;
        }
    }

    // Helper method for tracking changes
    private void trackChange(StringBuilder changes, String fieldName, String oldValue, String newValue) {
        if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
            changes.append(fieldName).append(": ")
                    .append(oldValue)
                    .append(" → ")
                    .append(newValue)
                    .append("\n");
        }
    }

    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getLong("id"));
        department.setName(rs.getString("name"));

        // Handle manager_id which might be null
        Long managerId = rs.getLong("manager_id");
        if (!rs.wasNull()) {
            // If you need the manager details, you could load them here
            // or create a separate method to load them when needed
            Employee manager = employeeDAO.findManagerById(managerId);
            department.setManager(manager);
        }

        return department;
    }

    public Department findByName(String name) throws SQLException {
        String sql = "SELECT id, name, manager_id FROM departments WHERE name= ?";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        }

        return null;
    }
}