package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.ui.button.MButton;
import io.hahnsoftware.emp.ui.form.UserFormPanel;
import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.dto.UserDAO;
import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.UserRole;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementPanel extends JPanel implements StyleConstants{
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
// Replace the existing scroll pane code with:
        JScrollPane scrollPane = new JScrollPane(userTable);
        styleScrollPanel(scrollPane);

// Update the table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StyleConstants.BG_PRIMARY);
        tablePanel.setBorder(createShadowBorder());
        tablePanel.add(scrollPane);
        add(tablePanel, "grow");

        refreshData();
    }
    private void styleScrollPanel(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(StyleConstants.BG_PRIMARY);
        scrollPane.setBackground(StyleConstants.BG_PRIMARY);
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
        MButton addButton = new MButton("+ Add New User", MButton.ButtonType.PRIMARY)
                .withSize(150, 38)
                .withAnimation(true);
        addButton.addActionListener(e -> showAddUserDialog());

        toolbar.add(searchField);
        toolbar.add(addButton);

        return toolbar;
    }

    private void setupModernTable() {
        // Basic table setup
        userTable.setRowHeight(45);
        userTable.setShowVerticalLines(false);
        userTable.setShowHorizontalLines(true);
        userTable.setGridColor(StyleConstants.BORDER_LIGHT);
        userTable.setBackground(StyleConstants.BG_PRIMARY);
        userTable.setSelectionBackground(StyleConstants.HOVER_COLOR);
        userTable.setSelectionForeground(StyleConstants.TEXT_PRIMARY);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userTable.setBorder(BorderFactory.createEmptyBorder());

        // Intercell spacing
        userTable.setIntercellSpacing(new Dimension(0, 1));

        // Custom header renderer
        JTableHeader header = userTable.getTableHeader();
        header.setDefaultRenderer(new ModernHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(StyleConstants.MAIN_COLOR);
        header.setForeground(StyleConstants.TEXT_LIGHT);
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        // Set column widths
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200); // Username
        columnModel.getColumn(1).setPreferredWidth(150); // Role
        columnModel.getColumn(2).setPreferredWidth(200); // Department
        columnModel.getColumn(3).setPreferredWidth(150); // Actions

        // Action column renderer and editor
        columnModel.getColumn(3).setCellRenderer(new ButtonRenderer());
        columnModel.getColumn(3).setCellEditor(new ButtonEditor());

        // Stripe pattern
        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? StyleConstants.BG_PRIMARY : StyleConstants.BG_SECONDARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);

        UserFormPanel formPanel = new UserFormPanel(() -> {
            refreshData();
            dialog.dispose();
        });

        try {
            List<Department> departments = departmentDAO.findAll();
            formPanel.setDepartments(departments);
        } catch (SQLException e) {
            showError("Error loading departments: " + e.getMessage());
        }

        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditUserDialog(String username) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", true);

        UserFormPanel formPanel = new UserFormPanel(() -> {
            refreshData();
            dialog.dispose();
        });

        try {
            User user = userDAO.findByUsername(username);
            List<Department> departments = departmentDAO.findAll();
            formPanel.setDepartments(departments);
            formPanel.setUser(user);
        } catch (SQLException e) {
            showError("Error loading data: " + e.getMessage());
        }

        dialog.setContentPane(formPanel);
        dialog.pack();
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
    private class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final MButton editButton;
        private final MButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(true);

            editButton = new MButton("Edit", MButton.ButtonType.BTN_INFO)
                    .withSize(80, 30)
                    .withAnimation(false);

            deleteButton = new MButton("Delete", MButton.ButtonType.BTN_DANGER)
                    .withSize(80, 30)
                    .withAnimation(false);

            add(editButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            // Set background based on selection and stripe pattern
            setBackground(isSelected ? table.getSelectionBackground() :
                    (row % 2 == 0 ? BG_PRIMARY : BG_SECONDARY));

            // Always show buttons
            editButton.setVisible(true);
            deleteButton.setVisible(true);

            // Make panel opaque to handle background properly
            setOpaque(true);

            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final MButton editButton;
        private final MButton deleteButton;
        private String username;

        public ButtonEditor() {
            super(new JCheckBox());

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            editButton = new MButton("Edit", MButton.ButtonType.BTN_INFO)
                    .withSize(80, 30)
                    .withAnimation(false);

            deleteButton = new MButton("Delete", MButton.ButtonType.BTN_DANGER)
                    .withSize(80, 30)
                    .withAnimation(false);

            editButton.addActionListener(e -> {
                fireEditingStopped();
                if (username != null) {
                    editUser(username);
                }
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                if (username != null) {
                    deleteUser(username);
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            username = (String) table.getValueAt(row, 0);
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
                showEditUserDialog(user.getUsername());
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

        MButton cancelButton = new MButton("Cancel", MButton.ButtonType.SECONDARY)
                .withSize(120, 38)
                .withAnimation(true);
        MButton deleteButton = new MButton("Delete", MButton.ButtonType.BTN_DANGER)
                .withSize(120, 38)
                .withAnimation(true);

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
