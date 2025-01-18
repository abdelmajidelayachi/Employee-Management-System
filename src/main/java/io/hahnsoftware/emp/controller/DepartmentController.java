package io.hahnsoftware.emp.controller;

import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Department management APIs")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Create a new department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Department created",
                content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = Department.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Department> createDepartment(
            @RequestBody Department department) {
        try {
            Department createdDepartment = departmentService.createDepartment(department);
            return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create department", e);
        }
    }

    @Operation(summary = "Get department by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the department",
                content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = Department.class))}),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(departmentService.getDepartmentById(id));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch department", e);
        }
    }

    @Operation(summary = "Get department by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the department"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<Department> getDepartmentByName(
            @Parameter(description = "Department name") @PathVariable String name) {
        try {
            return ResponseEntity.ok(departmentService.getDepartmentByName(name));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch department", e);
        }
    }

    @Operation(summary = "Get all departments")
    @ApiResponse(responseCode = "200", description = "List of all departments")
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        try {
            return ResponseEntity.ok(departmentService.getAllDepartments());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch departments", e);
        }
    }

    @Operation(summary = "Update department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department updated"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @RequestBody Department department) {
        try {
            department.setId(id);
            return ResponseEntity.ok(departmentService.updateDepartment(department));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update department", e);
        }
    }

    @Operation(summary = "Delete department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Department deleted"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.noContent().build();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete department", e);
        }
    }
}