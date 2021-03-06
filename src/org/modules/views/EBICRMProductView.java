package org.modules.views;

import org.modules.controls.ControlProduct;
import org.modules.views.dialogs.EBICRMHistoryView;
import org.modules.models.ModelDoc;
import org.modules.models.ModelProduct;
import org.modules.models.ModelProductDependency;
import org.modules.models.ModelProperties;
import org.core.gui.callbacks.EBIUICallback;
import org.sdk.EBISystem;
import org.sdk.gui.dialogs.EBIExceptionDialog;
import org.sdk.gui.dialogs.EBIMessage;
import org.jdesktop.swingx.sort.RowFilters;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.text.NumberFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

public class EBICRMProductView {

    @Getter
    @Setter
    private ModelDoc tabModDoc = null;
    @Getter
    @Setter
    private ModelProductDependency productDependencyModel = null;
    @Getter
    @Setter
    private ModelProduct productModel = null;
    @Getter
    @Setter
    private ModelProperties productModelDimension = null;

    public static String[] category = null;
    public static String[] type = null;
    public static String[] taxType = null;
    @Getter
    @Setter
    private ControlProduct dataControlProduct = new ControlProduct();

    private int selectedProductRow = -1;
    private int selectedDimensionRow = -1;
    private int selectedDependencyRow = -1;
    private int selectedDocRow = -1;

