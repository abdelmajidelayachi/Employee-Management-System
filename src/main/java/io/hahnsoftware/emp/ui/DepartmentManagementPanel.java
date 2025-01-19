package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.ui.button.MButton;
import io.hahnsoftware.emp.ui.form.DepartmentFormPanel;
import io.hahnsoftware.emp.ui.table.DepartmentTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;

public class DepartmentManagementPanel extends JPanel implements StyleConstants {
    private final JTable departmentTable;
    private final DepartmentTableModel tableModel;
    private final DepartmentDAO departmentDAO;
    private JTextField searchField;

    public DepartmentManagementPanel() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]15[]10[grow]"));
        setBackground(StyleConstants.BG_SECONDARY);

        try {
            departmentDAO = new DepartmentDAO(new EmployeeDAO());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DepartmentDAO", e);
        }

        // Add title
        JLabel titleLabel = new JLabel("Department Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(StyleConstants.TEXT_PRIMARY);
        add(titleLabel, "wrap");

        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, "growx, wrap");

        // Create table
        tableModel = new DepartmentTableModel();
        departmentTable = new JTable(tableModel);
        setupModernTable();

        // Create scroll pane first
        JScrollPane scrollPane = new JScrollPane(departmentTable);
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
        departmentTable.setRowHeight(45);
        departmentTable.setShowVerticalLines(false);
        departmentTable.setShowHorizontalLines(true);
        departmentTable.setGridColor(StyleConstants.BORDER_LIGHT);
        departmentTable.setBackground(StyleConstants.BG_PRIMARY);
        departmentTable.setSelectionBackground(StyleConstants.HOVER_COLOR);
        departmentTable.setSelectionForeground(StyleConstants.TEXT_PRIMARY);
        departmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        departmentTable.setBorder(BorderFactory.createEmptyBorder());

        // Intercell spacing
        departmentTable.setIntercellSpacing(new Dimension(0, 1));

        // Custom header renderer
        JTableHeader header = departmentTable.getTableHeader();
        header.setDefaultRenderer(new ModernHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(StyleConstants.MAIN_COLOR);
        header.setForeground(StyleConstants.TEXT_LIGHT);
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        // Set column widths
        TableColumnModel columnModel = departmentTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);  // ID
        columnModel.getColumn(1).setPreferredWidth(300);  // Name
        columnModel.getColumn(2).setPreferredWidth(200);  // Manager
        columnModel.getColumn(3).setPreferredWidth(150);  // Actions

        // Action column renderer and editor
        columnModel.getColumn(3).setCellRenderer(new ButtonRenderer());
        columnModel.getColumn(3).setCellEditor(new ButtonEditor());

        // Stripe pattern
        departmentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new MigLayout("fillx, insets 0", "[][grow]push[][100]", "[]"));
        filterPanel.setOpaque(false);

        // Search field with icon
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search departments...");
        styleTextField(searchField);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Add button with enhanced style
        MButton addButton = new MButton("+ Add Department", MButton.ButtonType.PRIMARY)
                .withSize(150, 38)
                .withAnimation(true);
        addButton.addActionListener(e -> showAddDepartmentDialog());

        // Add components with proper spacing
        filterPanel.add(searchPanel, "growx");
        filterPanel.add(addButton);

        // Add document listener for search field
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        return filterPanel;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(StyleConstants.BG_PRIMARY);
        scrollPane.setBackground(StyleConstants.BG_PRIMARY);

        // Style the scrollbars
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    }

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

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setBorder(createTextFieldBorder());
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(StyleConstants.BG_PRIMARY);
        field.setCaretColor(StyleConstants.MAIN_COLOR);
    }

    private javax.swing.border.Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    private javax.swing.border.Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220))
        );
    }

    private void showAddDepartmentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Department", true);

        DepartmentFormPanel formPanel = new DepartmentFormPanel(() -> {
            refreshData();
            dialog.dispose();
        });

        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditDepartmentDialog(Department department) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Department", true);

        DepartmentFormPanel formPanel = new DepartmentFormPanel(() -> {
            refreshData();
            dialog.dispose();
        });
        formPanel.setDepartment(department);

        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
            setBackground(isSelected ? table.getSelectionBackground() :
                    (row % 2 == 0 ? BG_PRIMARY : BG_SECONDARY));
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final MButton editButton;
        private final MButton deleteButton;
        private Department currentDepartment;

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
                if (currentDepartment != null) {
                    showEditDepartmentDialog(currentDepartment);
                }
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                if (currentDepartment != null) {
                    deleteDepartment(currentDepartment);
                }
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentDepartment = (Department) value;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentDepartment;
        }
    }

    private void deleteDepartment(Department department) {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this department?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                departmentDAO.deleteDepartment(department.getId());
                refreshData();
                showSuccess("Department deleted successfully");
            } catch (SQLException e) {
                showError("Error deleting department: " + e.getMessage());
            }
        }
    }

    private void applyFilters() {
        tableModel.filterByName(searchField.getText());
    }

    public void refreshData() {
        try {
            tableModel.setData(departmentDAO.findAll());
        } catch (SQLException e) {
            showError("Error loading departments: " + e.getMessage());
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