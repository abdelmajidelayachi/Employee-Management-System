package io.hahnsoftware.emp.controller;

import io.hahnsoftware.emp.dto.AuditDAO;
import io.hahnsoftware.emp.model.AuditLog;
import io.hahnsoftware.emp.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<AuditLog> logs = auditService.getAuditLogs(entityType, action, startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch audit logs", e);
        }
    }
}