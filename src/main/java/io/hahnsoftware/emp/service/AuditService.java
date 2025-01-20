package io.hahnsoftware.emp.service;

import io.hahnsoftware.emp.dao.AuditDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.AuditLog;
import io.hahnsoftware.emp.model.Employee;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditDAO auditDAO;

    public AuditService() throws SQLException {
        this.auditDAO = new AuditDAO(new EmployeeDAO());
    }

    public void logAction(
            @RequestParam("action") String action,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("changes") String changes) throws SQLException {
        auditDAO.logAction(action, entityType, entityId, employeeId, changes);
    }

    public List<AuditLog> getAuditLogs(
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) LocalDateTime endDate) throws SQLException {
        return auditDAO.getAuditLogs(entityType, action, startDate, endDate);
    }

    public void setCurrentActionUser(@RequestParam("employee") Employee employee) {
        AuditDAO.setActionUser(employee);
    }

    public Employee getCurrentActionUser() {
        return AuditDAO.getActionUser();
    }
}