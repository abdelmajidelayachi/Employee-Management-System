package io.hahnsoftware.emp.controller;

import io.hahnsoftware.emp.dto.EmployeeDto;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.SearchCriteria;
import io.hahnsoftware.emp.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing employees")
@SecurityRequirement(name = "bearer-jwt")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(
            @RequestBody EmployeeDto employee,
            @RequestParam String password) {
        try {
            return ResponseEntity.ok(employeeService.createEmployee(employee, password));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(employeeService.getEmployeeById(id));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch employee", e);
        }
    }

    @Operation(summary = "Get all employees")
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            return ResponseEntity.ok(employeeService.getAllEmployees());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    @Operation(summary = "Get all managers")
    @GetMapping("/managers")
    public ResponseEntity<List<Employee>> getAllManagers() {
        try {
            return ResponseEntity.ok(employeeService.getAllManagers());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch managers", e);
        }
    }

    @Operation(summary = "Search employees")
    @PostMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestBody SearchCriteria criteria) {
        try {
            return ResponseEntity.ok(employeeService.searchEmployees(criteria));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search employees", e);
        }
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeDto employee) {
        try {
            return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee", e);
        }
    }

    @Operation(summary = "Update employee password")
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        try {
            employeeService.updatePassword(id, newPassword);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password", e);
        }
    }

    @Operation(summary = "Delete employee")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        try {
            employeeService.deleteEmployee(employeeId);
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete employee", e);
        }
    }
}