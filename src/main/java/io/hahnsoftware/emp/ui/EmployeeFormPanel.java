package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.ui.date.DatePickerComponent;
import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.model.EmploymentStatus;
import net.sourceforge.jdatepicker.JDatePanel;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
public class EmployeeFormPanel extends JPanel implements StyleConstants {
    private final JTextField employeeIdField;
    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextArea addressArea;
    private final JTextField jobTitleField;
    private final JComboBox<Department> departmentCombo;
    private final JComboBox<EmploymentStatus> statusCombo;
    private final JButton saveButton;
    private final JButton cancelButton;

    private final EmployeeDAO employeeDAO;
    private final DepartmentDAO departmentDAO;
    private final Runnable onSaveComplete;
    private final DatePickerComponent hireDatePicker;

    private Long editingEmployeeId;

    public EmployeeFormPanel(Runnable onSaveComplete) {
        this.onSaveComplete = onSaveComplete;

        try {
            employeeDAO = new EmployeeDAO();
            departmentDAO = new DepartmentDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs", e);
        }

        // Set main panel layout with padding
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[grow]"));

        // Create main form panel with modern styling
        JPanel formPanel = new JPanel(new MigLayout("fillx, wrap 2, insets 30",
                "[][grow, fill]",
                "[]15[]15[]15[]15[]15[]15[]15[]"));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        formPanel.setBackground(BG_PRIMARY);

        // Initialize components with modern styling
        employeeIdField = createStyledTextField();
        fullNameField = createStyledTextField();
        emailField = createStyledTextField();
        phoneField = createStyledTextField();
        jobTitleField = createStyledTextField();

        // Styled text area
        addressArea = new JTextArea(3, 20);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Styled combo boxes
        departmentCombo = createStyledComboBox();
        statusCombo = createStyledComboBox();
        statusCombo.setModel(new DefaultComboBoxModel<>(EmploymentStatus.values()));

        // Date picker component
        hireDatePicker = new DatePickerComponent();
        hireDatePicker.addActionListener(e -> {
            LocalDate selectedDate = hireDatePicker.getDate();
            if (selectedDate != null) {
                hireDatePicker.getJFormattedTextField().setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                hireDatePicker.getJFormattedTextField().setText("");
            }
        });

        // Create section panels
        JPanel personalInfoPanel = createSectionPanel("Personal Information");
        JPanel employmentInfoPanel = createSectionPanel("Employment Information");

        // Add components to personal info section
        personalInfoPanel.add(createStyledLabel("Employee ID:"), "align label");
        personalInfoPanel.add(employeeIdField, "growx");
        personalInfoPanel.add(createStyledLabel("Full Name:"), "align label");
        personalInfoPanel.add(fullNameField, "growx");
        personalInfoPanel.add(createStyledLabel("Email:"), "align label");
        personalInfoPanel.add(emailField, "growx");
        personalInfoPanel.add(createStyledLabel("Phone:"), "align label");
        personalInfoPanel.add(phoneField, "growx");
        personalInfoPanel.add(createStyledLabel("Address:"), "align label");
        personalInfoPanel.add(addressScroll, "growx");

        // Add components to employment info section
        employmentInfoPanel.add(createStyledLabel("Job Title:"), "align label");
        employmentInfoPanel.add(jobTitleField, "growx");
        personalInfoPanel.add(createStyledLabel("Hire Date:"), "align label");
        personalInfoPanel.add(hireDatePicker, "growx");
        employmentInfoPanel.add(createStyledLabel("Department:"), "align label");
        employmentInfoPanel.add(departmentCombo, "growx");
        employmentInfoPanel.add(createStyledLabel("Status:"), "align label");
        employmentInfoPanel.add(statusCombo, "growx");

        // Style and create buttons
        saveButton = createStyledButton("Save", MAIN_COLOR);
        cancelButton = createStyledButton("Cancel", NEUTRAL_MEDIUM);

        // Button panel with modern styling
        JPanel buttonPanel = new JPanel(new MigLayout("insets 20", "push[]10[]"));
        buttonPanel.setBackground(BG_PRIMARY);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Add all panels to main form
        JPanel contentPanel = new JPanel(new MigLayout("fillx, wrap", "[grow]"));
        contentPanel.setBackground(BG_PRIMARY);
        contentPanel.add(personalInfoPanel, "growx");
        contentPanel.add(employmentInfoPanel, "growx");
        contentPanel.add(buttonPanel, "growx");

        // Add form to main panel with card-like styling
        JPanel wrapperPanel = new JPanel(new MigLayout("fill", "[center]", "[center]"));
        wrapperPanel.setBackground(BG_SECONDARY);
        wrapperPanel.add(contentPanel, "grow");

        add(wrapperPanel, "grow");

        // Setup button actions
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> onSaveComplete.run());

