package io.hahnsoftware.emp.controller;

import io.hahnsoftware.emp.model.AuditLog;
import io.hahnsoftware.emp.service.AuditService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@SecurityRequirement(name = "bearer-jwt")
public class AuditController {
    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate) {
        try {
            // Convert LocalDate to LocalDateTime for midnight start and end of day
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

            List<AuditLog> logs = auditService.getAuditLogs(entityType, action, startDateTime, endDateTime);
            return ResponseEntity.ok(logs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch audit logs", e);
        }
    }
}