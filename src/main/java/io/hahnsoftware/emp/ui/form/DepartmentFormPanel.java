package io.hahnsoftware.emp.ui.form;

import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.ui.StyleConstants;
import io.hahnsoftware.emp.ui.button.MButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class DepartmentFormPanel extends JPanel implements StyleConstants {
    private final JTextField nameField;
    private final JComboBox<Employee> managerComboBox;
    private final Runnable onSave;
    private Department existingDepartment;
    private final DepartmentDAO departmentDAO;
    private final EmployeeDAO employeeDAO;
    public DepartmentFormPanel(Runnable onSave) {
        this.onSave = onSave;
        try {
            this.employeeDAO = new EmployeeDAO();
            this.departmentDAO = new DepartmentDAO(this.employeeDAO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DepartmentDAO", e);
        }

        setLayout(new MigLayout("wrap 2, fillx, insets 20", "[][grow]", "[]15[]15[]25[]"));
        setBackground(BG_PRIMARY);

        // Initialize components
        nameField = new JTextField(20);
        managerComboBox = new JComboBox<>();

        // Style components
        styleComponents();

        // Add components
        setupLayout();

        // Setup buttons
        setupButtons();

        // Load managers
        loadManagers();
    }

    private void styleComponents() {
        styleTextField(nameField);
        styleComboBox(managerComboBox);
    }


    private void setupLayout() {
        add(createStyledLabel("Department Name:*"));
        add(nameField, "growx");

        add(createStyledLabel("Manager:"));
        add(managerComboBox, "growx");
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
            managerComboBox.setSelectedItem(department.getManager());
        } else {
            nameField.setText("");
            managerComboBox.setSelectedIndex(-1);
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
                newDepartment.setManager((Employee) managerComboBox.getSelectedItem());
                departmentDAO.createDepartment(newDepartment, true);
            } else {
                existingDepartment.setName(nameField.getText().trim());
                existingDepartment.setManager((Employee) managerComboBox.getSelectedItem());
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

    private void loadManagers() {
        try {
            java.util.List<Employee> managers = employeeDAO.findAllManagers();
            managerComboBox.removeAllItems();
            managerComboBox.addItem(null); // Add a null option for no manager
            for (Employee manager : managers) {
                managerComboBox.addItem(manager);
            }
        } catch (SQLException e) {
            showError("Error loading managers: " + e.getMessage());
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Employee) {
                    Employee employee = (Employee) value;
                    setText(employee.getFullName());
                } else if (value == null) {
                    setText("No Manager");
                }
                return this;
            }
        });
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