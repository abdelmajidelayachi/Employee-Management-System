package io.hahnsoftware.emp.ui.form;

import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.User;
import io.hahnsoftware.emp.model.UserRole;
import io.hahnsoftware.emp.ui.StyleConstants;
import io.hahnsoftware.emp.ui.button.MButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class UserFormPanel extends JPanel implements StyleConstants {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<UserRole> roleCombo;
    private final JComboBox<Department> departmentCombo;
    private final JCheckBox changePasswordCheckbox;
    private final Runnable onSave;
    private User existingUser;

    public UserFormPanel(Runnable onSave) {
        this.onSave = onSave;
        setLayout(new MigLayout("wrap 2, fillx, insets 20", "[][grow]", "[]15[]15[]15[]25[]"));
        setBackground(BG_PRIMARY);

        // Initialize components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        roleCombo = new JComboBox<>(UserRole.values());
        departmentCombo = new JComboBox<>();
        changePasswordCheckbox = new JCheckBox("Change Password");

        // Style components
        styleComponents();
        
        // Add components
        setupLayout();
        
        // Setup buttons
        setupButtons();
    }

    private void styleComponents() {
        styleTextField(usernameField);
        styleTextField(passwordField);
        styleComboBox(roleCombo);
        styleComboBox(departmentCombo);
        setupDepartmentComboBox();
        changePasswordCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        changePasswordCheckbox.setVisible(false); // Only show in edit mode
    }

    private void setupLayout() {
        // Add form components with styled labels
        add(createStyledLabel("Username:*"));
        add(usernameField, "growx");
        add(changePasswordCheckbox, "span 2, wrap");
        add(createStyledLabel("Password:*"));
        add(passwordField, "growx");
        add(createStyledLabel("Role:*"));
        add(roleCombo, "growx");
        add(createStyledLabel("Department:*"));
        add(departmentCombo, "growx");
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][]"));
        buttonPanel.setOpaque(false);

        MButton saveButton = new MButton("Save", MButton.ButtonType.PRIMARY)
                .withSize(150, 38)
                .withAnimation(true);
        
        MButton cancelButton = new MButton("Cancel", MButton.ButtonType.SECONDARY)
                .withSize(120, 38);

        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        });

        buttonPanel.add(cancelButton, "growx");
        buttonPanel.add(saveButton, "growx");

        add(buttonPanel, "span 2, growx");
    }

    public void setDepartments(List<Department> departments) {
        departmentCombo.removeAllItems();
        for (Department dept : departments) {
            departmentCombo.addItem(dept);
        }
    }

    public void setUser(User user) {
        this.existingUser = user;
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(false);
        roleCombo.setSelectedItem(user.getRole());
        if (user.getDepartment() != null) {
            departmentCombo.setSelectedItem(user.getDepartment());
        }

        // Show password change option for existing users
        changePasswordCheckbox.setVisible(true);
        passwordField.setEnabled(false);
        changePasswordCheckbox.addActionListener(e ->
                passwordField.setEnabled(changePasswordCheckbox.isSelected()));
    }

    private void save() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        // Create or update user
        try {
            if (existingUser == null) {
                User newUser = new User();
                newUser.setUsername(usernameField.getText());
                newUser.setRole((UserRole) roleCombo.getSelectedItem());
                newUser.setDepartment((Department) departmentCombo.getSelectedItem());

                onSave.run();
            } else {
                existingUser.setRole((UserRole) roleCombo.getSelectedItem());
                existingUser.setDepartment((Department) departmentCombo.getSelectedItem());

                onSave.run();
            }

            // Close form
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        } catch (Exception e) {
            showError("Error saving user: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty()) {
            showError("Username is required");
            return false;
        }

        if (existingUser == null || changePasswordCheckbox.isSelected()) {
            if (password.trim().isEmpty()) {
                showError("Password is required");
                return false;
            }
            if (password.length() < 6) {
                showError("Password must be at least 6 characters long");
                return false;
            }
        }

        return true;
    }

    private void setupDepartmentComboBox() {
        departmentCombo.setPreferredSize(new Dimension(departmentCombo.getPreferredSize().width, 38));
        departmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        departmentCombo.setBorder(createTextFieldBorder());

        departmentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department) {
                    setText(((Department) value).getName());
                }
                return this;
            }
        });

        ((JComponent) departmentCombo.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
    }
    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setBorder(createTextFieldBorder());
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(BG_PRIMARY);
        field.setCaretColor(MAIN_COLOR);
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 38));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(createTextFieldBorder());
        ((JComponent) comboBox.getRenderer()).setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private javax.swing.border.Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}