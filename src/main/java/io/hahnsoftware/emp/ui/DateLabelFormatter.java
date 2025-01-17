package io.hahnsoftware.emp.ui;

import javax.swing.JFormattedTextField.AbstractFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateLabelFormatter extends AbstractFormatter {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Object stringToValue(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateFormatter.parse(text));
        return cal;
    }

    @Override
    public String valueToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Calendar) {
            return dateFormatter.format(((Calendar) value).getTime());
        }
        return "";
    }
}