package io.hahnsoftware.emp.ui;

import io.hahnsoftware.emp.dao.EmployeeDAO;
import net.miginfocom.swing.MigLayout;
import io.hahnsoftware.emp.dao.AuditDAO;
import io.hahnsoftware.emp.model.AuditLog;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class AuditLogPanel extends JPanel {
    private JTable auditTable;
    private DefaultTableModel tableModel;
    private AuditDAO auditDAO;
    private JComboBox<String> entityFilter;
    private JComboBox<String> actionFilter;
    private JDatePickerImpl startDatePicker;
    private JDatePickerImpl endDatePicker;

    public AuditLogPanel() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]10[grow]"));

        try {
            auditDAO = new AuditDAO(new EmployeeDAO());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize AuditDAO", e);
        }

        // Create filter panel
        JPanel filterPanel = createFilterPanel();

        // Create table
        String[] columns = {"Timestamp", "Action", "Entity Type", "Entity ID", "User", "Changes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        auditTable = new JTable(tableModel);
        auditTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // Set column widths
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Timestamp
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Action
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Entity Type
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Entity ID
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(100); // User
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(300); // Changes

        // Add components
        add(filterPanel, "growx, wrap");
        add(new JScrollPane(auditTable), "grow");

        // Initial load
        refreshData();
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[]5[]5[]5[]push[]", "[]"));

        // Initialize filters
        entityFilter = new JComboBox<>(new String[]{"All Entities", "EMPLOYEE", "DEPARTMENT"});
        actionFilter = new JComboBox<>(new String[]{"All Actions", "CREATE", "UPDATE", "DELETE"});

        // Date pickers setup
        UtilDateModel startModel = new UtilDateModel();
        UtilDateModel endModel = new UtilDateModel();

        JDatePanelImpl startDatePanel = new JDatePanelImpl(startModel);
        JDatePanelImpl endDatePanel = new JDatePanelImpl(endModel);

        startDatePicker = new JDatePickerImpl(startDatePanel, new DateLabelFormatter());
        endDatePicker = new JDatePickerImpl(endDatePanel, new DateLabelFormatter());

        // Add listeners
        entityFilter.addActionListener(e -> refreshData());
        actionFilter.addActionListener(e -> refreshData());

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        // Add components
        panel.add(new JLabel("Entity:"));
        panel.add(entityFilter);
        panel.add(new JLabel("Action:"));
        panel.add(actionFilter);
        panel.add(new JLabel("Start Date:"));
        panel.add(startDatePicker);
        panel.add(new JLabel("End Date:"));
        panel.add(endDatePicker);
        panel.add(refreshButton, "right");

        return panel;
    }


    void refreshData() {
        try {
            // Get filter values
            String entity = entityFilter.getSelectedItem().toString().equalsIgnoreCase("All Entities") ? null : entityFilter.getSelectedItem().toString() ;
            String action = actionFilter.getSelectedItem().toString().equalsIgnoreCase("All Actions") ?  null :actionFilter.getSelectedItem().toString();
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            
            if (startDatePicker.getModel().getValue() != null) {
                startDate = ((Date) startDatePicker.getModel().getValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().atStartOfDay();
            }
            
            if (endDatePicker.getModel().getValue() != null) {
                endDate = ((Date) endDatePicker.getModel().getValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().atStartOfDay();
            }
            
            // Apply filters
//            List<AuditLog> logs = auditDAO.getFilteredLogs(
//                entity.equals("All Entities") ? null : entity,
//                action.equals("All Actions") ? null : action,
//                startDate,
//                endDate
//            );

            List<AuditLog> logs = auditDAO.getAuditLogs(entity, action, startDate, endDate);

            // Update table
            tableModel.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (AuditLog log : logs) {
                Object[] row = {
                        formatter.format(log.getTimestamp()),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getEmployee().getUsername(),
                        log.getChanges()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            showError("Error loading audit logs", e);
        }
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this,
                message + ": " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // Custom date formatter for JDatePicker
    private class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws java.text.ParseException {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return LocalDate.parse(text, dateFormatter);
        }

        @Override
        public String valueToString(Object value) {
            if (value == null) {
                return "";
            }
            if (value instanceof LocalDate) {
                return dateFormatter.format((LocalDate) value);
            }
            return value.toString();
        }
    }
}