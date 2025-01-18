package io.hahnsoftware.emp.ui.form;

import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.ui.StyleConstants;
import io.hahnsoftware.emp.ui.button.MButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class DepartmentFormPanel extends JPanel implements StyleConstants {
    private final JTextField nameField;
    private final Runnable onSave;
    private Department existingDepartment;
    private final DepartmentDAO departmentDAO;

    public DepartmentFormPanel(Runnable onSave) {
        this.onSave = onSave;
        try {
            this.departmentDAO = new DepartmentDAO();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DepartmentDAO", e);
        }

        setLayout(new MigLayout("wrap 2, fillx, insets 20", "[][grow]", "[]15[]25[]"));
        setBackground(BG_PRIMARY);

        // Initialize components
        nameField = new JTextField(20);

        // Style components
        styleComponents();
        
        // Add components
        setupLayout();
        
        // Setup buttons
        setupButtons();
    }

    private void styleComponents() {
        styleTextField(nameField);
    }

    private void setupLayout() {
        add(createStyledLabel("Department Name:*"));
        add(nameField, "growx");
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

    public void setDepartment(Department department) {
        this.existingDepartment = department;
        if (department != null) {
            nameField.setText(department.getName());
        }
    }

    private void save() {
        if (!validateForm()) {
            return;
        }

        try {
            if (existingDepartment == null) {
                Department newDepartment = new Department();
                newDepartment.setName(nameField.getText().trim());
                departmentDAO.createDepartment(newDepartment);
            } else {
                existingDepartment.setName(nameField.getText().trim());
                departmentDAO.updateDepartment(existingDepartment);
            }
            onSave.run();

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        } catch (SQLException e) {
            showError("Error saving department: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Department name is required");
            return false;
        }
        return true;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setBorder(createTextFieldBorder());
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(BG_PRIMARY);
        field.setCaretColor(MAIN_COLOR);
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