package io.hahnsoftware.emp.config;

import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.UserRole;

public class AuthorizationUtil {
    public static boolean canViewEmployees(User user) {
        return user.getRole() == UserRole.HR_PERSONNEL ||
               user.getRole() == UserRole.ADMINISTRATOR ||
               user.getRole() == UserRole.MANAGER;
    }
    
    public static boolean canViewEmployee(User user, Employee employee) {
        // Admins and HR can view all employees
        if (user.getRole() == UserRole.ADMINISTRATOR ||
            user.getRole() == UserRole.HR_PERSONNEL) {
            return true;
        }
        
        // Managers can only view employees in their department
        if (user.getRole() == UserRole.MANAGER) {
            return employee.getDepartment().getId().equals(user.getDepartment().getId());
        }
        
        return false;
    }
    
    public static boolean canCreateEmployee(User user) {
        return user.getRole() == UserRole.HR_PERSONNEL || 
               user.getRole() == UserRole.ADMINISTRATOR;
    }
    
    public static boolean canUpdateEmployee(User user, Employee employee) {
        // Admins and HR can update all employees
        if (user.getRole() == UserRole.ADMINISTRATOR || 
            user.getRole() == UserRole.HR_PERSONNEL) {
            return true;
        }
        
        // Managers can only update employees in their department
        if (user.getRole() == UserRole.MANAGER) {
            return employee.getDepartment().getId().equals(user.getDepartment().getId());
        }
        
        return false;
    }
    
    public static boolean canDeleteEmployee(User user) {
        return user.getRole() == UserRole.HR_PERSONNEL || 
               user.getRole() == UserRole.ADMINISTRATOR;
    }
}
