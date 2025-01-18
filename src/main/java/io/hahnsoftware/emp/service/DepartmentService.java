package io.hahnsoftware.emp.service;

import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.model.Department;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentDAO departmentDAO;


    public DepartmentService() throws SQLException {
        this.departmentDAO = new DepartmentDAO(new EmployeeDAO());
    }

    public Department createDepartment(Department department) throws SQLException {
        // Add validation logic if needed
        return departmentDAO.createDepartment(department, true);
    }

    public Department getDepartmentById(Long id) throws SQLException {
        Department department = departmentDAO.findById(id);
        if (department == null) {
            throw new RuntimeException("Department not found with id: " + id);
        }
        return department;
    }

    public Department getDepartmentByName(String name) throws SQLException {
        Department department = departmentDAO.findByName(name);
        if (department == null) {
            throw new RuntimeException("Department not found with name: " + name);
        }
        return department;
    }

    public List<Department> getAllDepartments() throws SQLException {
        return departmentDAO.findAll();
    }

    public Department updateDepartment(Department department) throws SQLException {
        departmentDAO.updateDepartment(department);
        return getDepartmentById(department.getId());
    }

    public void deleteDepartment(Long id) throws SQLException {
        departmentDAO.deleteDepartment(id);
    }
}