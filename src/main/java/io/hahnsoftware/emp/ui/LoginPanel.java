package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.Employee;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private final EmployeeDAO employeeDAO;
    private final Consumer<Employee> onLoginSuccess;

    public LoginPanel(Consumer<Employee> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        try {
            employeeDAO = new EmployeeDAO();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UserDAO", e);
        }

        setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        setBackground(StyleConstants.MAIN_COLOR);

        // Create main container
        JPanel container = createMainContainer();

        // Create wrapper with horizontal centering
        JPanel wrapperPanel = new JPanel(new MigLayout("fill, insets 0", "[center]", "[center]"));
        wrapperPanel.setOpaque(false);

        // Add container with fixed width
        JPanel fixedWidthPanel = new JPanel(new MigLayout("fill, insets 0", "[400!]", "[]"));
        fixedWidthPanel.setOpaque(false);
        fixedWidthPanel.add(container, "grow");

        wrapperPanel.add(fixedWidthPanel, "center");
        add(wrapperPanel, "grow");

        setupActionListeners();
    }

    private JPanel createMainContainer() {
        JPanel container = new JPanel(new MigLayout("wrap 1, fillx, insets 40", "[grow]", "[]30[]"));
        container.setBackground(StyleConstants.BG_SECONDARY);
        container.setBorder(createShadowBorder());

        // Add logo/title section
        container.add(createTitlePanel(), "growx");

        // Create and populate login form
        JPanel formPanel = createLoginForm();

        // Initialize form components
        usernameField = createStyledTextField("Username");
        passwordField = createStyledPasswordField();
        loginButton = createStyledButton("Login");

        // Add components to form
        formPanel.add(usernameField, "growx, h 40!");
        formPanel.add(passwordField, "growx, h 40!");
        formPanel.add(loginButton, "growx, h 45!");

        container.add(formPanel, "growx");

        return container;
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new MigLayout("wrap 1, fillx", "[center]", "[]10[]10[]"));
        titlePanel.setBackground(StyleConstants.BG_SECONDARY);

        // Logo/Icon
        JLabel iconLabel = new JLabel("👥");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 56));
        iconLabel.setForeground(StyleConstants.MAIN_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Employee Management");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 26));
        titleLabel.setForeground(StyleConstants.TEXT_PRIMARY);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Login to your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(StyleConstants.TEXT_SECONDARY);

        titlePanel.add(iconLabel, "align center");
        titlePanel.add(titleLabel, "align center");
        titlePanel.add(subtitleLabel, "align center");

        return titlePanel;
    }

    private JPanel createLoginForm() {
        JPanel formPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 0", "[grow]", "[]15[]25[]"));
        formPanel.setBackground(StyleConstants.BG_SECONDARY);
        return formPanel;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        styleTextField(field);
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.putClientProperty("JTextField.placeholderText", "Password");
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER_LIGHT, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(StyleConstants.BG_SECONDARY);
        field.setForeground(StyleConstants.TEXT_PRIMARY);
        field.setCaretColor(StyleConstants.MAIN_COLOR);

        // Enhanced focus effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(StyleConstants.MAIN_COLOR, 2),
                        new EmptyBorder(7, 12, 7, 12)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(StyleConstants.BORDER_LIGHT, 1),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        button.setForeground(StyleConstants.TEXT_PRIMARY);  // Changed to white text
        button.setBackground(StyleConstants.MAIN_COLOR);   // Use main color for default state
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Enhanced hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(StyleConstants.MAIN_LIGHTER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(StyleConstants.MAIN_COLOR);
                }
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(StyleConstants.MAIN_DARKER);
                }
            }
        });

        return button;
    }
    private javax.swing.border.Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(5, 5, 5, 5),
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1)
                ),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(StyleConstants.BORDER_LIGHT, 1),
                        new EmptyBorder(0, 0, 0, 0)
                )
        );
    }

    private void setupActionListeners() {
        usernameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Employee employee = employeeDAO.validateCredentials(username, password);
            if (employee != null) {
                onLoginSuccess.accept(employee);
                clearForm();
            } else {
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            showError("Error during login: " + e.getMessage());
        } finally {
            loginButton.setEnabled(true);
            loginButton.setText("Login");
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
    }

    void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }
}