package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dao.AuditDAO;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.UserRole;

public class PermissionUtils {
    // can view, edit, update, delete features
    public static boolean canAccessAll() {
        return AuditDAO.getActionUser() != null && AuditDAO.getActionUser().getRole().equals(UserRole.ADMINISTRATOR);
    }

    // can edit and view employee only
    public static boolean canAccessViewAndEditEmployee(Employee employeeTarget) {
        return canAccessAll() || AuditDAO.getActionUser() != null && AuditDAO.getActionUser().getRole().equals(UserRole.MANAGER) && employeeTarget.isCurrentUserManager();
    }

    public static boolean canAccessAllEmployee() {
        return canAccessAll() || AuditDAO.getActionUser() != null && AuditDAO.getActionUser().getRole().equals(UserRole.ADMINISTRATOR) || AuditDAO.getActionUser() != null && AuditDAO.getActionUser().getRole().equals(UserRole.HR_PERSONNEL);
    }

    public static boolean canViewEmployeePanel() {
        return AuditDAO.getActionUser() != null && AuditDAO.getActionUser().getRole().equals(UserRole.MANAGER);
    }

    public static boolean canViewLogAudit() {
        return canAccessAll() || AuditDAO.getActionUser() != null && AuditDAO.getActionUser().getRole().equals(UserRole.ADMINISTRATOR);
    }
}
