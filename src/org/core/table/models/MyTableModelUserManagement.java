package org.core.table.models;

import org.sdk.EBISystem;

import javax.swing.table.AbstractTableModel;

public class MyTableModelUserManagement extends AbstractTableModel {

    public String[] columnNames = {"Id",
            EBISystem.i18n("EBI_LANG_USER"),
            EBISystem.i18n("EBI_LANG_PASSWORD"),
            EBISystem.i18n("EBI_LANG_ADDED"),
            EBISystem.i18n("EBI_LANG_ADDED_FROM"),
            EBISystem.i18n("EBI_LANG_CHANGED"),
            EBISystem.i18n("EBI_LANG_CHANGED_FROM"),
            EBISystem.i18n("EBI_LANG_IS_ADMIN"),
            EBISystem.i18n("EBI_LANG_SAVE"),
            EBISystem.i18n("EBI_LANG_DELETE"),
            EBISystem.i18n("EBI_LANG_PRINT")
    };
    public Object[][] data = {{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", "", "", "", "", ""}};

    @Override
	public int getColumnCount() {
        return columnNames.length;
    }

    @Override
	public int getRowCount() {
        return data.length;
    }

    @Override
	public String getColumnName(final int col) {
        return columnNames[col];
    }

    @Override
	public Object getValueAt(final int row, final int col) {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @Override
	public Class getColumnClass(final int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
	public boolean isCellEditable(final int row, final int col) {
        return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
	public void setValueAt(final Object value, final int row, final int col) {

        if (data[0][col] instanceof Integer
                && !(value instanceof Integer)) {
            try {
                data[row][col] = Integer.valueOf(value.toString());
                fireTableCellUpdated(row, col);
            } catch (final NumberFormatException e) {

            }
        } else {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

    }

}
