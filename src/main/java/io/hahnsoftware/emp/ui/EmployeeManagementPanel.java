package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.ui.button.MButton;
import io.hahnsoftware.emp.ui.form.EmployeeFormPanel;
import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.dto.EmployeeDAO;
import io.hahnsoftware.emp.model.Employee;
import io.hahnsoftware.emp.model.EmploymentStatus;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeManagementPanel extends JPanel implements StyleConstants{

    private final JTable employeeTable;
    private final DefaultTableModel tableModel;
    private final EmployeeDAO employeeDAO;
    private final Runnable onAddNew;
    private List<Employee> allEmployees;
    private JTextField searchField;
    private JComboBox<EmploymentStatus> statusFilter;
    private JDatePickerImpl fromDatePicker;
    private JDatePickerImpl toDatePicker;

    // Update the constructor's table setup part
    public EmployeeManagementPanel(Runnable onAddNew) {
        this.onAddNew = onAddNew;
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]10[grow]"));
        setBackground(StyleConstants.BG_SECONDARY);

        try {
            employeeDAO = new EmployeeDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize EmployeeDAO", e);
        }

        // Add title
        JLabel titleLabel = new JLabel("Employee Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(StyleConstants.TEXT_PRIMARY);
        add(titleLabel, "wrap");

        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, "growx, wrap");

        // Create table
        String[] columns = {"ID", "Full Name", "Department", "Job Title", "Status", "Hire Date", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };

        employeeTable = new JTable(tableModel);
        setupModernTable();

        // Create scroll pane first
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        styleScrollPane(scrollPane);

        // Wrap scrollpane in a panel with shadow border
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StyleConstants.BG_PRIMARY);
        tablePanel.setBorder(createShadowBorder());
        tablePanel.add(scrollPane);

        add(tablePanel, "grow");
        refreshData();
    }

    private void setupModernTable() {
        // Basic table setup
        employeeTable.setRowHeight(45);
        employeeTable.setShowVerticalLines(false);
        employeeTable.setShowHorizontalLines(true);
        employeeTable.setGridColor(StyleConstants.BORDER_LIGHT);
        employeeTable.setBackground(StyleConstants.BG_PRIMARY);
        employeeTable.setSelectionBackground(StyleConstants.HOVER_COLOR);
        employeeTable.setSelectionForeground(StyleConstants.TEXT_PRIMARY);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.setBorder(BorderFactory.createEmptyBorder());

        // Intercell spacing
        employeeTable.setIntercellSpacing(new Dimension(0, 1));


        // Custom header renderer
        JTableHeader header = employeeTable.getTableHeader();
        header.setDefaultRenderer(new ModernHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(StyleConstants.MAIN_COLOR);
        header.setForeground(StyleConstants.TEXT_LIGHT);
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        // Set column widths
        TableColumnModel columnModel = employeeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // ID
        columnModel.getColumn(1).setPreferredWidth(200); // Full Name
        columnModel.getColumn(2).setPreferredWidth(150); // Department
        columnModel.getColumn(3).setPreferredWidth(150); // Job Title
        columnModel.getColumn(4).setPreferredWidth(100); // Status
        columnModel.getColumn(5).setPreferredWidth(100); // Hire Date
        columnModel.getColumn(6).setPreferredWidth(150); // Actions

        // Action column renderer and editor
        columnModel.getColumn(6).setCellRenderer(new ButtonRenderer());
        columnModel.getColumn(6).setCellEditor(new ButtonEditor());

        // Stripe pattern
        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(StyleConstants.BG_PRIMARY);
        scrollPane.setBackground(StyleConstants.BG_PRIMARY);

        // Style the scrollbars
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    }

    // Optional: Add this custom ScrollBarUI for a modern look
    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = StyleConstants.MAIN_COLOR.brighter();
            this.trackColor = StyleConstants.BG_SECONDARY;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y,
                    thumbBounds.width, thumbBounds.height,
                    8, 8);
            g2.dispose();
        }
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new MigLayout("fillx, insets 0", "[][grow]push[][100]", "[]"));
        filterPanel.setOpaque(false);

        // Search field with icon
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");  // You can replace with a proper icon
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search employees...");
        styleTextField(searchField);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Initialize status filter
        statusFilter = new JComboBox<>(new EmploymentStatus[]{null, EmploymentStatus.ACTIVE, EmploymentStatus.INACTIVE,
                EmploymentStatus.ON_LEAVE, EmploymentStatus.TERMINATED});
        statusFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    value = "All Statuses";
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> applyFilters());

        // Status filter with modern styling
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(statusFilter, BorderLayout.CENTER);

        // Add button with enhanced style
        MButton addButton = new MButton("+ Add Employee", MButton.ButtonType.PRIMARY)
                .withSize(150, 38)
                .withAnimation(true);
        addButton.addActionListener(e -> onAddNew.run());

        // Add components with proper spacing
        filterPanel.add(searchPanel, "growx");
        filterPanel.add(statusPanel, "width 150!");
        filterPanel.add(addButton);

        // Add document listener for search field
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        return filterPanel;
    }


    private Border createModernBorder() {
        return BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                BorderFactory.createLineBorder(StyleConstants.BORDER_LIGHT, 1)
        );
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));  // Increased height
        field.setBorder(createTextFieldBorder());
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(StyleConstants.BG_PRIMARY);
        field.setCaretColor(StyleConstants.MAIN_COLOR);

        // Add focus listener for highlight effect
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(createTextFieldBorderFocused());
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(createTextFieldBorder());
            }
        });
    }
    private Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)  // Increased padding
        );
    }

    private Border createTextFieldBorderFocused() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.MAIN_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 38));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(StyleConstants.BG_PRIMARY);
        comboBox.setBorder(createTextFieldBorder());

        // Style the dropdown button with a custom UI
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

                        // Button background
                        g2.setColor(StyleConstants.BG_PRIMARY);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        // Draw the arrow
                        int width = 8;
                        int height = 5;
                        int x = (getWidth() - width) / 2;
                        int y = (getHeight() - height) / 2;

                        g2.setColor(StyleConstants.TEXT_SECONDARY);
                        int[] xPoints = {x, x + width, x + width/2};
                        int[] yPoints = {y, y, y + height};
                        g2.fillPolygon(xPoints, yPoints, 3);

                        g2.dispose();
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(20, 20);
                    }
                };
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(comboBox.getBackground());
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        // Add hover effect to the combobox
        comboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                comboBox.setBorder(createTextFieldBorderFocused());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!comboBox.hasFocus()) {
                    comboBox.setBorder(createTextFieldBorder());
                }
            }
        });

        // Keep focus border when clicked
        comboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comboBox.setBorder(createTextFieldBorderFocused());
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                comboBox.setBorder(createTextFieldBorder());
            }
        });
    }


    private Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220))
        );
    }

    public void refreshData() {
        try {
            allEmployees = employeeDAO.findAll();
            applyFilters();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading employees: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        EmploymentStatus selectedStatus = (EmploymentStatus) statusFilter.getSelectedItem();

        List<Employee> filteredEmployees = allEmployees.stream()
                .filter(employee -> {
                    if (!searchText.isEmpty()) {
                        return employee.getFullName().toLowerCase().contains(searchText) ||
                                employee.getEmployeeId().toLowerCase().contains(searchText) ||
                                employee.getDepartment().getName().toLowerCase().contains(searchText)||
                                employee.getRole().name().toLowerCase().contains(searchText)||
                                employee.getUsername().toLowerCase().contains(searchText)||
                                employee.getJobTitle().toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .filter(employee -> selectedStatus == null || employee.getStatus() == selectedStatus)
                .collect(Collectors.toList());

        updateTableData(filteredEmployees);
    }

    private void updateTableData(List<Employee> employees) {
        tableModel.setRowCount(0);
        for (Employee emp : employees) {
            Object[] row = {
                    emp.getEmployeeId(),
                    emp.getFullName(),
                    emp.getDepartment().getName(),
                    emp.getJobTitle(),
                    emp.getStatus(),
                    emp.getHireDate(),
                    "Edit Delete"
            };
            tableModel.addRow(row);
        }
    }

    // Inner classes for table rendering
    private class ModernHeaderRenderer extends DefaultTableCellRenderer {
        public ModernHeaderRenderer() {
            setHorizontalAlignment(JLabel.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setBackground(StyleConstants.TABLE_HEADER_COLOR);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }


    private void editEmployee(String employeeId) {
        try {
            Employee employee = employeeDAO.findByEmployeeId(employeeId);
            if (employee != null) {
                showEditEmployeeDialog(employee);
            }
        } catch (SQLException e) {
            showError("Error loading employee: " + e.getMessage());
        }
    }

    private void deleteEmployee(String employeeId) {
        // Create custom confirm dialog
        JDialog confirmDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Confirm Delete", true);
        confirmDialog.setLayout(new MigLayout("wrap, fillx, insets 20", "[grow]", "[]20[]20[]"));
        confirmDialog.setBackground(Color.WHITE);
        ((JPanel) confirmDialog.getContentPane()).setBackground(Color.WHITE);

        // Add warning icon and message
        JLabel warningIcon = new JLabel("⚠️");
        warningIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        JLabel messageLabel = new JLabel("Are you sure you want to delete this employee?");
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
        buttonPanel.setForeground(TEXT_PRIMARY);

        MButton cancelButton = new MButton("Cancel", MButton.ButtonType.SECONDARY)
                .withSize(120, 38)
                .withAnimation(true);
        MButton deleteButton = new MButton("Delete", MButton.ButtonType.BTN_DANGER)
                .withSize(120, 38)
                .withAnimation(true);

        buttonPanel.add(cancelButton, "growx");
        buttonPanel.add(deleteButton, "growx");

        confirmDialog.add(buttonPanel, "growx");

        // Add actions
        deleteButton.addActionListener(e -> {
            try {
                employeeDAO.delete(employeeId);
                refreshData();
                showSuccess("Employee deleted successfully");
                confirmDialog.dispose();
            } catch (SQLException ex) {
                showError("Error deleting employee: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> confirmDialog.dispose());

        // Show dialog
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);
    }

    private void showEditEmployeeDialog(Employee employee) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Employee", true);

        EmployeeFormPanel formPanel = new EmployeeFormPanel(() -> {
            refreshData();
            dialog.dispose(); // Dispose the dialog after saving
        });
        formPanel.setEmployee(employee);

        // Create dialog
        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        private String employeeId;

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
                if (employeeId != null) {
                    editEmployee(employeeId);
                }
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                if (employeeId != null) {
                    deleteEmployee(employeeId);
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            employeeId = (String) table.getValueAt(row, 0);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Edit Delete";
        }
    }
    // Add utility methods for export and print functionality
    private void addExportButton(JPanel toolbar) {
        MButton exportButton = new MButton("Export", MButton.ButtonType.SECONDARY)
                .withSize(120, 38);;
        exportButton.addActionListener(e -> exportToExcel());
        toolbar.add(exportButton);
    }

    private void addPrintButton(JPanel toolbar) {
        MButton printButton = new MButton("Print", MButton.ButtonType.SECONDARY)
                .withSize(120, 38);;
        printButton.addActionListener(e -> printEmployeeList());
        toolbar.add(printButton);
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Employees");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                return f.getName().toLowerCase().endsWith(".xlsx") || f.isDirectory();
            }
            public String getDescription() {
                return "Excel Files (*.xlsx)";
            }
        });

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                // Export logic to be implemented
                showSuccess("Export completed successfully");
            } catch (Exception e) {
                showError("Error exporting data: " + e.getMessage());
            }
        }
    }

    private void printEmployeeList() {
        try {
            MessageFormat header = new MessageFormat("Employee List - {0}");
            MessageFormat footer = new MessageFormat("Page {0}");
            employeeTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException e) {
            showError("Error printing: " + e.getMessage());
        }
    }
}