package ebiCRM.data.control;

import ebiCRM.utils.EBICRMHistoryDataUtil;
import ebiNeutrinoSDK.EBISystem;
import ebiNeutrinoSDK.gui.dialogs.EBIExceptionDialog;
import ebiNeutrinoSDK.gui.dialogs.EBIMessage;
import ebiNeutrinoSDK.model.hibernate.*;

import javax.swing.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

public class EBIDataControlService {

    public Companyservice compService = null;
    public boolean isEdit = false;

    public EBIDataControlService() {
        compService = new Companyservice();
    }

    public boolean dataStore() {

        try {
            EBISystem.hibernate().transaction("EBICRM_SESSION").begin();

            if (isEdit == false) {
                compService.setCreateddate(new Date());
                compService.setCreatedfrom(EBISystem.gui().vpanel("Service").getCreatedFrom());
                compService.setCompany(EBISystem.getInstance().company);
            } else {
                createHistory(EBISystem.getInstance().company);
                compService.setChangeddate(new Date());
                compService.setChangedfrom(EBISystem.ebiUser);
            }

            compService.setDescription(EBISystem.gui().textArea("serviceDescriptionText", "Service").getText());
            compService.setServicenr(EBISystem.gui().textField("serviceNrText", "Service").getText());
            compService.setName(EBISystem.gui().textField("serviceNameText", "Service").getText());

            if (EBISystem.gui().combo("serviceStatusText", "Service").getSelectedItem() != null) {
                compService.setStatus(EBISystem.gui().combo("serviceStatusText", "Service").getSelectedItem().toString());
            }

            if (EBISystem.gui().combo("serviceCategoryText", "Service").getSelectedItem() != null) {
                compService.setCategory(EBISystem.gui().combo("serviceCategoryText", "Service").getSelectedItem().toString());
            }

            if (EBISystem.gui().combo("serviceTypeText", "Service").getSelectedItem() != null) {
                compService.setType(EBISystem.gui().combo("serviceTypeText", "Service").getSelectedItem().toString());
            }

            EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(compService);

            // SAVE DOC
            if (!compService.getCompanyservicedocses().isEmpty()) {
                final Iterator iter = compService.getCompanyservicedocses().iterator();
                while (iter.hasNext()) {
                    final Companyservicedocs docs = (Companyservicedocs) iter.next();
                    if (docs.getServicedocid() != null && docs.getServicedocid() < 0) {
                        docs.setServicedocid(null);
                    }
                    docs.setCompanyservice(compService);
                    EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(docs);
                }
            }
            // SAVE POSITION
            if (!compService.getCompanyservicepositionses().isEmpty()) {
                final Iterator iter = compService.getCompanyservicepositionses().iterator();

                while (iter.hasNext()) {
                    final Companyservicepositions pos = (Companyservicepositions) iter.next();
                    if (pos.getPositionid() != null && pos.getPositionid() < 0) {
                        pos.setPositionid(null);
                    }
                    pos.setCompanyservice(compService);
                    EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(pos);
                }
            }
            // SAVE PROBLEM
            if (!compService.getCompanyservicepsols().isEmpty()) {
                final Iterator it = compService.getCompanyservicepsols().iterator();
                while (it.hasNext()) {
                    final Companyservicepsol psol = (Companyservicepsol) it.next();
                    if (psol.getProsolid() != null && psol.getProsolid() < 0) {
                        psol.setProsolid(null);
                    }
                    psol.setCompanyservice(compService);
                    EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(psol);
                }
            }

            EBISystem.getInstance().getDataStore("Service", "ebiSave");
            EBISystem.getInstance().company.getCompanyservices().add(compService);
            EBISystem.hibernate().transaction("EBICRM_SESSION").commit();

            if (!isEdit) {
                EBISystem.gui().vpanel("Service").setID(compService.getServiceid());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void dataCopy(final int id) {

        try {
            if (EBISystem.getInstance().company.getCompanyservices().size() > 0) {
                Companyservice service = null;
                for (Companyservice servObj : EBISystem.getInstance().company.getCompanyservices()) {
                    if (servObj.getServiceid() == id) {
                        service = servObj;
                        break;
                    }
                }

                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                final Companyservice serv = new Companyservice();
                serv.setCreateddate(new Date());
                serv.setCreatedfrom(EBISystem.ebiUser);
                serv.setCompany(service.getCompany());
                serv.setDescription(service.getDescription());
                serv.setServicenr(service.getServicenr());
                serv.setName(service.getName() + " - (Copy)");
                serv.setStatus(service.getStatus());
                serv.setCategory(service.getCategory());
                serv.setType(service.getType());
                EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(serv);
                // SAVE DOC
                if (!service.getCompanyservicedocses().isEmpty()) {
                    final Iterator itd = service.getCompanyservicedocses().iterator();
                    while (itd.hasNext()) {
                        final Companyservicedocs docs = (Companyservicedocs) itd.next();

                        final Companyservicedocs dc = new Companyservicedocs();
                        dc.setCompanyservice(serv);
                        dc.setCreateddate(new Date());
                        dc.setCreatedfrom(EBISystem.ebiUser);
                        dc.setFiles(docs.getFiles());
                        dc.setName(docs.getName());
                        service.getCompanyservicedocses().add(dc);
                        EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(dc);
                    }
                }
                // SAVE POSITION
                if (!service.getCompanyservicepositionses().isEmpty()) {
                    final Iterator itp = service.getCompanyservicepositionses().iterator();

                    while (itp.hasNext()) {
                        final Companyservicepositions pos = (Companyservicepositions) itp.next();

                        final Companyservicepositions p = new Companyservicepositions();
                        p.setCompanyservice(serv);
                        p.setCategory(pos.getCategory());
                        p.setCreateddate(new Date());
                        p.setCreatedfrom(EBISystem.ebiUser);
                        p.setDeduction(pos.getDeduction());
                        p.setDescription(pos.getDescription());
                        p.setNetamount(pos.getNetamount());
                        p.setPretax(pos.getPretax());
                        p.setProductid(pos.getProductid());
                        p.setProductname(pos.getProductname());
                        p.setProductnr(pos.getProductnr());
                        p.setQuantity(pos.getQuantity());
                        p.setTaxtype(pos.getTaxtype());
                        p.setType(pos.getType());
                        service.getCompanyservicepositionses().add(p);
                        EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(p);
                    }
                }
                // SAVE PROBLEM
                if (!service.getCompanyservicepsols().isEmpty()) {
                    final Iterator it = service.getCompanyservicepsols().iterator();
                    while (it.hasNext()) {
                        final Companyservicepsol psol = (Companyservicepsol) it.next();

                        final Companyservicepsol sv = new Companyservicepsol();
                        sv.setCompanyservice(serv);
                        sv.setCategory(psol.getCategory());
                        sv.setClassification(psol.getClassification());
                        sv.setCreateddate(new Date());
                        sv.setCreatedfrom(EBISystem.ebiUser);
                        sv.setDescription(psol.getDescription());
                        sv.setName(psol.getName());
                        sv.setSolutionnr(psol.getSolutionnr());
                        sv.setStatus(psol.getStatus());
                        sv.setType(psol.getType());
                        service.getCompanyservicepsols().add(sv);
                        EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(sv);

                    }
                }

                EBISystem.getInstance().company.getCompanyservices().add(serv);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                EBISystem.gui().table("companyServiceTable", "Service").changeSelection(EBISystem.gui().table("companyServiceTable", "Service")
                        .convertRowIndexToView(EBISystem.getCRMModule().dynMethod.getIdIndexFormArrayInATable(
                                EBISystem.getCRMModule().getServicePane().tabModService.data, 6, serv.getServiceid())),
                        0, false, false);

            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void dataEdit(final int id) {

        if (EBISystem.getInstance().company.getCompanyservices().size() > 0) {

            for (Companyservice servObj : EBISystem.getInstance().company.getCompanyservices()) {
                if (servObj.getServiceid() == id) {
                    compService = servObj;
                    break;
                }
            }

            EBISystem.gui().vpanel("Service").setID(compService.getServiceid());
            EBISystem.gui().vpanel("Service").setCreatedDate(EBISystem.getInstance().getDateToString(compService.getCreateddate() == null ? new Date() : compService.getCreateddate()));
            EBISystem.gui().vpanel("Service").setCreatedFrom(compService.getCreatedfrom() == null ? EBISystem.ebiUser : compService.getCreatedfrom());

            if (compService.getChangeddate() != null) {
                EBISystem.gui().vpanel("Service").setChangedDate(EBISystem.getInstance().getDateToString(compService.getChangeddate()));
                EBISystem.gui().vpanel("Service").setChangedFrom(compService.getChangedfrom());
            } else {
                EBISystem.gui().vpanel("Service").setChangedDate("");
                EBISystem.gui().vpanel("Service").setChangedFrom("");
            }

            EBISystem.gui().textField("serviceNameText", "Service").setText(compService.getName());
            EBISystem.gui().textField("serviceNrText", "Service").setText(compService.getServicenr() == null ? "" : compService.getServicenr());

            if (compService.getStatus() != null) {
                EBISystem.gui().combo("serviceStatusText", "Service").setSelectedItem(compService.getStatus());
            }

            if (compService.getCategory() != null) {
                EBISystem.gui().combo("serviceCategoryText", "Service").setSelectedItem(compService.getCategory());
            }

            if (compService.getType() != null) {
                EBISystem.gui().combo("serviceTypeText", "Service").setSelectedItem(compService.getType());
            }
            EBISystem.gui().textArea("serviceDescriptionText", "Service").setText(compService.getDescription());

            EBISystem.getInstance().getDataStore("Service", "ebiEdit");

            EBISystem.gui().table("companyServiceTable", "Service").changeSelection(
                    EBISystem.gui().table("companyServiceTable", "Service")
                            .convertRowIndexToView(EBISystem.getCRMModule().dynMethod.getIdIndexFormArrayInATable(
                                    EBISystem.getCRMModule().getServicePane().tabModService.data, 6, id)), 0, false, false);
        } else {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_RECORD_NOT_FOUND")).Show(EBIMessage.INFO_MESSAGE);
        }
    }

    public void dataDelete(final int id) {
        if (EBISystem.getInstance().company.getCompanyservices().size() > 0) {
            for (Companyservice servObj : EBISystem.getInstance().company.getCompanyservices()) {
                if (servObj.getServiceid() == id) {
                    compService = servObj;
                    break;
                }
            }

            EBISystem.getInstance().getDataStore("Service", "ebiDelete");
            EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
            EBISystem.hibernate().session("EBICRM_SESSION").delete(compService);
            EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
            EBISystem.getInstance().company.getCompanyservices().remove(compService);
        }
    }

    public void dataShow() {
        final int srow = EBISystem.gui().table("companyServiceTable", "Service").getSelectedRow();
        final int size = EBISystem.getInstance().company.getCompanyservices().size();
        if (size > 0) {
            EBISystem.getCRMModule().getServicePane().tabModService.data = new Object[size][8];
            final Iterator<Companyservice> itr = EBISystem.getInstance().company.getCompanyservices().iterator();
            int i = 0;
            while (itr.hasNext()) {
                final Companyservice service = itr.next();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][0] = service.getServicenr() == null ? "" : service.getServicenr();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][1] = service.getName() == null ? "" : service.getName();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][2] = service.getStatus() == null ? "" : service.getStatus();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][3] = service.getType() == null ? "" : service.getType();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][4] = service.getCategory() == null ? "" : service.getCategory();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][5] = service.getDescription() == null ? "" : service.getDescription();
                EBISystem.getCRMModule().getServicePane().tabModService.data[i][6] = service.getServiceid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getServicePane().tabModService.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", ""}};
        }

        EBISystem.getCRMModule().getServicePane().tabModService.fireTableDataChanged();
        EBISystem.gui().table("companyServiceTable", "Service").changeSelection(srow, 0, false, false);
    }

    public void dataShowReport(final int id) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("ID", id);
        EBISystem.getInstance().getIEBIReportSystemInstance().useReportSystem(map,
                EBISystem.getInstance().convertReportCategoryToIndex(EBISystem.i18n("EBI_LANG_C_SERVICE")), getserviceNamefromId(id));
    }