        // Load departments
        loadDepartments();
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setBackground(BG_PRIMARY);
        field.setForeground(TEXT_PRIMARY);
        return field;
    }

    private void styleDatePicker(JDatePickerImpl datePicker) {
        // Style the text field
        JFormattedTextField textField = datePicker.getJFormattedTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        textField.setBackground(BG_PRIMARY);
        textField.setForeground(TEXT_PRIMARY);
        textField.setPreferredSize(new Dimension(200, 38)); // Consistent height with other fields

        // Optional: Add hover and focus effects
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(MAIN_COLOR),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }
        });
    }
    private JComboBox createStyledComboBox() {
        JComboBox box = new JComboBox();
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        box.setBackground(BG_PRIMARY);
        box.setForeground(TEXT_PRIMARY);
        return box;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(backgroundColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        return button;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 2, insets 20", "[][grow,fill]", "[]15[]"));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 16),
                        TEXT_PRIMARY
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    public void setEmployee(Employee employee) {
        editingEmployeeId = employee != null ? employee.getId() : null;

        if (employee != null) {
            employeeIdField.setText(employee.getEmployeeId());
            fullNameField.setText(employee.getFullName());
            emailField.setText(employee.getEmail());
            phoneField.setText(employee.getPhone());
            addressArea.setText(employee.getAddress());
            jobTitleField.setText(employee.getJobTitle());
            departmentCombo.setSelectedItem(employee.getDepartment());
            statusCombo.setSelectedItem(employee.getStatus());

            // Set hire date
            hireDatePicker.setDate(employee != null ? employee.getHireDate() : null);
        } else {
            clearForm();
        }
    }


    private void loadDepartments() {
        try {
            List<Department> departments = departmentDAO.findAll();
            departmentCombo.removeAllItems();

            // Custom renderer to display department name
            departmentCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof Department) {
                        Department dept = (Department) value;
                        value = dept.getName(); // Display department name
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

            for (Department dept : departments) {
                departmentCombo.addItem(dept);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading departments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSave() {
        try {
            // Determine if this is a create or update operation
            boolean isNewEmployee = editingEmployeeId == null;

            // Validate input fields
            String employeeId = employeeIdField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            String jobTitle = jobTitleField.getText().trim();
            Department department = (Department) departmentCombo.getSelectedItem();
            EmploymentStatus status = (EmploymentStatus) statusCombo.getSelectedItem();

            LocalDate hireDate = hireDatePicker.getDate();
            // Validate required fields
            if (employeeId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Employee ID is required",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Full Name is required",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create or update employee object
            Employee employee = isNewEmployee ? new Employee() : employeeDAO.findByEmployeeId(employeeId);

            // Set employee details
            employee.setEmployeeId(employeeId);
            employee.setFullName(fullName);
            employee.setEmail(email);
            employee.setPhone(phone);
            employee.setAddress(address);
            employee.setJobTitle(jobTitle);
            employee.setHireDate(hireDate);
            employee.setDepartment(department);
            employee.setStatus(status);

            // Set hire date only for new employees
            if (isNewEmployee) {
                employee.setHireDate(LocalDate.now());
            }

            // Perform validation
            List<String> validationErrors = validateEmployee(employee);
            if (!validationErrors.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        String.join("\n", validationErrors),
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save the employee
            if (isNewEmployee) {
                employeeDAO.create(employee);
                showSuccessAndClose("Employee created successfully");
            } else {
                employeeDAO.update(employee);
                showSuccessAndClose("Employee updated successfully");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving employee: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSuccessAndClose(String message) {
        // Show success message
        JOptionPane.showMessageDialog(this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Call the onSaveComplete runnable to close the dialog
        onSaveComplete.run();
    }

    
    private List<String> validateEmployee(Employee employee) {
        return employeeDAO.validateEmployee(employee);
    }

    private void clearForm() {
        editingEmployeeId = null;
        employeeIdField.setText("");
        fullNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        jobTitleField.setText("");
        departmentCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        hireDatePicker.setDate(null);
    }
    private class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final String datePattern = "yyyy-MM-dd";
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                if (value instanceof Date) {
                    return dateFormatter.format((Date) value);
                } else if (value instanceof LocalDate) {
                    return dateFormatter.format(Date.from(((LocalDate) value).atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()));
                }
            }
            return ""; // Return an empty string when value is null
        }
    }

}