package io.hahnsoftware.emp.ui;

import net.miginfocom.swing.MigLayout;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;


import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;
import java.util.Date;
import java.util.function.BiConsumer;

public class DateRangeDialog extends JDialog {
    private final JDatePickerImpl startDatePicker;
    private final JDatePickerImpl endDatePicker;
    private final BiConsumer<LocalDate, LocalDate> onSelect;

    public DateRangeDialog(Frame owner, BiConsumer<LocalDate, LocalDate> onSelect) {
        super(owner, "Select Date Range", true);
        this.onSelect = onSelect;

        setLayout(new MigLayout("wrap 2, fillx, insets 20", "[][grow]", "[]10[]20[]"));

        // Configure date picker properties
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");

        // Create start date picker
        UtilDateModel startModel = new UtilDateModel();
        JDatePanelImpl startDatePanel = new JDatePanelImpl(startModel);
        startDatePicker = new JDatePickerImpl(startDatePanel, new DateLabelFormatter());

        // Create end date picker
        UtilDateModel endModel = new UtilDateModel();
        JDatePanelImpl endDatePanel = new JDatePanelImpl(endModel);
        endDatePicker = new JDatePickerImpl(endDatePanel, new DateLabelFormatter());

        // Add components
        add(new JLabel("Start Date:"));
        add(startDatePicker, "growx");
        add(new JLabel("End Date:"));
        add(endDatePicker, "growx");

        // Create buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        add(buttonPanel, "span 2, growx");

        // Add button actions
        okButton.addActionListener(e -> {
            handleOK();
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        // Set dialog properties
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void handleOK() {
        LocalDate startDate = null;
        LocalDate endDate = null;

        // Convert start date
        Date startValue = (Date) startDatePicker.getModel().getValue();
        if (startValue != null) {
            startDate = startValue.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // Convert end date
        Date endValue = (Date) endDatePicker.getModel().getValue();
        if (endValue != null) {
            endDate = endValue.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // Validate date range
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(this,
                    "End date cannot be before start date",
                    "Invalid Date Range",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        onSelect.accept(startDate, endDate);
    }

    public static void show(Frame owner, BiConsumer<LocalDate, LocalDate> onSelect) {
        SwingUtilities.invokeLater(() -> {
            DateRangeDialog dialog = new DateRangeDialog(owner, onSelect);
            dialog.setVisible(true);
        });
    }
}