    public void dataNew() {
        compService = new Companyservice();
        EBISystem.getCRMModule().getServicePane().initialize(false);
        EBISystem.getInstance().getDataStore("Service", "ebiNew");
        EBISystem.gui().vpanel("Service").setID(-1);
    }

    public void createInvoiceFromService(final int id) {
        final Iterator iter = EBISystem.getInstance().company.getCompanyservices().iterator();

        if (!EBISystem.getCRMModule().crmToolBar.isInvoiceSelected()) {
            EBISystem.getCRMModule().crmToolBar.enableToolButtonInvoice();
            EBISystem.getCRMModule().ebiContainer.showClosableInvoiceContainer();
            EBISystem.getInstance().getIEBIContainerInstance().setSelectedTab(EBISystem.getInstance().getIEBIContainerInstance().getIndexByTitle(EBISystem.i18n("EBI_LANG_C_TAB_INVOICE")));
        } else {
            EBISystem.getCRMModule().getInvoicePane().dataControlInvoice.dataNew();
            EBISystem.getInstance().getIEBIContainerInstance().setSelectedTab(EBISystem.getInstance().getIEBIContainerInstance().getIndexByTitle(EBISystem.i18n("EBI_LANG_C_TAB_INVOICE")));
        }

        while (iter.hasNext()) {

            final Companyservice serv = (Companyservice) iter.next();

            if (serv.getServiceid() == id) {

                // Invoice field
                EBISystem.gui().textField("invoiceNameText", "Invoice").setText(serv.getName());

                EBISystem.gui().textField("orderText", "Invoice")
                        .setText(EBISystem.i18n("EBI_LANG_C_SERVICE") + ": " + serv.getServiceid());

                if (!serv.getCompanyservicepositionses().isEmpty()) {
                    final Iterator ip = serv.getCompanyservicepositionses().iterator();
                    while (ip.hasNext()) {
                        final Companyservicepositions pos = (Companyservicepositions) ip.next();
                        final Crminvoiceposition inpos = new Crminvoiceposition();
                        inpos.setCrminvoice(EBISystem.getCRMModule().getInvoicePane().dataControlInvoice.getInvoice());
                        inpos.setProductid(pos.getProductid());
                        inpos.setCategory(pos.getCategory());
                        inpos.setCreateddate(new Date());
                        inpos.setCreatedfrom(EBISystem.ebiUser);
                        inpos.setDeduction(pos.getDeduction());
                        inpos.setDescription(pos.getDescription());
                        inpos.setNetamount(pos.getNetamount());
                        inpos.setPretax(pos.getPretax());
                        inpos.setProductname(pos.getProductname());
                        inpos.setProductnr(pos.getProductnr());
                        inpos.setQuantity(pos.getQuantity());
                        inpos.setTaxtype(pos.getTaxtype());
                        inpos.setType(pos.getType());
                        EBISystem.getCRMModule().getInvoicePane().dataControlInvoice.getInvoice().getCrminvoicepositions().add(inpos);
                        EBISystem.getCRMModule().getInvoicePane().dataControlInvoice.dataShowProduct();
                    }
                }
            }
        }
    }

