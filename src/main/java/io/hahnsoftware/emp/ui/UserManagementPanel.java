package io.hahnsoftware.emp.ui;

import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.dto.UserDAO;
import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.UserRole;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private static final Color MAIN_COLOR = new Color(57, 145, 169); // #3991a9
    private static final Color LIGHTER_MAIN = new Color(87, 175, 199);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color TABLE_HEADER_COLOR = new Color(51, 51, 51);
    private static final Color TABLE_STRIPE_COLOR = new Color(250, 250, 250);

    private final JTable userTable;
    private final DefaultTableModel tableModel;
    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;

    public UserManagementPanel() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]10[grow]"));
        setBackground(BACKGROUND_COLOR);

        try {
            userDAO = new UserDAO();
            departmentDAO = new DepartmentDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs", e);
        }

        // Add title
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TABLE_HEADER_COLOR);
        add(titleLabel, "wrap");

        // Create modern toolbar
        JPanel toolbar = createModernToolbar();
        add(toolbar, "growx, wrap");

        // Create table with modern styling
        String[] columns = {"Username", "Role", "Department", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        userTable = new JTable(tableModel);
        setupModernTable();

        // Add table with modern scrollpane
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);

        // Wrap scrollpane in a panel with shadow border
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(createShadowBorder());
        tablePanel.add(scrollPane);

        add(tablePanel, "grow");

        refreshData();
    }

    void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userDAO.findAll();
            for (User user : users) {
                Object[] row = {
                        user.getUsername(),
                        user.getRole(),
                        user.getDepartment() != null ? user.getDepartment().getName() : "",
                        "Edit Delete"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private boolean validateForm(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            showError("Username is required");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return false;
        }

        // Check if username already exists (for new user creation)
        try {
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                showError("Username already exists");
                return false;
            }
        } catch (SQLException e) {
            showError("Error validating username: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void saveUser(String username, String password, UserRole role, Department department) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setRole(role);
            user.setDepartment(department);

            userDAO.createUser(user, password);
            refreshData();
            showSuccess("User created successfully");
        } catch (SQLException e) {
            showError("Error creating user: " + e.getMessage());
        }
    }
    private JPanel createModernToolbar() {
        JPanel toolbar = new JPanel(new MigLayout("fillx, insets 0", "[left]push[right]"));
        toolbar.setOpaque(false);

        // Search field
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search users...");
        styleTextField(searchField);

        // Add button with icon
        JButton addButton = new ModernButton("Add New User +");
        styleButton(addButton);
        addButton.addActionListener(e -> showAddUserDialog());

        toolbar.add(searchField);
        toolbar.add(addButton);

        return toolbar;
    }

    private void setupModernTable() {
        // Basic table setup
        userTable.setRowHeight(40);
        userTable.setShowVerticalLines(false);
        userTable.setShowHorizontalLines(true);
        userTable.setGridColor(new Color(230, 230, 230));
        userTable.setBackground(Color.WHITE);
        userTable.setSelectionBackground(new Color(237, 244, 246));
        userTable.setSelectionForeground(Color.BLACK);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Custom header renderer
        JTableHeader header = userTable.getTableHeader();
        header.setDefaultRenderer(new ModernHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 40));
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Set column widths
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200); // Username
        columnModel.getColumn(1).setPreferredWidth(150); // Role
        columnModel.getColumn(2).setPreferredWidth(200); // Department
        columnModel.getColumn(3).setPreferredWidth(150); // Actions

        // Modern button renderer and editor
        columnModel.getColumn(3).setCellRenderer(new ModernButtonRenderer());
        columnModel.getColumn(3).setCellEditor(new ModernButtonEditor(new JCheckBox()));

        // Stripe pattern
        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_STRIPE_COLOR);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setLayout(new MigLayout("wrap 2, fillx, insets 20", "[][grow]", "[]15[]15[]15[]25[]"));

        // Style the dialog
        dialog.setBackground(Color.WHITE);
        ((JPanel) dialog.getContentPane()).setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Add New User");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dialog.add(titleLabel, "span 2, center, wrap 20");

        // Form components
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        JComboBox<Department> departmentCombo = new JComboBox<>();

        // Style form components
        styleTextField(usernameField);
        styleTextField(passwordField);
        styleComboBox(roleCombo);
        styleComboBox(departmentCombo);

        // Load departments
        try {
            List<Department> departments = departmentDAO.findAll();
            for (Department dept : departments) {
                departmentCombo.addItem(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add form components with styled labels
        dialog.add(createStyledLabel("Username:*"));
        dialog.add(usernameField, "growx");
        dialog.add(createStyledLabel("Password:*"));
        dialog.add(passwordField, "growx");
        dialog.add(createStyledLabel("Role:*"));
        dialog.add(roleCombo, "growx");
        dialog.add(createStyledLabel("Department:*"));
        dialog.add(departmentCombo, "growx");

        // Button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        styleButton(saveButton);
        styleButton(cancelButton);
        cancelButton.setBackground(Color.LIGHT_GRAY);

        buttonPanel.add(cancelButton, "growx");
        buttonPanel.add(saveButton, "growx");

        dialog.add(buttonPanel, "span 2, growx");

        // Dialog actions
        saveButton.addActionListener(e -> {
            if (validateForm(usernameField.getText(), new String(passwordField.getPassword()))) {
                saveUser(
                        usernameField.getText(),
                        new String(passwordField.getPassword()),
                        (UserRole) roleCombo.getSelectedItem(),
                        (Department) departmentCombo.getSelectedItem()
                );
                dialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Dialog properties
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Styling methods
    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 35));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JComponent) comboBox.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    private void styleButton(JButton button) {
        button.setBackground(MAIN_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(LIGHTER_MAIN);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(MAIN_COLOR);
            }
        });
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TABLE_HEADER_COLOR);
        return label;
    }

    private Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220))
        );
    }

    // Modern header renderer
    private class ModernHeaderRenderer extends DefaultTableCellRenderer {
        public ModernHeaderRenderer() {
            setHorizontalAlignment(JLabel.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setBackground(TABLE_HEADER_COLOR);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }

    // Modern button renderer and editor classes
    private class ModernButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editButton;
        private final JButton deleteButton;

        public ModernButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(true);

            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            styleActionButton(editButton, new Color(52, 152, 219));
            styleActionButton(deleteButton, new Color(231, 76, 60));

            add(editButton);
            add(deleteButton);
        }

        private void styleActionButton(JButton button, Color color) {
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            button.setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private class ModernButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton;

        public ModernButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            styleActionButton(editButton, new Color(52, 152, 219));
            styleActionButton(deleteButton, new Color(231, 76, 60));

            editButton.addActionListener(e -> {
                stopCellEditing();
                int row = userTable.getSelectedRow();
                if (row >= 0) {
                    String username = (String) userTable.getValueAt(row, 0);
                    editUser(username);
                }
            });

            deleteButton.addActionListener(e -> {
                stopCellEditing();
                int row = userTable.getSelectedRow();
                if (row >= 0) {
                    String username = (String) userTable.getValueAt(row, 0);
                    deleteUser(username);
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        private void styleActionButton(JButton button, Color color) {
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(color.brighter());
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(color);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Edit Delete";
        }
    }

    private void editUser(String username) {
        try {
            User user = userDAO.findByUsername(username);
            if (user != null) {
                showEditUserDialog(user);
            }
        } catch (SQLException e) {
            showError("Error loading user: " + e.getMessage());
        }
    }

    private void deleteUser(String username) {
        // Create custom confirm dialog
        JDialog confirmDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Confirm Delete", true);
        confirmDialog.setLayout(new MigLayout("wrap, fillx, insets 20", "[grow]", "[]20[]20[]"));
        confirmDialog.setBackground(Color.WHITE);
        ((JPanel) confirmDialog.getContentPane()).setBackground(Color.WHITE);

        // Add warning icon and message
        JLabel warningIcon = new JLabel("⚠️");
        warningIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        JLabel messageLabel = new JLabel("Are you sure you want to delete this user?");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel detailLabel = new JLabel("This action cannot be undone.");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailLabel.setForeground(Color.GRAY);

        // Add components
        confirmDialog.add(warningIcon, "center");
        confirmDialog.add(messageLabel, "center");
        confirmDialog.add(detailLabel, "center");

        // Button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        JButton deleteButton = new JButton("Delete");

        styleButton(cancelButton);
        styleButton(deleteButton);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        deleteButton.setBackground(new Color(231, 76, 60));

        buttonPanel.add(cancelButton, "growx");
        buttonPanel.add(deleteButton, "growx");

        confirmDialog.add(buttonPanel, "growx");

        // Add actions
        deleteButton.addActionListener(e -> {
            try {
                userDAO.deleteByUsername(username);
                refreshData();
                showSuccess("User deleted successfully");
                confirmDialog.dispose();
            } catch (SQLException ex) {
                showError("Error deleting user: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> confirmDialog.dispose());

        // Show dialog
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);
    }

    private void showEditUserDialog(User user) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", true);
        dialog.setLayout(new MigLayout("wrap 2, fillx, insets 20", "[][grow]", "[]15[]15[]15[]25[]"));
        dialog.setBackground(Color.WHITE);
        ((JPanel) dialog.getContentPane()).setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Edit User");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dialog.add(titleLabel, "span 2, center, wrap 20");

        // Form components
        JTextField usernameField = new JTextField(user.getUsername(), 20);
        JCheckBox changePasswordCheckbox = new JCheckBox("Change Password");
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        JComboBox<Department> departmentCombo = new JComboBox<>();

        // Style components
        styleTextField(usernameField);
        styleTextField(passwordField);
        styleComboBox(roleCombo);
        styleComboBox(departmentCombo);
        changePasswordCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Set initial values
        usernameField.setEnabled(false);
        roleCombo.setSelectedItem(user.getRole());
        passwordField.setEnabled(false);
        changePasswordCheckbox.addActionListener(e ->
                passwordField.setEnabled(changePasswordCheckbox.isSelected()));

        // Load departments
        try {
            List<Department> departments = departmentDAO.findAll();
            for (Department dept : departments) {
                departmentCombo.addItem(dept);
                if (user.getDepartment() != null &&
                        user.getDepartment().getId().equals(dept.getId())) {
                    departmentCombo.setSelectedItem(dept);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add form components
        dialog.add(createStyledLabel("Username:"));
        dialog.add(usernameField, "growx");
        dialog.add(changePasswordCheckbox, "span 2, wrap");
        dialog.add(createStyledLabel("New Password:"));
        dialog.add(passwordField, "growx");
        dialog.add(createStyledLabel("Role:"));
        dialog.add(roleCombo, "growx");
        dialog.add(createStyledLabel("Department:"));
        dialog.add(departmentCombo, "growx");

        // Button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][]"));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");

        styleButton(saveButton);
        styleButton(cancelButton);
        cancelButton.setBackground(Color.LIGHT_GRAY);

        buttonPanel.add(cancelButton, "growx");
        buttonPanel.add(saveButton, "growx");

        dialog.add(buttonPanel, "span 2, growx");

        // Dialog actions
        saveButton.addActionListener(e -> {
            if (updateUser(
                    user,
                    changePasswordCheckbox.isSelected() ? new String(passwordField.getPassword()) : null,
                    (UserRole) roleCombo.getSelectedItem(),
                    (Department) departmentCombo.getSelectedItem()
            )) {
                dialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Dialog properties
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean updateUser(User user, String newPassword, UserRole role, Department department) {
        try {
            user.setRole(role);
            user.setDepartment(department);

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (newPassword.length() < 6) {
                    showError("Password must be at least 6 characters long");
                    return false;
                }
                userDAO.updateUserWithPassword(user, newPassword);
            } else {
                userDAO.updateUser(user);
            }

            refreshData();
            showSuccess("User updated successfully");
            return true;
        } catch (SQLException e) {
            showError("Error updating user: " + e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
