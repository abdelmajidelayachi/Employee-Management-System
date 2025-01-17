package io.hahnsoftware.emp.ui.date;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DatePickerComponent extends JDatePickerImpl {

    public DatePickerComponent() {
        super(new JDatePanelImpl(new UtilDateModel()), new DateLabelFormatter());

        // Style the date picker
        JFormattedTextField textField = getJFormattedTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        textField.setPreferredSize(new Dimension(200, 38));
    }

    public void setDate(LocalDate date) {
        if (date != null) {
            getModel().setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
            getModel().setSelected(true);
        } else {
            getModel().setValue(null);
            getModel().setSelected(false);
        }
    }

    public LocalDate getDate() {
        Date selectedDate = (Date) getModel().getValue();
        return selectedDate != null
                ? selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                : null;
    }

    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value instanceof Date) {
                return dateFormatter.format((Date) value);
            } else if (value instanceof LocalDate) {
                return ((LocalDate) value).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return "";
        }
    }
}