package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.dto.UserDAO;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.UserRole;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

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
    
    private void onLoginSuccess(User user) {
        dashboardPanel.setCurrentUser(user);
        cardLayout.show(mainPanel, "dashboard");
    }
    
    private void onLogout() {
        cardLayout.show(mainPanel, "login");
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create admin user if it doesn't exist
        try {
            UserDAO userDAO = new UserDAO();
            User adminUser = userDAO.findByUsername("admin");
            if (adminUser == null) {
                User newAdminUser = new User();
                newAdminUser.setUsername("admin");
                newAdminUser.setRole(UserRole.ADMINISTRATOR);
                userDAO.createUser(newAdminUser, "admin");
                System.out.println("Admin user created successfully.");
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