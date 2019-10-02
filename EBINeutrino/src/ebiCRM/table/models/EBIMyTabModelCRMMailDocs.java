package ebiCRM.table.models;

import ebiNeutrinoSDK.EBISystem;

import javax.swing.table.AbstractTableModel;

public class EBIMyTabModelCRMMailDocs extends AbstractTableModel {


    public String[] columnNames = {
            EBISystem.i18n("EBI_LANG_ID"),
            EBISystem.i18n("EBI_LANG_FILENAME"),
    };
    public Object[][] data = {{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "",}};

    public Object[][] dax = {{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "",}};

    public void addRow(final int row, final Object[] cols) {
        if (data.length == 0 && data[0][0].equals(EBISystem.i18n("EBI_LANG_PLEASE_SELECT")) && dax.length == 0 && dax[0][0].equals(EBISystem.i18n("EBI_LANG_PLEASE_SELECT"))) {
            data = new Object[1][2];
            dax = new Object[1][5];
        }

        if (row >= dax.length) {

            final Object[][] tmp = new Object[row + 1][5];
            for (int i = 0; i < dax.length; i++) {
                tmp[i] = dax[i];
            }
            dax = tmp;


            final Object[][] tmp_1 = new Object[row + 1][2];
            for (int x = 0; x < data.length; x++) {
                tmp_1[x] = data[x];
            }
            data = tmp_1;
        }

        data[row][0] = cols[3];
        data[row][1] = cols[4];
        data[row][2] = cols[5];
        data[row][3] = cols[6];
        data[row][4] = cols[7];
        data[row][5] = cols[8];
        dax[row] = cols;

    }

    public void removeRow(final int row) {
        Object[][] tmp = new Object[data.length - 1][5];
        int zahler = 0;
        for (int i = 0; i < data.length; i++) {
            if (i != row) {
                tmp[zahler] = data[i];
                zahler++;
            }
        }

        if (tmp.length == 0) {
            tmp = new Object[1][5];
            tmp[0][0] = EBISystem.i18n("EBI_LANG_PLEASE_SELECT");
            tmp[0][1] = "";
            tmp[0][2] = "";
            tmp[0][3] = "";
            tmp[0][4] = "";
            tmp[0][5] = "";
        }
        data = tmp;
        dax = tmp;
    }

    public Object[] getRow(final int row) {
        if (row < 0 || row > data.length - 1) {
            return null;
        }

        return data[row];
    }

    public void setRow(final int row, final Object[] rowData) {
        if (row < 0 || row > data.length - 1) {
            return;
        }

        data[row][0] = rowData[3];
        data[row][1] = rowData[4];
        data[row][2] = rowData[5];
        data[row][3] = rowData[6];
        data[row][4] = rowData[7];
        data[row][5] = rowData[8];
        dax[row] = rowData;

    }

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
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
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