    private void createHistory(final Company com) {

        final List<String> list = new ArrayList<String>();

        list.add(EBISystem.i18n("EBI_LANG_ADDED") + ": " + EBISystem.getInstance().getDateToString(compService.getCreateddate()));
        list.add(EBISystem.i18n("EBI_LANG_ADDED_FROM") + ": " + compService.getCreatedfrom());

        if (compService.getChangeddate() != null) {
            list.add(EBISystem.i18n("EBI_LANG_CHANGED") + ": " + EBISystem.getInstance().getDateToString(compService.getChangeddate()));
            list.add(EBISystem.i18n("EBI_LANG_CHANGED_FROM") + ": " + compService.getChangedfrom());
        }

        list.add(EBISystem.i18n("EBI_LANG_SERVICE_NUMBER") + ": "
                + (compService.getServicenr()
                        .equals(EBISystem.gui().textField("serviceNrText", "Service").getText()) == true
                ? compService.getServicenr()
                : compService.getServicenr() + "$"));
        list.add(EBISystem.i18n("EBI_LANG_NAME") + ": "
                + (compService.getName()
                        .equals(EBISystem.gui().textField("serviceNameText", "Service").getText()) == true
                ? compService.getName()
                : compService.getName() + "$"));

        list.add(EBISystem.i18n("EBI_LANG_C_STATUS") + ": "
                + (compService.getStatus().equals(EBISystem.gui().combo("serviceStatusText", "Service")
                        .getSelectedItem().toString()) == true ? compService.getStatus() : compService.getStatus() + "$"));

        list.add(EBISystem.i18n("EBI_LANG_CATEGORY") + ": "
                + (compService.getCategory()
                        .equals(EBISystem.gui().combo("serviceCategoryText", "Service")
                                .getSelectedItem().toString()) == true ? compService.getCategory() : compService.getCategory() + "$"));
        list.add(
                EBISystem.i18n("EBI_LANG_TYPE") + ": "
                + (compService.getType()
                        .equals(EBISystem.gui().combo("serviceTypeText", "Service")
                                .getSelectedItem().toString()) == true ? compService.getType() : compService.getType() + "$"));

        list.add(EBISystem.i18n("EBI_LANG_DESCRIPTION") + ": "
                + (compService.getDescription().equals(
                        EBISystem.gui().textArea("serviceDescriptionText", "Service").getText()) == true
                ? compService.getDescription() : compService.getDescription() + "$"));
        list.add("*EOR*"); // END OF RECORD

        if (!compService.getCompanyservicedocses().isEmpty()) {

            final Iterator iter = compService.getCompanyservicedocses().iterator();
            while (iter.hasNext()) {
                final Companyservicedocs obj = (Companyservicedocs) iter.next();
                list.add(obj.getName() == null ? EBISystem.i18n("EBI_LANG_FILENAME") + ": "
                        : EBISystem.i18n("EBI_LANG_FILENAME") + ": " + obj.getName());
                list.add(EBISystem.getInstance().getDateToString(obj.getCreateddate()) == null
                        ? EBISystem.i18n("EBI_LANG_C_ADDED_DATE") + ": "
                        : EBISystem.i18n("EBI_LANG_C_ADDED_DATE") + ": "
                        + EBISystem.getInstance().getDateToString(obj.getCreateddate()));
                list.add(obj.getCreatedfrom() == null ? EBISystem.i18n("EBI_LANG_ADDED_FROM") + ": "
                        : EBISystem.i18n("EBI_LANG_ADDED_FROM") + ": " + obj.getCreatedfrom());
                list.add("*EOR*");
            }
        }

        if (!compService.getCompanyservicepositionses().isEmpty()) {

            final Iterator iter = compService.getCompanyservicepositionses().iterator();

            while (iter.hasNext()) {
                final Companyservicepositions obj = (Companyservicepositions) iter.next();
                list.add(EBISystem.i18n("EBI_LANG_QUANTITY") + ": " + String.valueOf(obj.getQuantity()));
                list.add(EBISystem.i18n("EBI_LANG_PRODUCT_NUMBER") + ": " + obj.getProductnr());
                list.add(obj.getProductname() == null ? EBISystem.i18n("EBI_LANG_NAME") + ":"
                        : EBISystem.i18n("EBI_LANG_NAME") + ": " + obj.getProductname());
                list.add(obj.getCategory() == null ? EBISystem.i18n("EBI_LANG_CATEGORY") + ":"
                        : EBISystem.i18n("EBI_LANG_CATEGORY") + ": " + obj.getCategory());
                list.add(obj.getTaxtype() == null ? EBISystem.i18n("EBI_LANG_TAX") + ":"
                        : EBISystem.i18n("EBI_LANG_TAX") + ": " + obj.getTaxtype());
                list.add(String.valueOf(obj.getPretax()) == null ? EBISystem.i18n("EBI_LANG_PRICE") + ":"
                        : EBISystem.i18n("EBI_LANG_PRICE") + ": " + String.valueOf(obj.getPretax()));
                list.add(String.valueOf(obj.getDeduction()) == null ? EBISystem.i18n("EBI_LANG_DEDUCTION") + ":"
                        : EBISystem.i18n("EBI_LANG_DEDUCTION") + ": " + String.valueOf(obj.getDeduction()));
                list.add(obj.getDescription() == null ? EBISystem.i18n("EBI_LANG_DESCRIPTION") + ":"
                        : EBISystem.i18n("EBI_LANG_DESCRIPTION") + ": " + obj.getDescription());
                list.add("*EOR*");

            }
        }

        if (!compService.getCompanyservicepsols().isEmpty()) {

            final Iterator iter = compService.getCompanyservicepsols().iterator();
            while (iter.hasNext()) {
                final Companyservicepsol obj = (Companyservicepsol) iter.next();
                list.add(obj.getName() == null ? EBISystem.i18n("EBI_LANG_C_CNAME") + ":"
                        : EBISystem.i18n("EBI_LANG_C_CNAME") + ": " + obj.getName());
                list.add(obj.getClassification() == null ? EBISystem.i18n("EBI_LANG_CLASSIFICATION") + ":"
                        : EBISystem.i18n("EBI_LANG_CLASSIFICATION") + ": " + obj.getClassification());
                list.add(obj.getStatus() == null ? EBISystem.i18n("EBI_LANG_STATUS") + ":"
                        : EBISystem.i18n("EBI_LANG_STATUS") + ": " + obj.getStatus());
                list.add(obj.getType() == null ? EBISystem.i18n("EBI_LANG_TYPE") + ":"
                        : EBISystem.i18n("EBI_LANG_TYPE") + ": " + obj.getType());
                list.add(obj.getCategory() == null ? EBISystem.i18n("EBI_LANG_CATEGORY") + ":"
                        : EBISystem.i18n("EBI_LANG_CATEGORY") + ": " + obj.getCategory());
                list.add(obj.getDescription() == null ? EBISystem.i18n("EBI_LANG_DESCRIPTION") + ":"
                        : EBISystem.i18n("EBI_LANG_DESCRIPTION") + ": " + obj.getDescription());
                list.add("*EOR*");
            }
        }

        try {
            EBISystem.getCRMModule().hcreator
                    .setDataToCreate(new EBICRMHistoryDataUtil(com.getCompanyid(), "Service", list));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void dataNewDoc() {
        final File selFile = EBISystem.getInstance().getOpenDialog(JFileChooser.FILES_ONLY);
        if (selFile != null) {
            final byte[] file = EBISystem.getInstance().readFileToByte(selFile);
            if (file != null) {
                final Companyservicedocs docs = new Companyservicedocs();
                docs.setServicedocid((compService.getCompanyservicedocses().size() + 1) * -1);
                docs.setCompanyservice(compService);
                docs.setName(selFile.getName());
                docs.setCreateddate(new java.sql.Date(new java.util.Date().getTime()));
                docs.setCreatedfrom(EBISystem.ebiUser);
                docs.setFiles(file);
                compService.getCompanyservicedocses().add(docs);
            } else {
                EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_FILE_CANNOT_READING")).Show(EBIMessage.ERROR_MESSAGE);
                return;
            }
        }
    }

    public void dataViewDoc(final int id) {
        String FileName;
        String FileType;
        OutputStream fos;
        try {

            final Iterator iter = this.compService.getCompanyservicedocses().iterator();
            while (iter.hasNext()) {

                final Companyservicedocs doc = (Companyservicedocs) iter.next();

                if (id == doc.getServicedocid()) {
                    // Get the BLOB inputstream

                    final String file = doc.getName().replaceAll(" ", "_");
                    final byte buffer[] = doc.getFiles();
                    FileName = "tmp/" + file;
                    FileType = file.substring(file.lastIndexOf("."));
                    fos = new FileOutputStream(FileName);
                    fos.write(buffer, 0, buffer.length);
                    fos.close();
                    EBISystem.getInstance().resolverType(FileName, FileType);
                    break;
                }
            }
        } catch (final FileNotFoundException exx) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_FILE_NOT_FOUND")).Show(EBIMessage.INFO_MESSAGE);
        } catch (final IOException exx1) {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_ERROR_LOADING_FILE")).Show(EBIMessage.INFO_MESSAGE);
        }

    }

    public void dataShowDoc() {
        if (compService.getCompanyservicedocses().size() > 0) {
            EBISystem.getCRMModule().getServicePane().tabModDoc.data = new Object[compService.getCompanyservicedocses().size()][4];
            final Iterator itr = compService.getCompanyservicedocses().iterator();
            int i = 0;
            while (itr.hasNext()) {
                final Companyservicedocs obj = (Companyservicedocs) itr.next();
                EBISystem.getCRMModule().getServicePane().tabModDoc.data[i][0] = obj.getName() == null ? "" : obj.getName();
                EBISystem.getCRMModule().getServicePane().tabModDoc.data[i][1] = EBISystem.getInstance().getDateToString(obj.getCreateddate()) == null ? "" : EBISystem.getInstance().getDateToString(obj.getCreateddate());
                EBISystem.getCRMModule().getServicePane().tabModDoc.data[i][2] = obj.getCreatedfrom() == null ? "" : obj.getCreatedfrom();
                EBISystem.getCRMModule().getServicePane().tabModDoc.data[i][3] = obj.getServicedocid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getServicePane().tabModDoc.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", ""}};
        }
        EBISystem.getCRMModule().getServicePane().tabModDoc.fireTableDataChanged();
    }

    public void dataShowProduct() {
        if (this.compService.getCompanyservicepositionses().size() > 0) {
            EBISystem.getCRMModule().getServicePane().tabModProduct.data = new Object[compService.getCompanyservicepositionses().size()][9];
            final Iterator itr = compService.getCompanyservicepositionses().iterator();
            int i = 0;
            final NumberFormat currency = NumberFormat.getCurrencyInstance();

            while (itr.hasNext()) {
                final Companyservicepositions obj = (Companyservicepositions) itr.next();
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][0] = String.valueOf(obj.getQuantity());
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][1] = obj.getProductnr();
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][2] = obj.getProductname() == null ? "" : obj.getProductname();
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][3] = obj.getCategory() == null ? "" : obj.getCategory();
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][4] = obj.getTaxtype() == null ? "" : obj.getTaxtype();
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][5] = currency
                        .format(EBISystem.getCRMModule().dynMethod.calculatePreTaxPrice(obj.getNetamount(),
                                String.valueOf(obj.getQuantity()), String.valueOf(obj.getDeduction()))) == null
                        ? "" : currency.format(EBISystem.getCRMModule().dynMethod.calculatePreTaxPrice(
                                obj.getNetamount(), String.valueOf(obj.getQuantity()), String.valueOf(obj.getDeduction())));
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][6] = obj.getDeduction().equals("") ? "" : obj.getDeduction() + "%";
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][7] = obj.getDescription() == null ? "" : obj.getDescription();
                EBISystem.getCRMModule().getServicePane().tabModProduct.data[i][8] = obj.getPositionid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getServicePane().tabModProduct.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", "", ""}};
        }
        EBISystem.getCRMModule().getServicePane().tabModProduct.fireTableDataChanged();
    }

    public void dataShowProblemSolution() {
        if (compService.getCompanyservicepsols().size() > 0) {
            EBISystem.getCRMModule().getServicePane().tabModProsol.data = new Object[compService.getCompanyservicepsols().size()][8];
            final Iterator itr = compService.getCompanyservicepsols().iterator();
            int i = 0;
            while (itr.hasNext()) {
                final Companyservicepsol obj = (Companyservicepsol) itr.next();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][0] = obj.getSolutionnr() == null ? "" : obj.getSolutionnr();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][1] = obj.getName() == null ? "" : obj.getName();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][2] = obj.getClassification() == null ? "" : obj.getClassification();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][3] = obj.getCategory() == null ? "" : obj.getCategory();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][4] = obj.getType() == null ? "" : obj.getType();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][5] = obj.getStatus() == null ? "" : obj.getStatus();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][6] = obj.getDescription() == null ? "" : obj.getDescription();
                EBISystem.getCRMModule().getServicePane().tabModProsol.data[i][7] = obj.getProsolid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getServicePane().tabModProsol.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", ""}};
        }
        EBISystem.getCRMModule().getServicePane().tabModProsol.fireTableDataChanged();
    }

    public void dataDeleteDoc(final int id) {
        final Iterator iter = compService.getCompanyservicedocses().iterator();
        while (iter.hasNext()) {
            final Companyservicedocs doc = (Companyservicedocs) iter.next();
            if (doc.getServicedocid() == id) {
                compService.getCompanyservicedocses().remove(doc);
                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                EBISystem.hibernate().session("EBICRM_SESSION").delete(doc);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                break;
            }
        }
    }

    public void dataDeleteProblemSolution(final int id) {
        final Iterator iter = compService.getCompanyservicepsols().iterator();
        while (iter.hasNext()) {
            final Companyservicepsol servicerec = (Companyservicepsol) iter.next();
            if (servicerec.getProsolid() == id) {
                compService.getCompanyservicepsols().remove(servicerec);
                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                EBISystem.hibernate().session("EBICRM_SESSION").delete(servicerec);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                break;
            }
        }
    }

    public void dataDeleteProduct(final int id) {
        final Iterator iter = compService.getCompanyservicepositionses().iterator();
        while (iter.hasNext()) {
            final Companyservicepositions servicepro = (Companyservicepositions) iter.next();
            if (servicepro.getPositionid() == id) {
                compService.getCompanyservicepositionses().remove(servicepro);
                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                EBISystem.hibernate().session("EBICRM_SESSION").delete(servicepro);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                break;
            }
        }
    }

    public Companyservice getcompService() {
        return compService;
    }

    public void setcompService(final Companyservice compService) {
        this.compService = compService;
    }

    public Set<Companyservicedocs> getserviceDocList() {
        return compService.getCompanyservicedocses();
    }

    public Set<Companyservicepositions> getservicePosList() {
        return compService.getCompanyservicepositionses();
    }

    public Set<Companyservicepsol> getserviceProSolList() {
        return compService.getCompanyservicepsols();
    }

    public Set<Companyservice> getServiceList() {
        return EBISystem.getInstance().company.getCompanyservices();
    }

    private String getserviceNamefromId(final int id) {
        String name = "";
        final Iterator iter = EBISystem.getInstance().company.getCompanyservices().iterator();
        while (iter.hasNext()) {
            final Companyservice service = (Companyservice) iter.next();
            if (service.getServiceid() == id) {
                name = service.getName();
                break;
            }
        }
        return name;
    }
}
