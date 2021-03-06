package org.modules.views;

import org.modules.controls.ControlBank;
import org.modules.views.dialogs.EBICRMHistoryView;
import org.modules.models.ModelCRMBankdata;
import org.core.gui.callbacks.EBIUICallback;
import org.sdk.EBISystem;
import org.sdk.gui.dialogs.EBIExceptionDialog;
import org.sdk.gui.dialogs.EBIMessage;
import org.jdesktop.swingx.sort.RowFilters;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import lombok.Getter;
import lombok.Setter;

public class EBICRMBankView {

    @Getter
    @Setter
    private ModelCRMBankdata tabModel = null;
    @Getter
    @Setter
    private ControlBank bankDataControl = new ControlBank();
    private int selectedRow = -1;

    public void initializeAction() {
        EBISystem.gui().textField("filterTableText", "Bank").addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(final KeyEvent e) {
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                EBISystem.gui().table("companyBankTable", "Bank").setRowFilter(RowFilters.regexFilter("(?i)" + EBISystem.gui().textField("filterTableText", "Bank").getText()));
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                EBISystem.gui().table("companyBankTable", "Bank").setRowFilter(RowFilters.regexFilter("(?i)" + EBISystem.gui().textField("filterTableText", "Bank").getText()));
            }
        });

        EBISystem.gui().table("companyBankTable", "Bank").setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EBISystem.gui().table("companyBankTable", "Bank").addSelectionListener(new EBIUICallback() {
            @Override
            public void selectionListenerEvent(ListSelectionEvent e) {
                super.selectionListenerEvent(e);
                final ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (EBISystem.gui().table("companyBankTable", "Bank").getSelectedRow() != -1) {
                    selectedRow = EBISystem.gui().table("companyBankTable", "Bank").convertRowIndexToModel(EBISystem.gui().table("companyBankTable", "Bank").getSelectedRow());

                    if (lsm.isSelectionEmpty()) {
                        EBISystem.gui().button("editBank", "Bank").setEnabled(false);
                        EBISystem.gui().button("deleteBank", "Bank").setEnabled(false);
                        EBISystem.gui().button("historyBank", "Bank").setEnabled(false);
                        EBISystem.gui().button("copyBank", "Bank").setEnabled(false);
                    } else if (!EBISystem.i18n("EBI_LANG_PLEASE_SELECT").equals(tabModel.data[selectedRow][0].toString())) {
                        EBISystem.gui().button("editBank", "Bank").setEnabled(true);
                        EBISystem.gui().button("deleteBank", "Bank").setEnabled(true);
                        EBISystem.gui().button("historyBank", "Bank").setEnabled(true);
                        EBISystem.gui().button("copyBank", "Bank").setEnabled(true);

                    }
                }
            }
        });

        EBISystem.gui().table("companyBankTable", "Bank").addKeyAction(new EBIUICallback() {
            @Override
            public void tableKeyUp(int selRow) {
                super.tableKeyUp(selRow);
                selectedRow = selRow;
                editBank();
            }

            @Override
            public void tableKeyDown(int selRow) {
                super.tableKeyDown(selRow);
                selectedRow = selRow;
                editBank();
            }

            @Override
            public void tableKeyEnter(int selRow) {
                super.tableKeyEnter(selRow);
                selectedRow = selRow;
                editBank();
            }
        });

        EBISystem.gui().table("companyBankTable", "Bank").setMouseCallback(new MouseAdapter() {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent e) {
                if (EBISystem.gui().table("companyBankTable", "Bank").rowAtPoint(e.getPoint()) != -1) {
                    selectedRow = EBISystem.gui().table("companyBankTable", "Bank").convertRowIndexToModel(EBISystem.gui().table("companyBankTable", "Bank").rowAtPoint(e.getPoint()));
                }
                editBank();
            }
        });
    }

    public void initialize(boolean reload) {
        if (reload) {
            tabModel = new ModelCRMBankdata();
            EBISystem.gui().table("companyBankTable", "Bank").setModel(tabModel);
        }
        EBISystem.gui().vpanel("Bank").setCreatedDate(EBISystem.getInstance().getDateToString(new java.util.Date()));
        EBISystem.gui().vpanel("Bank").setCreatedFrom(EBISystem.ebiUser);
        EBISystem.gui().vpanel("Bank").setChangedDate("");
        EBISystem.gui().vpanel("Bank").setChangedFrom("");

        EBISystem.gui().textField("bankNameText", "Bank").setText("");
        EBISystem.gui().textField("abaNrText", "Bank").setText("");
        EBISystem.gui().textField("accountNrText", "Bank").setText("");
        EBISystem.gui().textField("bicText", "Bank").setText("");
        EBISystem.gui().textField("ibanText", "Bank").setText("");
        EBISystem.gui().textField("countryBankText", "Bank").setText("");
    }

    public void newBank() {
        EBISystem.showInActionStatus("Bank");
        bankDataControl.dataNew();
        bankDataControl.setEdit(false);
    }

    public void editBank() {
        if ((selectedRow < 0
                || EBISystem.i18n("EBI_LANG_PLEASE_SELECT")
                        .equals(tabModel.data[selectedRow][0].toString()))) {
            return;
        }
        EBISystem.showInActionStatus("Bank");
        bankDataControl.dataNew();
        bankDataControl.dataEdit(Integer.parseInt(tabModel.data[selectedRow][6].toString()));
        bankDataControl.setEdit(true);
    }

    public void copyBank() {
        if ((selectedRow < 0
                || EBISystem.i18n("EBI_LANG_PLEASE_SELECT")
                        .equals(tabModel.data[selectedRow][0].toString()))) {
            return;
        }
        EBISystem.showInActionStatus("Bank");
        Integer id = bankDataControl.dataCopy(Integer.parseInt(tabModel.data[selectedRow][6].toString()));
        bankDataControl.dataEdit(id);
        bankDataControl.dataShow(id);
        bankDataControl.setEdit(true);
    }

    public boolean saveBank() {
        if (!validateInput()) {
            return false;
        }
        EBISystem.showInActionStatus("Bank");
        int row = EBISystem.gui().table("companyBankTable", "Bank").getSelectedRow();
        Integer id = bankDataControl.dataStore();
        bankDataControl.dataShow(id);
        EBISystem.gui().table("companyBankTable", "Bank").changeSelection(row, 0, false, false);
        return true;
    }

    public void deleteBank() {
        if (selectedRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(tabModel.data[selectedRow][0].toString())) {
            return;
        }
        if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_MESSAGE_DELETE_RECORD")).Show(EBIMessage.WARNING_MESSAGE_YESNO) == true) {
            EBISystem.showInActionStatus("Bank");
            bankDataControl.dataDelete(Integer.parseInt(tabModel.data[selectedRow][6].toString()));
            bankDataControl.dataNew();
            bankDataControl.dataShow(-1);
            bankDataControl.setEdit(false);
        }
    }

    public void historyBank() {
        new EBICRMHistoryView(EBISystem.getModule().hcreator.retrieveDBHistory(EBISystem.getInstance().getCompany().getCompanyid(), "Bankdata")).setVisible();
    }

    private boolean validateInput() {
        boolean ret = true;
        if ("".equals(EBISystem.gui().textField("bankNameText", "Bank").getText())) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_ERROR_INSERT_BANK_NAME")).Show(EBIMessage.ERROR_MESSAGE);
            ret = false;
        } else if ("".equals(EBISystem.gui().textField("abaNrText", "Bank").getText())) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_ERROR_INSERT_BANK_CODE")).Show(EBIMessage.ERROR_MESSAGE);
            ret = false;
        } else if ("".equals(EBISystem.gui().textField("accountNrText", "Bank").getText())) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_ERROR_INSERT_BANK_NR")).Show(EBIMessage.ERROR_MESSAGE);
            ret = false;
        }
        return ret;
    }
}
