package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dao.AuditDAO;
import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
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
    private DashboardPanel dashboardPanel; // Remove final modifier

    public MainWindow() {
        setTitle("Employee Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 768));

        // Initialize layouts
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize only login panel
        loginPanel = new LoginPanel(this::onLoginSuccess);

        // Add only login panel to card layout initially
        mainPanel.add(loginPanel, "login");

        // Show login panel by default
        cardLayout.show(mainPanel, "login");

        // Add to frame
        add(mainPanel);

        // Pack and center
        pack();
        setLocationRelativeTo(null);
    }

    private void onLoginSuccess(Employee employee) {
        // Initialize dashboard panel only after successful login
        AuditDAO.setActionUser(employee);
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(this::onLogout);
            dashboardPanel.setCurrentUser();
            mainPanel.add(dashboardPanel, "dashboard");
        }

        cardLayout.show(mainPanel, "dashboard");
    }

    public void onLogout() {
        AuditDAO.setActionUser(null);

        // Remove and cleanup dashboard
        if (dashboardPanel != null) {
            mainPanel.remove(dashboardPanel);
            dashboardPanel = null;
        }

        cardLayout.show(mainPanel, "login");

        // Clear login form if needed
        loginPanel.clearForm();

        // Trigger garbage collection for cleanup (optional)
        System.gc();
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