    public void initializeAction() {
        EBISystem.gui().textField("filterTableText", "Product").addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(final KeyEvent e) {
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                EBISystem.gui().table("companyProductTable", "Product").setRowFilter(RowFilters.regexFilter("(?i)" + EBISystem.gui().textField("filterTableText", "Product").getText()));
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                EBISystem.gui().table("companyProductTable", "Product").setRowFilter(RowFilters.regexFilter("(?i)" + EBISystem.gui().textField("filterTableText", "Product").getText()));
            }
        });

        // TAX PANEL
        EBISystem.gui().combo("productTaxTypeTex", "Product").addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                if (!EBISystem.i18n("EBI_LANG_PLEASE_SELECT").equals(EBISystem.gui().combo("productTaxTypeTex", "Product").getSelectedItem().toString())) {
                    EBISystem.gui().button("calcClear", "Product").setEnabled(true);
                    EBISystem.gui().button("calcGross", "Product").setEnabled(true);
                }
            }
        });

        final NumberFormat taxFormat = NumberFormat.getNumberInstance();
        taxFormat.setMinimumFractionDigits(2);
        taxFormat.setMaximumFractionDigits(3);

        EBISystem.gui().FormattedField("productGrossText", "Product").setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(taxFormat)));

        EBISystem.gui().FormattedField("productNetamoutText", "Product").setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(taxFormat)));
        EBISystem.gui().FormattedField("salePriceText", "Product").setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(taxFormat)));

        /**
         * ***********************************************************************************
         */
        //  PRODUCT TABLE PROPERTIES
        /**
         * ***********************************************************************************
         */
        EBISystem.gui().table("ProductPropertiesTable", "Product").setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EBISystem.gui().table("ProductPropertiesTable", "Product").addSelectionListener(new EBIUICallback() {
            @Override
            public void selectionListenerEvent(ListSelectionEvent e) {
                super.selectionListenerEvent(e);
                final ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (lsm.getMinSelectionIndex() != -1) {
                    selectedDimensionRow = EBISystem.gui().table("ProductPropertiesTable", "Product").convertRowIndexToModel(lsm.getMinSelectionIndex());
                }

                if (lsm.isSelectionEmpty()) {
                    EBISystem.gui().button("deleteProperties", "Product").setEnabled(false);
                    EBISystem.gui().button("editProperties", "Product").setEnabled(false);
                } else if (!productModelDimension.data[selectedDimensionRow][0].toString().equals(EBISystem.i18n("EBI_LANG_PLEASE_SELECT"))) {
                    EBISystem.gui().button("deleteProperties", "Product").setEnabled(true);
                    EBISystem.gui().button("editProperties", "Product").setEnabled(true);
                }
            }
        });

        /**
         * ***********************************************************************************
         */
        //  PRODUCT TABLE RELATION
        /**
         * ***********************************************************************************
         */
        EBISystem.gui().table("ProductRelationTable", "Product").setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EBISystem.gui().table("ProductRelationTable", "Product").addSelectionListener(new EBIUICallback() {
            @Override
            public void selectionListenerEvent(ListSelectionEvent e) {
                super.selectionListenerEvent(e);
                final ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (lsm.getMinSelectionIndex() != -1) {
                    selectedDependencyRow = EBISystem.gui().table("ProductRelationTable", "Product").convertRowIndexToModel(lsm.getMinSelectionIndex());
                }

                if (lsm.isSelectionEmpty()) {
                    EBISystem.gui().button("deleteRelation", "Product").setEnabled(false);
                } else if (!productDependencyModel.data[selectedDependencyRow][0].toString().equals(EBISystem.i18n("EBI_LANG_PLEASE_SELECT"))) {
                    EBISystem.gui().button("deleteRelation", "Product").setEnabled(true);
                }
            }
        });

        /**
         * ***********************************************************************************
         */
        //  PRODUCT TABLE DOCUMENT
        /**
         * ***********************************************************************************
         */
        EBISystem.gui().table("productTableDoc", "Product").setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EBISystem.gui().table("productTableDoc", "Product").addSelectionListener(new EBIUICallback() {
            @Override
            public void selectionListenerEvent(ListSelectionEvent e) {
                super.selectionListenerEvent(e);

                final ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (lsm.getMinSelectionIndex() != -1) {
                    selectedDocRow = EBISystem.gui().table("productTableDoc", "Product").convertRowIndexToModel(lsm.getMinSelectionIndex());
                }

                if (selectedDocRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                        equals(tabModDoc.data[selectedDocRow][0].toString())) {
                    return;
                }

                if (lsm.isSelectionEmpty()) {
                    EBISystem.gui().button("showProductDoc", "Product").setEnabled(false);
                    EBISystem.gui().button("deleteProductDoc", "Product").setEnabled(false);
                } else if (!tabModDoc.data[selectedDocRow][0].toString().equals(EBISystem.i18n("EBI_LANG_PLEASE_SELECT"))) {
                    EBISystem.gui().button("showProductDoc", "Product").setEnabled(true);
                    EBISystem.gui().button("deleteProductDoc", "Product").setEnabled(true);
                    dataControlProduct.loadDocsToImageView(Integer.parseInt(tabModDoc.data[selectedDocRow][3].toString()));
                }
            }
        });

        /**
         * ***********************************************************************************
         */
        //  PRODUCT AVAILABLE TABLE
        /**
         * ***********************************************************************************
         */
        EBISystem.gui().table("companyProductTable", "Product").setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EBISystem.gui().table("companyProductTable", "Product").addSelectionListener(new EBIUICallback() {
            @Override
            public void selectionListenerEvent(ListSelectionEvent e) {
                super.selectionListenerEvent(e);
                final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.getMinSelectionIndex() != -1) {
                    selectedProductRow = EBISystem.gui().table("companyProductTable", "Product").convertRowIndexToModel(lsm.getMinSelectionIndex());
                }

                if (lsm.isSelectionEmpty()) {
                    EBISystem.gui().button("editProduct", "Product").setEnabled(false);
                    EBISystem.gui().button("deleteProduct", "Product").setEnabled(false);
                    EBISystem.gui().button("historyProduct", "Product").setEnabled(false);
                    EBISystem.gui().button("reportProduct", "Product").setEnabled(false);
                    EBISystem.gui().button("copyProduct", "Product").setEnabled(false);
                } else if (!productModel.data[selectedProductRow][0].toString().equals(EBISystem.i18n("EBI_LANG_PLEASE_SELECT"))) {
                    EBISystem.gui().button("editProduct", "Product").setEnabled(true);
                    EBISystem.gui().button("deleteProduct", "Product").setEnabled(true);
                    EBISystem.gui().button("historyProduct", "Product").setEnabled(true);
                    EBISystem.gui().button("reportProduct", "Product").setEnabled(true);
                    EBISystem.gui().button("copyProduct", "Product").setEnabled(true);
                }
            }
        });

        EBISystem.gui().table("companyProductTable", "Product").addKeyAction(new EBIUICallback() {
            @Override
            public void tableKeyUp(int selRow) {
                super.tableKeyUp(selRow);
                selectedProductRow = selRow;
                editProduct();
            }

            @Override
            public void tableKeyDown(int selRow) {
                super.tableKeyDown(selRow);
                selectedProductRow = selRow;
                editProduct();
            }

            @Override
            public void tableKeyEnter(int selRow) {
                super.tableKeyEnter(selRow);
                selectedProductRow = selRow;
                if (selectedProductRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                        equals(productModel.data[selectedProductRow][0].toString())) {
                    return;
                }
                editProduct();
            }
        });

        EBISystem.gui().table("companyProductTable", "Product").setMouseCallback(new MouseAdapter() {
            @Override
            public void mouseReleased(final java.awt.event.MouseEvent e) {
                if (EBISystem.gui().table("companyProductTable", "Product").rowAtPoint(e.getPoint()) != -1) {
                    selectedProductRow = EBISystem.gui().table("companyProductTable", "Product").convertRowIndexToModel(EBISystem.gui().table("companyProductTable", "Product").rowAtPoint(e.getPoint()));
                }
                if (selectedProductRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                        equals(productModel.data[selectedProductRow][0].toString())) {
                    return;
                }
                editProduct();
            }
        });
    }

    public void initialize(boolean reload) {
        if (reload) {
            productDependencyModel = new ModelProductDependency();
            productModel = new ModelProduct();
            productModelDimension = new ModelProperties();
            tabModDoc = new ModelDoc();

            EBISystem.gui().table("ProductPropertiesTable", "Product").setModel(productModelDimension);
            EBISystem.gui().table("ProductRelationTable", "Product").setModel(productDependencyModel);
            EBISystem.gui().table("productTableDoc", "Product").setModel(tabModDoc);
            EBISystem.gui().table("companyProductTable", "Product").setModel(productModel);
        }

        clearPicture();

        EBISystem.gui().button("productShowImage", "Product").setEnabled(false);
        EBISystem.gui().button("deleteProperties", "Product").setEnabled(false);
        EBISystem.gui().button("editProperties", "Product").setEnabled(false);
        EBISystem.gui().button("deleteRelation", "Product").setEnabled(false);
        EBISystem.gui().vpanel("Product").setID(-1);

        EBISystem.gui().combo("ProductCategoryText", "Product").setModel(new DefaultComboBoxModel(category));
        EBISystem.gui().combo("ProductTypeText", "Product").setModel(new DefaultComboBoxModel(type));
        EBISystem.gui().combo("productTaxTypeTex", "Product").setModel(new DefaultComboBoxModel(taxType));

        EBISystem.gui().getPanel("picturePanel", "Product").setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        EBISystem.gui().vpanel("Product").setCreatedFrom(EBISystem.ebiUser);
        EBISystem.gui().vpanel("Product").setCreatedDate(EBISystem.getInstance().getDateToString(new Date()));

        EBISystem.gui().vpanel("Product").setChangedFrom("");
        EBISystem.gui().vpanel("Product").setChangedDate("");

        EBISystem.gui().label("productPictureLabel", "Product").setHorizontalAlignment(SwingConstants.CENTER);
        EBISystem.gui().label("productPictureLabel", "Product").setHorizontalTextPosition(SwingConstants.CENTER);

        EBISystem.gui().textField("ProductNrTex", "Product").setText("");
        EBISystem.gui().textField("ProductNameText", "Product").setText("");
        EBISystem.gui().textArea("productDescription", "Product").setText("");

        EBISystem.gui().FormattedField("productGrossText", "Product").setText("");
        EBISystem.gui().FormattedField("productNetamoutText", "Product").setText("");
        EBISystem.gui().FormattedField("salePriceText", "Product").setText("");

        EBISystem.gui().FormattedField("productGrossText", "Product").setValue(null);
        EBISystem.gui().FormattedField("productNetamoutText", "Product").setValue(null);
        EBISystem.gui().FormattedField("salePriceText", "Product").setValue(null);
    }

    public void clearPicture(){
        final JLabel lab = new JLabel();
        lab.setHorizontalAlignment(SwingConstants.CENTER);
        lab.setHorizontalTextPosition(SwingConstants.CENTER);
        lab.setText(EBISystem.i18n("EBI_LANG_PICTURE"));
        EBISystem.gui().getPanel("picturePanel", "Product").setLayout(new BorderLayout());
        EBISystem.gui().getPanel("picturePanel", "Product").removeAll();
        EBISystem.gui().getPanel("picturePanel", "Product").add(lab, BorderLayout.CENTER);
        EBISystem.gui().getPanel("picturePanel", "Product").updateUI();
    }
    
    
    /**
     * *************************************************************************
     * *
     *************************************************************************
     */
    public void newDocs() {
        dataControlProduct.dataNewDoc();
        EBISystem.showInActionStatus("Product");
        dataControlProduct.dataShowDoc();
    }

    public void showDocs() {
        if (selectedDocRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(tabModDoc.data[selectedDocRow][0].toString())) {
            return;
        }
        dataControlProduct.dataViewDoc(Integer.parseInt(tabModDoc.data[selectedDocRow][3].toString()));
    }

    public void deleteDocs() {
        if (selectedDocRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(tabModDoc.data[selectedDocRow][0].toString())) {
            return;
        }
        if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_MESSAGE_DELETE_RECORD")).Show(EBIMessage.WARNING_MESSAGE_YESNO) == true) {
            EBISystem.showInActionStatus("Product");
            dataControlProduct.dataDeleteDoc(Integer.parseInt(tabModDoc.data[selectedDocRow][3].toString()));
            dataControlProduct.dataShowDoc();
        }
    }

    public void showProduct() {
        dataControlProduct.dataShow(-1);
    }

    public void newProduct() {
        EBISystem.showInActionStatus("Product");
        dataControlProduct.dataNew();
        dataControlProduct.dataShow(-1);
        dataControlProduct.dataShowDependency();
        dataControlProduct.dataShowDimension();
        dataControlProduct.dataShowDoc();
        dataControlProduct.isEdit = false;
    }

    public boolean saveProduct() {
        if (!validateInput()) {
            return false;
        }
        EBISystem.showInActionStatus("Product");
        int row = EBISystem.gui().table("companyProductTable", "Product").getSelectedRow();
        Integer id = dataControlProduct.dataStore();
        dataControlProduct.dataShow(id);
        dataControlProduct.dataShowDoc();
        dataControlProduct.dataShowDependency();
        dataControlProduct.dataShowDimension();
        EBISystem.gui().table("companyProductTable", "Product").changeSelection(row, 0, false, false);
        return true;
    }

    public void editProduct() {
        if (selectedProductRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productModel.data[selectedProductRow][0].toString())) {
            return;
        }
        EBISystem.showInActionStatus("Product");
        dataControlProduct.dataNew();
        dataControlProduct.dataEdit(Integer.parseInt(productModel.data[selectedProductRow][5].toString()));
        dataControlProduct.dataShowDependency();
        dataControlProduct.dataShowDimension();
        dataControlProduct.dataShowDoc();
        dataControlProduct.isEdit = true;
    }

    public void copyProduct() {
        if (selectedProductRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productModel.data[selectedProductRow][0].toString())) {
            return;
        }
        EBISystem.showInActionStatus("Product");
        Integer id = dataControlProduct.dataCopy(Integer.parseInt(productModel.data[selectedProductRow][5].toString()));
        dataControlProduct.dataEdit(id);
        dataControlProduct.dataShow(id);
        dataControlProduct.dataShowDependency();
        dataControlProduct.dataShowDimension();
        dataControlProduct.dataShowDoc();
        dataControlProduct.isEdit = true;
    }

    public void deleteProduct() {
        if (selectedProductRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productModel.data[selectedProductRow][0].toString())) {
            return;
        }
        if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_MESSAGE_DELETE_RECORD")).Show(EBIMessage.WARNING_MESSAGE_YESNO) == true) {
            EBISystem.showInActionStatus("Product");
            dataControlProduct.dataDelete(Integer.parseInt(productModel.data[selectedProductRow][5].toString()));
            dataControlProduct.dataNew();
            dataControlProduct.dataShow(-1);
            dataControlProduct.dataShowDependency();
            dataControlProduct.dataShowDimension();
            dataControlProduct.dataShowDoc();
            dataControlProduct.isEdit = false;
        }
    }

    public void historyProduct() {
        new EBICRMHistoryView(EBISystem.getModule().hcreator.retrieveDBHistory(Integer.parseInt(productModel.data[selectedProductRow][5].toString()), "Product")).setVisible();
    }

    private boolean validateInput() {
        boolean ret = true;
        if ("".equals(EBISystem.gui().textField("ProductNrTex", "Product").getText())) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_INSERT_NUMBER")).Show(EBIMessage.ERROR_MESSAGE);
            ret = false;
        } else if ("".equals(EBISystem.gui().textField("ProductNameText", "Product").getText())) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_INSERT_PRODUCT_NAME")).Show(EBIMessage.ERROR_MESSAGE);
            ret = false;
        } else if (EBISystem.gui().combo("ProductCategoryText", "Product").getSelectedItem() != null) {
            if (EBISystem.i18n("EBI_LANG_PLEASE_SELECT").equals(
                    EBISystem.gui().combo("ProductCategoryText", "Product").getSelectedItem().toString())) {
                EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_SELECT_CATEGORY")).Show(EBIMessage.ERROR_MESSAGE);
                ret = false;
            }
        } else if (!dataControlProduct.isEdit) {
            if (dataControlProduct.existProduct(EBISystem.gui().textField("ProductNrTex", "Product").getText())) {
                EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_SAME_RECORD_EXSIST")).Show(EBIMessage.ERROR_MESSAGE);
                ret = false;
            }
        }
        return ret;
    }

    public void calcClear() {
        if (!"".equals(EBISystem.gui().FormattedField("productNetamoutText", "Product").getText())) {
            dataControlProduct.calculatePreTaxPrice();
        }
    }

    public void calcGross() {
        if (!"".equals(EBISystem.gui().FormattedField("productGrossText", "Product").getText())) {
            dataControlProduct.calculateClearPrice();
        }
    }

    /**
     * *************************************************************************
     * Product Dependency manipulation
     * ************************************************************************
     */
    public void newDependency() {
        dataControlProduct.dataNewDependency();
        EBISystem.showInActionStatus("Product");
        dataControlProduct.dataShowDependency();
    }

    public void deleteDependency() {
        if (selectedDependencyRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productDependencyModel.data[selectedDependencyRow][0].toString())) {
            return;
        }
        if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_MESSAGE_DELETE_RECORD")).Show(EBIMessage.WARNING_MESSAGE_YESNO) == true) {
            dataControlProduct.dataDeleteDependency(Integer.parseInt(productDependencyModel.data[selectedDependencyRow][2].toString()));
            EBISystem.showInActionStatus("Product");
            dataControlProduct.dataShowDependency();
        }
    }

    public void showDependency() {
        dataControlProduct.dataShowDependency();
    }

    /**
     * *************************************************************************
     * Product Dimension manipulation
     * ************************************************************************
     */
    public void newDimension() {
        dataControlProduct.dataNewDimension();
        EBISystem.showInActionStatus("Product");
    }

    public void editDimension() {
        if (selectedDimensionRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productModelDimension.data[selectedDimensionRow][0].toString())) {
            return;
        }
        dataControlProduct.dataEditDimension(Integer.parseInt(productModelDimension.data[selectedDimensionRow][2].toString()));
        EBISystem.showInActionStatus("Product");
        dataControlProduct.dataShowDimension();
    }

    public void deleteDimension() throws Exception {
        if (selectedDimensionRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productModelDimension.data[selectedDimensionRow][0].toString())) {
            return;
        }
        if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_MESSAGE_DELETE_RECORD")).Show(EBIMessage.WARNING_MESSAGE_YESNO) == true) {
            dataControlProduct.dataDeleteDimension(Integer.parseInt(productModelDimension.data[selectedDimensionRow][2].toString()));
            EBISystem.showInActionStatus("Product");
            dataControlProduct.dataShowDimension();
        }
    }

    public void showDimension() {
        dataControlProduct.dataShowDimension();
    }

    public void showProductReport() {
        if (selectedProductRow < 0 || EBISystem.i18n("EBI_LANG_PLEASE_SELECT").
                equals(productModel.data[selectedProductRow][0].toString())) {
            return;
        }
        boolean pass;
        if (EBISystem.getInstance().getIEBISystemUserRights().isCanPrint()
                || EBISystem.getInstance().getIEBISystemUserRights().isAdministrator()) {
            pass = true;
        } else {
            pass = EBISystem.getInstance().getIEBISecurityInstance().secureModule();
        }
        if (pass) {
            EBISystem.showInActionStatus("Product");
            dataControlProduct.dataShowReport(Integer.parseInt(productModel.data[selectedProductRow][5].toString()));
        }
    }
}
