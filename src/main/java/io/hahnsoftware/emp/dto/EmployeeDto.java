package io.hahnsoftware.emp.dto;

import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.EmploymentStatus;
import io.hahnsoftware.emp.model.UserRole;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.hahnsoftware.emp.dao.EmployeeDAO.isValidEmail;
import static io.hahnsoftware.emp.dao.EmployeeDAO.isValidPhone;

public class EmployeeDto {
    private final String employeeId;
    private final String fullName;
    private final String jobTitle;
    private final Long departmentId;
    private final LocalDate hireDate;
    private final String username;
    private final String passwordHash;
    private final UserRole role;
    private final EmploymentStatus status;
    private final String email;
    private final String phone;
    private final String address;

    private final DepartmentDAO departmentDAO;

    public EmployeeDto(String employeeId, String fullName, String jobTitle, Long departmentId,
                       LocalDate hireDate, String username, String passwordHash, UserRole role,
                       EmploymentStatus status, String email, String phone, String address) throws SQLException {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.departmentId = departmentId;
        this.hireDate = hireDate;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.departmentDAO = new DepartmentDAO(new EmployeeDAO());
    }

    public List<String> validateEmployeeDto() {
        List<String> errors = new ArrayList<>();

        // Validate Employee ID
        if (employeeId == null || employeeId.trim().isEmpty()) {
            errors.add("Employee ID is required");
        } else if (employeeId.length() > 20) {
            errors.add("Employee ID cannot be longer than 20 characters");
        }

        // Validate Full Name
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.add("Full name is required");
        } else if (fullName.length() > 100) {
            errors.add("Full name cannot be longer than 100 characters");
        }

        // Validate Job Title
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            errors.add("Job title is required");
        } else if (jobTitle.length() > 100) {
            errors.add("Job title cannot be longer than 100 characters");
        }

        // Validate Department
        if (departmentId == null) {
            errors.add("Department is required");
        }

        // Validate Hire Date
        if (hireDate == null) {
            errors.add("Hire date is required");
        } else if (hireDate.isAfter(LocalDate.now())) {
            errors.add("Hire date cannot be in the future");
        }

        // Validate Employment Status
        if (status == null) {
            errors.add("Employment status is required");
        }

        // Validate Email
        if (email == null || email.trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!isValidEmail(email)) {
            errors.add("Invalid email format");
        } else if (email.length() > 100) {
            errors.add("Email cannot be longer than 100 characters");
        }

        // Validate Phone (optional but must be valid if provided)
        if (phone != null && !phone.trim().isEmpty()) {
            if (!isValidPhone(phone)) {
                errors.add("Invalid phone number format");
            } else if (phone.length() > 20) {
                errors.add("Phone number cannot be longer than 20 characters");
            }
        }

        // Validate Address (optional but check length if provided)
        if (address != null && address.length() > 200) {
            errors.add("Address cannot be longer than 200 characters");
        }

        return errors;
    }

    // Convert DTO to Entity
    public Employee convertToEntity() throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFullName(fullName);
        employee.setJobTitle(jobTitle);

        // Get department by ID
        Department department = departmentDAO.findById(departmentId);
        if (department == null) {
            throw new IllegalArgumentException("Department not found with id: " + departmentId);
        }
        employee.setDepartment(department);

        employee.setHireDate(hireDate);
        employee.setUsername(username);
        employee.setPasswordHash(passwordHash);
        employee.setRole(role);
        employee.setStatus(status);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setAddress(address);

        return employee;
    }

    // Getters and setters
    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}