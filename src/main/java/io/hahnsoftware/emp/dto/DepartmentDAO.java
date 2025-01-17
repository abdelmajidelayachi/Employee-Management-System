package io.hahnsoftware.emp.dto;

import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
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

    public Department createDepartment(Department department) throws SQLException {
        String sql = "INSERT INTO departments (name, manager_id) VALUES (?, ?)";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql, new String[] {"id"})) {
            stmt.setString(1, department.getName());
            if (department.getManager() != null) {
                stmt.setLong(2, department.getManager().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    department.setId(generatedKeys.getLong(1));
                }
            }

            DatabaseConnection.getConnection().commit();
            return department;
        } catch (SQLException e) {
            DatabaseConnection.getConnection().rollback();
            throw e;
        }
    }

    public void updateDepartment(Department department) throws SQLException {
        String sql = "UPDATE departments SET name = ?, manager_id = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, department.getName());
            if (department.getManager() != null) {
                stmt.setLong(2, department.getManager().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setLong(3, department.getId());

            stmt.executeUpdate();
            DatabaseConnection.getConnection().commit();
        } catch (SQLException e) {
            DatabaseConnection.getConnection().rollback();
            throw e;
        }
    }

    public void deleteDepartment(Long id) throws SQLException {
        String sql = "DELETE FROM departments WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);

            stmt.executeUpdate();
            DatabaseConnection.getConnection().commit();
        } catch (SQLException e) {
            DatabaseConnection.getConnection().rollback();
            throw e;
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
            Employee manager = new Employee();
            manager.setId(managerId);
            department.setManager(manager);
        }

        return department;
    }
}