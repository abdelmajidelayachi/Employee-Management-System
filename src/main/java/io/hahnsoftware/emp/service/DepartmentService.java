package io.hahnsoftware.emp.service;

import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.dto.DepartmentDTO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.Employee;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentDAO departmentDAO;


    public DepartmentService() throws SQLException {
        this.departmentDAO = new DepartmentDAO(new EmployeeDAO());
    }

    public Department createDepartment(DepartmentDTO department) throws SQLException {
        if (department.managerId() == null) {
            throw new RuntimeException("Manager Id is required!"  );
        }
        if(department.name() == null) {
            throw new RuntimeException("Department name is required!");
        }
        Employee manager  = new EmployeeDAO().findManagerById(department.managerId());
        if (manager == null) {
            throw new RuntimeException("manager not found");
        }
        Department newDepartment  = new Department();
        newDepartment.setName(department.name());

        newDepartment.setManager(manager);
        return departmentDAO.createDepartment(newDepartment, true);
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

    public Department updateDepartment(DepartmentDTO departmentDTO, Long id) throws SQLException {

        if (departmentDTO.managerId() == null) {
            throw new RuntimeException("Manager Id is required!"  );
        }
        if(departmentDTO.name() == null) {
            throw new RuntimeException("Department name is required!");
        }
        Employee manager  = new EmployeeDAO().findManagerById(departmentDTO.managerId());
        if (manager == null) {
            throw new RuntimeException("manager not found");
        }
        Department department = new Department();
        department.setManager(manager);
        department.setId(id);
        department.setName(departmentDTO.name());
        departmentDAO.updateDepartment(department);
        return getDepartmentById(department.getId());
    }

    public void deleteDepartment(Long id) throws SQLException {
        departmentDAO.deleteDepartment(id);
    }
}