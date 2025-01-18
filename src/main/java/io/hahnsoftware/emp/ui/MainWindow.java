package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dto.AuditDAO;
import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.EmploymentStatus;
import io.hahnsoftware.emp.model.UserRole;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class MainWindow extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final LoginPanel loginPanel;
    private final DashboardPanel dashboardPanel;

    
    public MainWindow() {
        setTitle("Employee Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));
        
        // Initialize layouts
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize panels
        loginPanel = new LoginPanel(this::onLoginSuccess);
        dashboardPanel = new DashboardPanel(this::onLogout);
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "login");
        mainPanel.add(dashboardPanel, "dashboard");
        
        // Show login panel by default
        cardLayout.show(mainPanel, "login");
        
        // Add to frame
        add(mainPanel);
        
        // Pack and center
        pack();
        setLocationRelativeTo(null);
    }

    private void onLoginSuccess(Employee employee) {
        dashboardPanel.setCurrentUser(employee);
        AuditDAO.setActionUser(employee);
        cardLayout.show(mainPanel, "dashboard");
    }

    public void onLogout() {

        AuditDAO.setActionUser(null);
        cardLayout.show(mainPanel, "login");
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            EmployeeDAO employeeDAO = new EmployeeDAO();
            Employee adminEmployee = employeeDAO.findByUsername("admin");
            if (adminEmployee == null) {
                Employee newAdminEmployee = new Employee();
                newAdminEmployee.setUsername("admin");
                newAdminEmployee.setFullName("Super ADMIN");
                // check the department of administrators exit
                DepartmentDAO departmentDAO = new DepartmentDAO(employeeDAO);
                Department department = departmentDAO.findByName("ADMINISTRATORS");
                if (department == null) {
                    Department adDepartment = new Department();
                    adDepartment.setName("ADMINISTRATORS");
                    department = departmentDAO.createDepartment(adDepartment, false);
                    Department managerDep = new Department();
                    managerDep.setName("Managers");
                    departmentDAO.createDepartment(adDepartment, false);
                }
                newAdminEmployee.setDepartment(department);
                newAdminEmployee.setRole(UserRole.ADMINISTRATOR);
                newAdminEmployee.setHireDate(LocalDate.now());
                newAdminEmployee.setEmployeeId("0001");
                newAdminEmployee.setJobTitle("ADMIN");
                newAdminEmployee.setEmail("admin@gmail.com");
                newAdminEmployee.setStatus(EmploymentStatus.ACTIVE);
                employeeDAO.create(newAdminEmployee, "admin", false);
                System.out.println("Admin employee created successfully.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Create and show window
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}