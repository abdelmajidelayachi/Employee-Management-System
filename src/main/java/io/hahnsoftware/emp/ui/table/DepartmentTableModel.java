package io.hahnsoftware.emp.ui.table;

import io.hahnsoftware.emp.dao.DepartmentDAO;
import io.hahnsoftware.emp.dao.EmployeeDAO;
import io.hahnsoftware.emp.model.Department;
import io.hahnsoftware.emp.ui.StyleConstants;

import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentTableModel extends AbstractTableModel implements StyleConstants {
    private static final String[] COLUMNS = {"ID", "Name", "Manager", "Actions"};
    private final List<Department> departments;
    private boolean[] editable;

    public DepartmentTableModel() {
        this.departments = new ArrayList<>();
        this.editable = new boolean[]{false, false, false, true}; // Only actions column is editable
    }

    @Override
    public int getRowCount() {
        return departments.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0: return Long.class;    // ID
            case 3: return String.class;   // Actions
            default: return String.class;  // Name, Manager
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editable[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row >= departments.size()) return null;

        Department dept = departments.get(row);
        switch (column) {
            case 0: return dept.getId();
            case 1: return dept.getName();
            case 2: return dept.getManager() != null ? dept.getManager().getFullName() : "-";
            case 3: return dept;  // Return department object for actions
            default: return null;
        }
    }

    // Data management
    public void setData(List<Department> departments) {
        this.departments.clear();
        if (departments != null) {
            this.departments.addAll(departments);
        }
        fireTableDataChanged();
    }

    public void addDepartment(Department department) {
        if (department != null) {
            departments.add(department);
            fireTableRowsInserted(departments.size() - 1, departments.size() - 1);
        }
    }

    public void updateDepartment(int row, Department department) {
        if (row >= 0 && row < departments.size() && department != null) {
            departments.set(row, department);
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeDepartment(int row) {
        if (row >= 0 && row < departments.size()) {
            departments.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public Department getDepartmentAt(int row) {
        return (row >= 0 && row < departments.size()) ? departments.get(row) : null;
    }

    // Search and Sort functionality
    public void filterByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            try {
                DepartmentDAO departmentDAO = new DepartmentDAO(new EmployeeDAO());
                setData(departmentDAO.findAll());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        List<Department> filtered = new ArrayList<>();
        String term = searchTerm.toLowerCase().trim();

        for (Department dept : departments) {
            boolean matchesName = dept.getName().toLowerCase().contains(term);
            boolean matchesManager = dept.getManager() != null &&
                    dept.getManager().getFullName().toLowerCase().contains(term);

            if (matchesName || matchesManager) {
                filtered.add(dept);
            }
        }

        setData(filtered);
    }
    public void sortByColumn(int column, boolean ascending) {
        departments.sort((d1, d2) -> {
            int result;
            switch (column) {
                case 0: // ID
                    result = d1.getId().compareTo(d2.getId());
                    break;
                case 1: // Name
                    result = d1.getName().compareToIgnoreCase(d2.getName());
                    break;
                case 2: // Manager
                    String m1 = d1.getManager() != null ? d1.getManager().getFullName() : "";
                    String m2 = d2.getManager() != null ? d2.getManager().getFullName() : "";
                    result = m1.compareToIgnoreCase(m2);
                    break;
                default:
                    return 0;
            }
            return ascending ? result : -result;
        });
        fireTableDataChanged();
    }
}