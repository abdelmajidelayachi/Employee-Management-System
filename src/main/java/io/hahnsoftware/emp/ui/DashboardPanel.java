package io.hahnsoftware.emp.ui;

import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.UserRole;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {
    // Enhanced color scheme
    private static final Color MAIN_COLOR = new Color(57, 145, 169);     // #3991a9
    private static final Color MAIN_DARK = new Color(41, 128, 152);      // Darker variant
    private static final Color SIDEBAR_COLOR = MAIN_COLOR;    // Darker, more professional
    private static final Color HOVER_COLOR = new Color(41, 51, 61);      // Slightly lighter than sidebar
    private static final Color ACTIVE_COLOR = MAIN_COLOR;                // Active menu item color
    private static final Color BG_COLOR = MAIN_COLOR;      // Light gray background
    private static final Color BORDER_COLOR = new Color(230, 230, 230);  // Border color

    private final JPanel contentPanel;
    private final CardLayout contentLayout;
    private User currentUser;
    private String currentPanel = "";
    private final Map<String, JButton> menuButtons = new HashMap<>();

    private final EmployeeListPanel employeeListPanel;
    private final EmployeeFormPanel employeeFormPanel;
    private final UserManagementPanel userManagementPanel;
    private final DepartmentManagementPanel departmentManagementPanel;
    private final AuditLogPanel auditLogPanel;

    public DashboardPanel(Runnable onLogout) {
        setLayout(new MigLayout("fill, insets 0", "[270!][grow]", "[70!][grow]"));
        setBackground(BG_COLOR);

        // Initialize content panels
        employeeListPanel = new EmployeeListPanel(this::showEmployeeForm);
        employeeFormPanel = new EmployeeFormPanel(this::showEmployeeList);
        userManagementPanel = new UserManagementPanel();
        departmentManagementPanel = new DepartmentManagementPanel();
        auditLogPanel = new AuditLogPanel();

        // Create content panel with card layout
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(BG_COLOR);

        // Add panels to content area
        contentPanel.add(employeeListPanel, "employeeList");
        contentPanel.add(employeeFormPanel, "employeeForm");
        contentPanel.add(userManagementPanel, "userManagement");
        contentPanel.add(departmentManagementPanel, "departmentManagement");
        contentPanel.add(auditLogPanel, "auditLog");

        // Create header and sidebar
        JPanel headerPanel = createHeader();
        JPanel sidebar = createModernSidebar(onLogout);

        // Create content wrapper with shadow
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BG_COLOR);
        contentWrapper.setBorder(createContentBorder());
        contentWrapper.add(contentPanel, BorderLayout.CENTER);

        // Add all components
        add(headerPanel, "span 2, growx, wrap");
        add(sidebar, "cell 0 1, grow");
        add(contentWrapper, "cell 1 1, grow");

        // Show initial panel
        showPanel("employeeList");
    }

    private Border createContentBorder() {
        return BorderFactory.createCompoundBorder(
                new EmptyBorder(20, 20, 20, 20),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 15 25", "[grow][]")) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Add subtle gradient
                GradientPaint gp = new GradientPaint(0, 0, MAIN_COLOR, 0, getHeight(), MAIN_DARK);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👥");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel("Employee Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel, "cell 0 0");

        // User info section
        if (currentUser != null) {
            JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            userPanel.setOpaque(false);

            JLabel userIcon = new JLabel("👤");
            userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            userIcon.setForeground(Color.WHITE);

            JLabel userLabel = new JLabel(currentUser.getUsername());
            userLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            userLabel.setForeground(Color.WHITE);

            userPanel.add(userIcon);
            userPanel.add(userLabel);
            headerPanel.add(userPanel, "cell 1 0");
        }

        return headerPanel;
    }
    private JPanel createModernSidebar(Runnable onLogout) {
        JPanel sidebar = new JPanel(new MigLayout("fillx, wrap 1, insets 20 0", "[grow]", "[]10[]10[]10[]push[]"));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setForeground(MAIN_DARK);

        // Menu buttons
        addMenuButton(sidebar, "Employees", "👥", "employeeList");
        addMenuButton(sidebar, "Users", "👤", "userManagement");
        addMenuButton(sidebar, "Departments", "🏢", "departmentManagement");
        addMenuButton(sidebar, "Audit Log", "📋", "auditLog");

        // Logout button with special styling
        JButton logoutBtn = createModernMenuButton("Logout", "🚪", onLogout);
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);  // Set initial text color
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutBtn.setBackground(new Color(231, 76, 60).brighter());
                logoutBtn.setForeground(Color.WHITE);  // Keep text white on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutBtn.setBackground(new Color(231, 76, 60));
                logoutBtn.setForeground(Color.WHITE);  // Keep text white on exit
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                logoutBtn.setBackground(new Color(231, 76, 60).darker());
                logoutBtn.setForeground(Color.WHITE);  // Keep text white when pressed
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                logoutBtn.setBackground(new Color(231, 76, 60));
                logoutBtn.setForeground(Color.WHITE);  // Keep text white when released
            }
        });
        sidebar.add(logoutBtn, "growx, bottom");

        return sidebar;
    }
    private void addMenuButton(JPanel sidebar, String text, String icon, String panelName) {
        JButton button = createModernMenuButton(text, icon, () -> showPanel(panelName));
        menuButtons.put(panelName, button);
        sidebar.add(button, "growx");
    }

    private JButton createModernMenuButton(String text, String icon, Runnable action) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add action and hover effects
        button.addActionListener(e -> action.run());
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(ACTIVE_COLOR)) {
                    button.setBackground(HOVER_COLOR);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(ACTIVE_COLOR)) {
                    button.setBackground(SIDEBAR_COLOR);
                }
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(ACTIVE_COLOR)) {
                    button.setBackground(HOVER_COLOR.darker());
                }
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(ACTIVE_COLOR)) {
                    button.setBackground(HOVER_COLOR);
                }
            }
        });

        return button;
    }

    private void showPanel(String panelName) {
        // Reset previous button style
        if (!currentPanel.isEmpty() && menuButtons.containsKey(currentPanel)) {
            JButton prevButton = menuButtons.get(currentPanel);
            prevButton.setBackground(SIDEBAR_COLOR);
        }

        // Set new button style
        if (menuButtons.containsKey(panelName)) {
            JButton currentButton = menuButtons.get(panelName);
            currentButton.setBackground(ACTIVE_COLOR);
        }

        // Update current panel and show it
        currentPanel = panelName;
        contentLayout.show(contentPanel, panelName);

        // Refresh data based on panel type
        switch (panelName) {
            case "employeeList" -> employeeListPanel.refreshData();
            case "userManagement" -> userManagementPanel.refreshData();
            case "departmentManagement" -> departmentManagementPanel.refreshData();
            case "auditLog" -> auditLogPanel.refreshData();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateAccess();
        // Refresh UI to show user info
        removeAll();
        setLayout(new MigLayout("fill, insets 0", "[270!][grow]", "[70!][grow]"));
        add(createHeader(), "span 2, growx, wrap");
        add(createModernSidebar(() -> {}), "cell 0 1, grow");
        add(contentPanel, "cell 1 1, grow");
        revalidate();
        repaint();
    }

    private void updateAccess() {
        boolean isAdmin = currentUser.getRole() == UserRole.ADMINISTRATOR;
        menuButtons.forEach((key, button) -> {
            switch (key) {
                case "userManagement", "departmentManagement", "auditLog" -> button.setVisible(isAdmin);
            }
        });
    }

    private void showEmployeeList() {
        showPanel("employeeList");
    }

    private void showEmployeeForm() {
        showPanel("employeeForm");
    }
}