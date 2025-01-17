package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.ui.table.DepartmentTableModel;
import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.dto.DepartmentDAO;
import io.hahnsoftware.emp.model.Department;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
public class DepartmentManagementPanel extends JPanel implements StyleConstants {
    private final JTable departmentTable;
    private final DepartmentTableModel tableModel;
    private final DepartmentDAO departmentDAO;
    private JTextField searchField;

    // Font constants
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public DepartmentManagementPanel() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[]0[]0[grow]"));
        setBackground(BG_SECONDARY);

        try {
            departmentDAO = new DepartmentDAO();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DepartmentDAO", e);
        }

        tableModel = new DepartmentTableModel();
        departmentTable = createTable();

        // Main layout components
        add(createHeaderPanel(), "growx, wrap");
        add(createToolbarPanel(), "growx, wrap");
        add(createMainContentPanel(), "grow");

        refreshData();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new MigLayout("fillx, insets 15 20", "[grow][]"));
        header.setBackground(MAIN_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Department Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_LIGHT);
        titleLabel.setIcon(createIcon("", titleLabel.getFont()));

        // Add button
        JButton addButton = createPrimaryButton("Add Department", SUCCESS);
        addButton.addActionListener(e -> showDepartmentDialog(null));

        header.add(titleLabel);
        header.add(addButton);
        return header;
    }

    private JPanel createToolbarPanel() {
        JPanel toolbar = new JPanel(new MigLayout("fillx, insets 10 20", "[][]push[][]"));
        toolbar.setBackground(BG_PRIMARY);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        // Search field
        JPanel searchBox = createSearchBox();

        // Action buttons
        JButton filterBtn = createToolbarButton("Filter", INFO);
        JButton exportBtn = createToolbarButton("Export", WARNING);
        JButton refreshBtn = createToolbarButton("Refresh", SUCCESS);

        toolbar.add(searchBox, "w 300!");
        toolbar.add(filterBtn);
        toolbar.add(exportBtn);
        toolbar.add(refreshBtn);

        return toolbar;
    }

    // Add these methods to the DepartmentManagementPanel class

    private Icon createIcon(String text, Font font) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(c.getForeground());
                g2.drawString(text, x, y + fm.getAscent());
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }

    // Create action button method (to be used by both renderer and editor)
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(color);
        button.setBackground(BG_PRIMARY);
        button.setBorder(BorderFactory.createLineBorder(color, 1));
        button.setPreferredSize(new Dimension(60, 28));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color);
                button.setForeground(TEXT_LIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BG_PRIMARY);
                button.setForeground(color);
            }
        });

        return button;
    }

    // For backward compatibility
    private void showDepartmentDialog(Department department) {
        showEditDialog(department);
    }

    private JPanel createSearchBox() {
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setBackground(BG_PRIMARY);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Search icon
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(TEXT_SECONDARY);

        // Search field
        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setFont(REGULAR_FONT);
        searchField.putClientProperty("JTextField.placeholderText", "Search departments...");

        // Add search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
        });

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);
        return searchBox;
    }

    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[grow]"));
        contentPanel.setBackground(BG_SECONDARY);

        // Table with scrollpane
        JScrollPane scrollPane = new JScrollPane(departmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scrollPane.getViewport().setBackground(BG_PRIMARY);

        // Style scrollbars
        styleScrollBars(scrollPane);

        contentPanel.add(scrollPane, "grow");
        return contentPanel;
    }

    private JTable createTable() {
        JTable table = new JTable(tableModel);

        // Basic styling
        table.setFont(REGULAR_FONT);
        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setBackground(BG_PRIMARY);
        table.setSelectionBackground(BG_SECONDARY);
        table.setSelectionForeground(TEXT_PRIMARY);

        // Headers
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_SECONDARY);
        header.setForeground(TEXT_LIGHT);
        header.setFont(BUTTON_FONT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        // Cell renderer for striping
        table.setDefaultRenderer(Object.class, createStripedRenderer());

        // Column configuration
        configureTableColumns(table);

        return table;
    }

    private TableCellRenderer createStripedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_PRIMARY : BG_SECONDARY);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
    }

    private void configureTableColumns(JTable table) {
        TableColumnModel columns = table.getColumnModel();

        // Set widths
        columns.getColumn(0).setPreferredWidth(80);   // ID
        columns.getColumn(1).setPreferredWidth(250);  // Name
        columns.getColumn(2).setPreferredWidth(200);  // Manager
        columns.getColumn(3).setPreferredWidth(130);  // Actions

        // Action column
        columns.getColumn(3).setCellRenderer(new ActionButtonsRenderer());
        columns.getColumn(3).setCellEditor(new ActionButtonsEditor());
    }

    // Helper method for action buttons
    private JButton createPrimaryButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_LIGHT);
        button.setBackground(color);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e) { button.setBackground(color); }
        });

        return button;
    }

    private JButton createToolbarButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setForeground(color);
        button.setBackground(BG_PRIMARY);
        button.setBorder(BorderFactory.createLineBorder(color));
        button.setFocusPainted(false);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color);
                button.setForeground(TEXT_LIGHT);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(BG_PRIMARY);
                button.setForeground(color);
            }
        });

        return button;
    }

    private void styleScrollBars(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    }

    private void applySearch() {
        tableModel.filterByName(searchField.getText());
    }

    // Action Buttons Renderer and Editor
    private class ActionButtonsRenderer extends JPanel implements TableCellRenderer {
        private final JButton editButton;
        private final JButton deleteButton;

        public ActionButtonsRenderer() {
            setLayout(new MigLayout("insets 0", "[]4[]", "[]"));
            setBackground(BG_PRIMARY);

            editButton = createActionButton("Edit", INFO);
            deleteButton = createActionButton("Delete", DANGER);

            add(editButton);
            add(deleteButton);
        }

        private JButton createActionButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFont(BUTTON_FONT);
            button.setForeground(color);
            button.setBackground(BG_PRIMARY);
            button.setBorder(BorderFactory.createLineBorder(color, 1));
            button.setPreferredSize(new Dimension(60, 28));
            return button;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() :
                    row % 2 == 0 ? BG_PRIMARY : BG_SECONDARY);
            return this;
        }
    }

    private class ActionButtonsEditor extends DefaultCellEditor {
        private final JPanel buttonPanel;

        public ActionButtonsEditor() {
            super(new JCheckBox());

            buttonPanel = new JPanel(new MigLayout("insets 0", "[]4[]", "[]"));
            buttonPanel.setBackground(BG_PRIMARY);

            JButton editButton = createActionButton("Edit", INFO);
            JButton deleteButton = createActionButton("Delete", DANGER);

            editButton.addActionListener(e -> {
                stopCellEditing();
                Department dept = tableModel.getDepartmentAt(departmentTable.getSelectedRow());
                if (dept != null) {
                    showEditDialog(dept);
                }
            });

            deleteButton.addActionListener(e -> {
                stopCellEditing();
                Department dept = tableModel.getDepartmentAt(departmentTable.getSelectedRow());
                if (dept != null) {
                    showDeleteConfirmation(dept);
                }
            });

            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            buttonPanel.setBackground(table.getSelectionBackground());
            return buttonPanel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // Dialog Management
    private void showEditDialog(Department department) {
        boolean isNew = department == null;
        department = isNew ? new Department() : department;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Add Department" : "Edit Department", true);
        dialog.setLayout(new MigLayout("fillx, wrap 2, insets 20", "[][grow]", "[]20[]"));
        dialog.getContentPane().setBackground(BG_PRIMARY);

        // Title
        JLabel title = new JLabel(isNew ? "Add New Department" : "Edit Department");
        title.setFont(HEADER_FONT);
        title.setForeground(TEXT_PRIMARY);
        dialog.add(title, "span 2, center, gapbottom 10");

        // Form Fields
        JTextField nameField = createFormTextField();
        nameField.setText(department.getName());

        dialog.add(createFormLabel("Department Name:*"));
        dialog.add(nameField, "growx");

        // Buttons
        Department finalDepartment = department;
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]8[]"));
        buttonPanel.setBackground(BG_PRIMARY);

        JButton cancelBtn = createToolbarButton("Cancel", TEXT_SECONDARY);
        JButton saveBtn = createPrimaryButton(isNew ? "Create" : "Update", MAIN_COLOR);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (validateForm(nameField.getText())) {
                saveDepartment(finalDepartment, nameField.getText());
                dialog.dispose();
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        dialog.add(buttonPanel, "span 2, growx");

        // Show dialog
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showDeleteConfirmation(Department department) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Confirm Delete", true);
        dialog.setLayout(new MigLayout("fillx, wrap, insets 20", "[grow]", "[]10[]"));
        dialog.getContentPane().setBackground(BG_PRIMARY);

        // Warning message
        JLabel warningIcon = new JLabel("⚠️");
        warningIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        JLabel message = new JLabel("Delete department '" + department.getName() + "'?");
        message.setFont(BUTTON_FONT);

        JLabel subMessage = new JLabel("This action cannot be undone.");
        subMessage.setFont(REGULAR_FONT);
        subMessage.setForeground(TEXT_SECONDARY);

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]8[]"));
        buttonPanel.setBackground(BG_PRIMARY);

        JButton cancelBtn = createToolbarButton("Cancel", TEXT_SECONDARY);
        JButton deleteBtn = createPrimaryButton("Delete", DANGER);

        cancelBtn.addActionListener(e -> dialog.dispose());
        deleteBtn.addActionListener(e -> {
            deleteDepartment(department);
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(deleteBtn);

        // Add components
        dialog.add(warningIcon, "center");
        dialog.add(message, "center");
        dialog.add(subMessage, "center");
        dialog.add(buttonPanel, "growx");

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Form Components
    private JTextField createFormTextField() {
        JTextField field = new JTextField();
        field.setFont(REGULAR_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(MAIN_COLOR),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        return field;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(REGULAR_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    // Data Operations
    private void saveDepartment(Department department, String name) {
        try {
            department.setName(name);
            if (department.getId() == null) {
                departmentDAO.createDepartment(department);
                showNotification("Department created successfully", SUCCESS);
            } else {
                departmentDAO.updateDepartment(department);
                showNotification("Department updated successfully", SUCCESS);
            }
            refreshData();
        } catch (SQLException e) {
            showNotification("Error saving department: " + e.getMessage(), DANGER);
        }
    }

    private void deleteDepartment(Department department) {
        try {
            departmentDAO.deleteDepartment(department.getId());
            refreshData();
            showNotification("Department deleted successfully", SUCCESS);
        } catch (SQLException e) {
            showNotification("Error deleting department: " + e.getMessage(), DANGER);
        }
    }

    void refreshData() {
        try {
            tableModel.setData(departmentDAO.findAll());
        } catch (SQLException e) {
            showNotification("Error loading departments: " + e.getMessage(), DANGER);
        }
    }

    // Notifications
    private void showNotification(String message, Color color) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), String.valueOf(true));
        dialog.setUndecorated(true);
        dialog.setLayout(new MigLayout("fillx, insets 15", "[]push[]"));
        dialog.getContentPane().setBackground(color);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(BUTTON_FONT);
        messageLabel.setForeground(TEXT_LIGHT);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeBtn.setForeground(TEXT_LIGHT);
        closeBtn.setBorder(null);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.add(messageLabel);
        dialog.add(closeBtn);

        dialog.pack();
        dialog.setLocationRelativeTo(null);

        // Auto-dismiss after 3 seconds
        Timer timer = new Timer(3000, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
    }

    // Form Validation
    private boolean validateForm(String name) {
        if (name == null || name.trim().isEmpty()) {
            showNotification("Department name is required", WARNING);
            return false;
        }
        return true;
    }

    // Custom ScrollBar UI
    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = MAIN_COLOR;
            this.trackColor = BG_SECONDARY;
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
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(thumbColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            g2.dispose();
        }
    }
}