package ebiCRM.data.control;

import ebiCRM.gui.dialogs.EBICRMAddContactAddressType;
import ebiCRM.utils.EBICRMHistoryDataUtil;
import ebiNeutrinoSDK.EBISystem;
import ebiNeutrinoSDK.gui.dialogs.EBIExceptionDialog;
import ebiNeutrinoSDK.gui.dialogs.EBIMessage;
import ebiNeutrinoSDK.model.hibernate.*;

import javax.swing.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

public class EBIDataControlOrder {

    public Companyorder compOrder = null;
    private int offerID = 0;
    public boolean isEdit = false;

    public EBIDataControlOrder() {
        compOrder = new Companyorder();
    }

    public boolean dataStore() {

        try {
            EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
            if (isEdit == false) {
                compOrder.setCreateddate(new Date());
                compOrder.setCreatedfrom(EBISystem.gui().vpanel("Order").getCreatedFrom());
                compOrder.setCompany(EBISystem.getInstance().company);
            } else {
                createHistory(EBISystem.getInstance().company);
                compOrder.setChangeddate(new Date());
                compOrder.setChangedfrom(EBISystem.ebiUser);
            }

            if (EBISystem.gui().timePicker("orderCreatedText", "Order").getDate() != null) {
                compOrder.setOfferdate(EBISystem.gui().timePicker("orderCreatedText", "Order").getDate());
            }

            if (EBISystem.gui().timePicker("orderReceiveText", "Order").getDate() != null) {
                compOrder.setValidto(EBISystem.gui().timePicker("orderReceiveText", "Order").getDate());
            }

            compOrder.setDescription(EBISystem.gui().textArea("orderDescription", "Order").getText());
            compOrder.setOrdernr(EBISystem.gui().textField("orderNrText", "Order").getText());
            compOrder.setName(EBISystem.gui().textField("orderNameText", "Order").getText());

            if (!"".equals(EBISystem.gui().textField("orderOfferText", "Order").getText())) {
                compOrder.setOfferid(offerID);
            }

            if (EBISystem.gui().combo("orderStatusText", "Order").getSelectedItem() != null) {
                compOrder.setStatus(EBISystem.gui().combo("orderStatusText", "Order").getSelectedItem().toString());
            }

            EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(compOrder);

            if (!compOrder.getCompanyorderdocses().isEmpty()) {
                final Iterator iter = compOrder.getCompanyorderdocses().iterator();
                while (iter.hasNext()) {
                    final Companyorderdocs docs = (Companyorderdocs) iter.next();
                    if(docs.getOrderdocid() != null && docs.getOrderdocid() < 0){
                        docs.setOrderdocid(null);
                    }
                    docs.setCompanyorder(compOrder);
                    EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(docs);
                }
            }

            //Save position
            if (!compOrder.getCompanyorderpositionses().isEmpty()) {
                final Iterator iter = compOrder.getCompanyorderpositionses().iterator();
                while (iter.hasNext()) {
                    final Companyorderpositions pos = (Companyorderpositions) iter.next();
                    if(pos.getPositionid() != null && pos.getPositionid() < 0){
                        pos.setPositionid(null);
                    }
                    pos.setCompanyorder(compOrder);
                    EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(pos);
                }
            }

            // Save Receiver
            if (!compOrder.getCompanyorderreceivers().isEmpty()) {
                final Iterator iter = compOrder.getCompanyorderreceivers().iterator();
                while (iter.hasNext()) {
                    final Companyorderreceiver rec = (Companyorderreceiver) iter.next();
                    if(rec.getReceiverid() != null && rec.getReceiverid() < 0){
                        rec.setReceiverid(null);
                    }
                    rec.setCompanyorder(compOrder);
                    EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(rec);
                }
            }

            EBISystem.getInstance().getDataStore("Order", "ebiSave");
            EBISystem.getInstance().company.getCompanyorders().add(compOrder);

            EBISystem.hibernate().transaction("EBICRM_SESSION").commit();

            if (!isEdit) {
                EBISystem.gui().vpanel("Order").setID(compOrder.getOrderid());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void dataCopy(final int id) {
        try {
        	if (EBISystem.getInstance().company.getCompanyorders().size() > 0) {
        		Companyorder order = null;
            	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
            		if(ordObj.getOrderid() == id) {
            			order = ordObj;
            			break;
            		}
            	}

                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                final Companyorder ordnew = new Companyorder();
                ordnew.setCreateddate(new Date());
                ordnew.setCreatedfrom(EBISystem.ebiUser);

                ordnew.setCompany(order.getCompany());
                ordnew.setOfferdate(order.getOfferdate());
                ordnew.setValidto(order.getValidto());

                ordnew.setDescription(order.getDescription());
                ordnew.setOrdernr(order.getOrdernr());
                ordnew.setName(order.getName() + " - (Copy)");
                ordnew.setIsrecieved(order.getIsrecieved());
                ordnew.setOfferid(order.getOfferid());
                ordnew.setStatus(order.getStatus());

                EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(ordnew);

                if (!order.getCompanyorderdocses().isEmpty()) {
                    final Iterator itd = order.getCompanyorderdocses().iterator();
                    while (itd.hasNext()) {
                        final Companyorderdocs docs = (Companyorderdocs) itd.next();

                        final Companyorderdocs dc = new Companyorderdocs();
                        dc.setCompanyorder(ordnew);
                        dc.setCreateddate(new Date());
                        dc.setCreatedfrom(EBISystem.ebiUser);
                        dc.setFiles(docs.getFiles());
                        dc.setName(docs.getName());
                        ordnew.getCompanyorderdocses().add(dc);
                        EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(dc);
                    }
                }

                //Save position
                if (!order.getCompanyorderpositionses().isEmpty()) {
                    final Iterator itp = order.getCompanyorderpositionses().iterator();
                    while (itp.hasNext()) {
                        final Companyorderpositions pos = (Companyorderpositions) itp.next();

                        final Companyorderpositions p = new Companyorderpositions();
                        p.setCompanyorder(ordnew);
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
                        ordnew.getCompanyorderpositionses().add(p);
                        EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(p);
                    }
                }
                // Save Receiver
                if (!order.getCompanyorderreceivers().isEmpty()) {
                    final Iterator itr = order.getCompanyorderreceivers().iterator();
                    while (itr.hasNext()) {
                        final Companyorderreceiver rec = (Companyorderreceiver) itr.next();

                        final Companyorderreceiver r = new Companyorderreceiver();
                        r.setCompanyorder(ordnew);
                        r.setCnum(rec.getCnum());
                        r.setCountry(rec.getCountry());
                        r.setCreateddate(new Date());
                        r.setCreatedfrom(EBISystem.ebiUser);
                        r.setEmail(rec.getEmail());
                        r.setFax(rec.getFax());
                        r.setGender(rec.getGender());
                        r.setLocation(rec.getLocation());
                        r.setMittelname(rec.getMittelname());
                        r.setName(rec.getName());
                        r.setPbox(rec.getPbox());
                        r.setPhone(rec.getPhone());
                        r.setPosition(rec.getPosition());
                        r.setReceivervia(rec.getReceivervia());
                        r.setStreet(rec.getStreet());
                        r.setSurname(rec.getSurname());
                        r.setZip(rec.getZip());
                        ordnew.getCompanyorderreceivers().add(r);
                        EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(r);
                    }
                }

                EBISystem.getInstance().company.getCompanyorders().add(ordnew);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();

                EBISystem.gui().table("companyorderTable", "Order").
                        changeSelection(EBISystem.gui().table("companyorderTable", "Order").
                                convertRowIndexToView(EBISystem.getCRMModule().dynMethod.
                                        getIdIndexFormArrayInATable(EBISystem.getCRMModule().getOrderPane().tabModOrder.data, 7, ordnew.getOrderid())), 0, false, false);

            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void dataEdit(final int id) {

        if (EBISystem.getInstance().company.getCompanyorders().size() > 0) {

        	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
        		if(ordObj.getOrderid() == id) {
        			compOrder = ordObj;
        			break;
        		}
        	}

            EBISystem.gui().vpanel("Order").setID(compOrder.getOrderid());
            EBISystem.gui().vpanel("Order").setCreatedDate(EBISystem.getInstance().getDateToString(compOrder.getCreateddate() == null ? new Date() : compOrder.getCreateddate()));
            EBISystem.gui().vpanel("Order").setCreatedFrom(compOrder.getCreatedfrom() == null ? EBISystem.ebiUser : compOrder.getCreatedfrom());

            if (compOrder.getOfferdate() != null) {
                EBISystem.gui().timePicker("orderCreatedText", "Order").setDate(compOrder.getOfferdate());
                EBISystem.gui().timePicker("orderCreatedText", "Order").
                        getEditor().setText(EBISystem.getInstance().getDateToString(compOrder.getOfferdate()));
            }

            if (compOrder.getValidto() != null) {
                EBISystem.gui().timePicker("orderReceiveText", "Order").setDate(compOrder.getValidto());
                EBISystem.gui().timePicker("orderReceiveText", "Order").
                        getEditor().setText(EBISystem.getInstance().getDateToString(compOrder.getValidto()));
            }

            if (compOrder.getChangeddate() != null) {
                EBISystem.gui().vpanel("Order").setChangedDate(EBISystem.getInstance().getDateToString(compOrder.getChangeddate()));
                EBISystem.gui().vpanel("Order").setChangedFrom(compOrder.getChangedfrom());
            } else {
                EBISystem.gui().vpanel("Order").setChangedDate("");
                EBISystem.gui().vpanel("Order").setChangedFrom("");
            }

            EBISystem.gui().textField("orderNameText", "Order").setText(compOrder.getName());
            EBISystem.gui().textField("orderNrText", "Order").setText(compOrder.getOrdernr() == null ? "" : compOrder.getOrdernr());

            if (compOrder.getStatus() != null) {
                EBISystem.gui().combo("orderStatusText", "Order").setSelectedItem(compOrder.getStatus());
            }

            if (compOrder.getOfferid() != null) {
                EBISystem.gui().textField("orderOfferText", "Order").setText(getOfferName(compOrder.getOfferid()));
            }

            EBISystem.gui().textArea("orderDescription", "Order").setText(compOrder.getDescription());
 
            EBISystem.getInstance().getDataStore("Order", "ebiEdit");
            EBISystem.gui().table("companyorderTable", "Order").
                    changeSelection(EBISystem.gui().table("companyorderTable", "Order").
                            convertRowIndexToView(EBISystem.getCRMModule().dynMethod.
                                    getIdIndexFormArrayInATable(EBISystem.getCRMModule().getOrderPane().tabModOrder.data, 7, id)), 0, false, false);
        } else {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_RECORD_NOT_FOUND")).Show(EBIMessage.INFO_MESSAGE);
        }
    }

    public void createInvoiceFromOrder(final int id) {

        if (!EBISystem.getCRMModule().crmToolBar.isInvoiceSelected()) {
            EBISystem.getCRMModule().crmToolBar.enableToolButtonInvoice();
            EBISystem.getCRMModule().ebiContainer.showClosableInvoiceContainer();
            EBISystem.getCRMModule().ebiContainer.setSelectedTab(EBISystem.getInstance().getIEBIContainerInstance().getIndexByTitle(EBISystem.i18n("EBI_LANG_C_TAB_INVOICE")));
        } else {
            EBISystem.getCRMModule().getInvoicePane().dataControlInvoice.dataNew();
            EBISystem.getCRMModule().ebiContainer.setSelectedTab(EBISystem.getInstance().getIEBIContainerInstance().getIndexByTitle(EBISystem.i18n("EBI_LANG_C_TAB_INVOICE")));
        }

        if (EBISystem.getInstance().company.getCompanyorders().size() > 0){
        	Companyorder ord = null;
        	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
        		if(ordObj.getOrderid() == id) {
        			ord = ordObj;
        			break;
        		}
        	}

            //Invoice field
            EBISystem.gui().textField("invoiceNameText", "Invoice").setText(ord.getName());
            EBISystem.gui().textField("orderText", "Invoice").setText(EBISystem.i18n("EBI_LANG_C_ORDER") + ": " + ord.getOrderid());

            if (!ord.getCompanyorderpositionses().isEmpty()) {
                final Iterator ip = ord.getCompanyorderpositionses().iterator();
                while (ip.hasNext()) {
                    final Companyorderpositions pos = (Companyorderpositions) ip.next();
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

            if (!ord.getCompanyorderreceivers().isEmpty()) {
                final Iterator ir = ord.getCompanyorderreceivers().iterator();
                final int size = ord.getCompanyorderreceivers().size();
                while (ir.hasNext()) {
                    final Companyorderreceiver re = (Companyorderreceiver) ir.next();
                    if (re.getCnum() != null && re.getCnum() == 1) {
                        //Invoice contact field
                        EBISystem.gui().combo("genderText", "Invoice").setSelectedItem(re.getGender());
                        EBISystem.gui().textField("titleText", "Invoice").setText(re.getPosition());
                        EBISystem.gui().textField("companyNameText", "Invoice").setText(ord.getCompany().getName());
                        EBISystem.gui().textField("nameText", "Invoice").setText(re.getName());
                        EBISystem.gui().textField("surnameText", "Invoice").setText(re.getSurname());
                        EBISystem.gui().textField("streetNrText", "Invoice").setText(re.getStreet());
                        EBISystem.gui().textField("zipText", "Invoice").setText(re.getZip());
                        EBISystem.gui().textField("locationText", "Invoice").setText(re.getLocation());
                        EBISystem.gui().textField("postCodeText", "Invoice").setText(re.getPbox());
                        EBISystem.gui().textField("countryText", "Invoice").setText(re.getCountry());
                        EBISystem.gui().textField("telefonText", "Invoice").setText(re.getPhone());
                        EBISystem.gui().textField("faxText", "Invoice").setText(re.getFax());
                        EBISystem.gui().textField("emailText", "Invoice").setText(re.getEmail());
                        EBISystem.gui().textField("internetText", "Invoice").setText(ord.getCompany().getWeb());
                        break;
                    } else if (size == 1) {
                        EBISystem.gui().combo("genderText", "Invoice").setSelectedItem(re.getGender());
                        EBISystem.gui().textField("titleText", "Invoice").setText(re.getPosition());
                        EBISystem.gui().textField("companyNameText", "Invoice").setText(ord.getCompany().getName());
                        EBISystem.gui().textField("nameText", "Invoice").setText(re.getName());
                        EBISystem.gui().textField("surnameText", "Invoice").setText(re.getSurname());
                        EBISystem.gui().textField("streetNrText", "Invoice").setText(re.getStreet());
                        EBISystem.gui().textField("zipText", "Invoice").setText(re.getZip());
                        EBISystem.gui().textField("locationText", "Invoice").setText(re.getLocation());
                        EBISystem.gui().textField("postCodeText", "Invoice").setText(re.getPbox());
                        EBISystem.gui().textField("countryText", "Invoice").setText(re.getCountry());
                        EBISystem.gui().textField("telefonText", "Invoice").setText(re.getPhone());
                        EBISystem.gui().textField("faxText", "Invoice").setText(re.getFax());
                        EBISystem.gui().textField("emailText", "Invoice").setText(re.getEmail());
                        EBISystem.gui().textField("internetText", "Invoice").setText(ord.getCompany().getWeb());

                    }
                }
            }
        } else {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_RECORD_NOT_FOUND")).Show(EBIMessage.INFO_MESSAGE);
        }
    }

    public void createServiceFromOrder(final int id) {

        EBISystem.getCRMModule().getServicePane().dataControlService.dataNew();

        if (EBISystem.getInstance().company.getCompanyorders().size() > 0) {
            
        	Companyorder ord = null;
        	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
        		if(ordObj.getOrderid() == id) {
        			ord = ordObj;
        			break;
        		}
        	}
        	
            EBISystem.gui().textField("serviceNrText", "Service").setText(ord.getOrdernr() == null ? "" : ord.getOrdernr());
            EBISystem.gui().textField("serviceNameText", "Service").setText(ord.getName());

            EBISystem.gui().textArea("serviceDescriptionText", "Service").setText(ord.getDescription());
            EBISystem.getCRMModule().ebiContainer.setSelectedTab(EBISystem.getInstance().getIEBIContainerInstance().getIndexByTitle(EBISystem.i18n("EBI_LANG_C_SERVICE")));

            if (ord.getCompanyorderpositionses().size() > 0) {
                final Iterator it = ord.getCompanyorderpositionses().iterator();
                if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_WOULD_YOU_IMPORT_PRODUCT")).Show(EBIMessage.INFO_MESSAGE_YESNO) == true) {

                    Companyorderpositions posi;
                    while (it.hasNext()) {

                        posi = (Companyorderpositions) it.next();

                        final Companyservicepositions servPos = new Companyservicepositions();
                        servPos.setProductid(posi.getProductid());
                        servPos.setCategory(posi.getCategory());
                        servPos.setDeduction(posi.getDeduction());
                        servPos.setDescription(posi.getDescription());
                        servPos.setNetamount(posi.getNetamount());
                        servPos.setPretax(posi.getPretax());
                        servPos.setProductname(posi.getProductname());
                        servPos.setProductnr(posi.getProductnr());
                        servPos.setQuantity(posi.getQuantity());
                        servPos.setTaxtype(posi.getTaxtype());
                        servPos.setType(posi.getType());
                        EBISystem.getCRMModule().getServicePane().dataControlService.getservicePosList().add(servPos);
                    }
                    EBISystem.getCRMModule().getServicePane().dataControlService.dataShowProduct();
                }
            }


            if (ord.getCompanyorderdocses().size() > 0) {
                final Iterator itr = ord.getCompanyorderdocses().iterator();
                if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_WOULD_YOU_IMPORT_DOCUMENT")).Show(EBIMessage.INFO_MESSAGE_YESNO) == true) {

                    while (itr.hasNext()) {

                        final Companyorderdocs obj = (Companyorderdocs) itr.next();
                        final Companyservicedocs docs = new Companyservicedocs();
                        docs.setName(obj.getName());
                        docs.setCreateddate(new java.sql.Date(new java.util.Date().getTime()));
                        docs.setCreatedfrom(EBISystem.ebiUser);
                        docs.setFiles(obj.getFiles());
                        EBISystem.getCRMModule().getServicePane().dataControlService.getserviceDocList().add(docs);

                    }
                    EBISystem.getCRMModule().getServicePane().dataControlService.dataShowDoc();
                }
            }
        } else {
            EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_C_RECORD_NOT_FOUND")).Show(EBIMessage.INFO_MESSAGE);
        }
    }

    public void dataDelete(final int id) {
        if (EBISystem.getInstance().company.getCompanyorders().size() > 0) {
        	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
        		if(ordObj.getOrderid() == id) {
        			compOrder = ordObj;
        			break;
        		}
        	}
            EBISystem.getInstance().getDataStore("Order", "ebiDelete");
            EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
            EBISystem.hibernate().session("EBICRM_SESSION").saveOrUpdate(compOrder);
            EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
            EBISystem.getInstance().company.getCompanyorders().remove(compOrder);
        }
    }

    public void dataShow() {

        final int srow = EBISystem.gui().table("companyorderTable", "Order").getSelectedRow();
        final int size = EBISystem.getInstance().company.getCompanyorders().size();

        if (size > 0) {
            EBISystem.getCRMModule().getOrderPane().tabModOrder.data = new Object[size][8];
            final Iterator<Companyorder> iter = EBISystem.getInstance().company.getCompanyorders().iterator();
            int i = 0;
            while (iter.hasNext()) {
                final Companyorder order = iter.next();
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][0] = order.getName() == null ? "" : order.getName();
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][1] = order.getOfferdate() == null ? "" : EBISystem.getInstance().getDateToString(order.getOfferdate());
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][2] = order.getValidto() == null ? "" : EBISystem.getInstance().getDateToString(order.getValidto());
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][3] = String.valueOf(order.getOfferid() == null ? "0" : order.getOfferid());
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][4] = order.getStatus() == null ? "" : order.getStatus();
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][5] = order.getDescription() == null ? "" : order.getDescription();
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][6] = order.getIsrecieved() == null ? 0 : order.getIsrecieved();
                EBISystem.getCRMModule().getOrderPane().tabModOrder.data[i][7] = order.getOrderid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getOrderPane().tabModOrder.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", "", ""}};
        }
        EBISystem.getCRMModule().getOrderPane().tabModOrder.fireTableDataChanged();
        EBISystem.gui().table("companyorderTable", "Order").changeSelection(srow, 0, false, false);
    }

    public Hashtable<String, Double> getTaxName(final int id) {

        final NumberFormat cashFormat = NumberFormat.getCurrencyInstance();
        cashFormat.setMinimumFractionDigits(2);
        cashFormat.setMaximumFractionDigits(3);
        final Hashtable<String, Double> taxTable = new Hashtable<String, Double>();

        if (EBISystem.getInstance().company.getCompanyorders().size() > 0) {
        	Companyorder or = null;
        	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
        		if(ordObj.getOrderid() == id) {
        			or = ordObj;
        			break;
        		}
        	}
            final Iterator itx = or.getCompanyorderpositionses().iterator();
            while (itx.hasNext()) {
                final Companyorderpositions pos = (Companyorderpositions) itx.next();
                if (pos.getTaxtype() != null) {
                    if (taxTable.containsKey(pos.getTaxtype())) {
                        taxTable.put(pos.getTaxtype(), taxTable.get(pos.getTaxtype()) + (((pos.getNetamount() * pos.getQuantity().intValue()) * EBISystem.getCRMModule().dynMethod.getTaxVal(pos.getTaxtype())) / 100));
                    } else {
                        taxTable.put(pos.getTaxtype(), (((pos.getNetamount() * pos.getQuantity().intValue()) * EBISystem.getCRMModule().dynMethod.getTaxVal(pos.getTaxtype())) / 100));
                    }
                }
            }
        }
        return taxTable;
    }

    public void dataShowReport(final int id) {

        if (isEdit) {
            if (!dataStore()) {
                return;
            }
        }

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("ID", id);
        final Hashtable<String, Double> taxTable = getTaxName(id);
        final Iterator itx = taxTable.keySet().iterator();

        String taxTypes = "";
        String taxValues = "";

        final NumberFormat cashFormat = NumberFormat.getCurrencyInstance();
        cashFormat.setMinimumFractionDigits(2);
        cashFormat.setMaximumFractionDigits(3);

        while (itx.hasNext()) {
            final String key = ((String) itx.next());
            taxTypes += key + ":\n";
            taxValues += cashFormat.format(taxTable.get(key)) + "\n";
        }

        map.put("TAXDIFF_TEXT", taxTypes);
        map.put("TAXDIFF_VALUE", taxValues);

        EBISystem.getInstance().getIEBIReportSystemInstance().
                useReportSystem(map,
                        EBISystem.getInstance().convertReportCategoryToIndex(EBISystem.i18n("EBI_LANG_C_ORDER")),
                        getOrderNamefromId(id));
    }

    public String dataShowAndMailReport(final int id, final boolean showWindow) {
        String fileName = "";
        String to = "";

        if (isEdit) {
            if (!dataStore()) {
                return null;
            }
        }

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("ID", id);

        final Hashtable<String, Double> taxTable = getTaxName(id);
        final Iterator itx = taxTable.keySet().iterator();

        String taxTypes = "";
        String taxValues = "";

        final NumberFormat cashFormat = NumberFormat.getCurrencyInstance();
        cashFormat.setMinimumFractionDigits(2);
        cashFormat.setMaximumFractionDigits(3);

        while (itx.hasNext()) {
            final String key = ((String) itx.next());
            taxTypes += key + ":\n";
            taxValues += cashFormat.format(taxTable.get(key)) + "\n";
        }

        map.put("TAXDIFF_TEXT", taxTypes);
        map.put("TAXDIFF_VALUE", taxValues);

        try {

        	if (EBISystem.getInstance().company.getCompanyorders().size() > 0) {
            	Companyorder order = null;
            	for(Companyorder ordObj : EBISystem.getInstance().company.getCompanyorders()) {
            		if(ordObj.getOrderid() == id) {
            			order = ordObj;
            			break;
            		}
            	}

                final Iterator ix = order.getCompanyorderreceivers().iterator();
                int i = 1;
                final int c = order.getCompanyorderreceivers().size();
                while (ix.hasNext()) {
                    final Companyorderreceiver rec = (Companyorderreceiver) ix.next();
                    to += rec.getEmail();
                    if (i < c) {
                        to += ";";
                    }
                    i++;
                }
                fileName = EBISystem.getInstance().getIEBIReportSystemInstance().useReportSystem(map,
                        EBISystem.getInstance().convertReportCategoryToIndex(EBISystem.i18n("EBI_LANG_C_ORDER")),
                        getOrderNamefromId(id), showWindow, true, to);
            } else {
                EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_NO_RECEIVER_WAS_FOUND")).Show(EBIMessage.ERROR_MESSAGE);
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
            EBIExceptionDialog.getInstance(EBISystem.printStackTrace(ex)).Show(EBIMessage.ERROR_MESSAGE);
        }

        return fileName;
    }

    public void dataNew() {
        compOrder = new Companyorder();
        EBISystem.getCRMModule().getOrderPane().initialize(false);
        EBISystem.gui().vpanel("Order").setID(-1);
        EBISystem.getInstance().getDataStore("Order", "ebiNew");
    }

    private String getOfferName(final int id) {
        String retName = "";

        final Iterator iter = EBISystem.getCRMModule().getOfferPane().dataControlOffer.getOfferList().iterator();
        while (iter.hasNext()) {
            final Companyoffer offer = (Companyoffer) iter.next();
            if (offer.getOfferid() == id) {
                this.offerID = offer.getOfferid();
                retName = offer.getName();
                break;
            }
        }
        return retName;
    }

    private void createHistory(final Company com) {

        final List<String> list = new ArrayList<String>();

        list.add(EBISystem.i18n("EBI_LANG_ADDED") + ": " + EBISystem.getInstance().getDateToString(compOrder.getCreateddate()));
        list.add(EBISystem.i18n("EBI_LANG_ADDED_FROM") + ": " + compOrder.getCreatedfrom());

        if (compOrder.getChangeddate() != null) {
            list.add(EBISystem.i18n("EBI_LANG_CHANGED") + ": " + EBISystem.getInstance().getDateToString(compOrder.getChangeddate()));
            list.add(EBISystem.i18n("EBI_LANG_CHANGED_FROM") + ": " + compOrder.getChangedfrom());
        }

        list.add(EBISystem.i18n("EBI_LANG_ORDER_NUMBER") + ": " + (compOrder.getOrdernr().equals(EBISystem.gui().textField("orderNrText", "Order").getText()) == true ? compOrder.getOrdernr() : compOrder.getOrdernr() + "$"));
        list.add(EBISystem.i18n("EBI_LANG_NAME") + ": " + (compOrder.getName().equals(EBISystem.gui().textField("orderNameText", "Order").getText()) == true ? compOrder.getName() : compOrder.getName() + "$"));

        list.add(EBISystem.i18n("EBI_LANG_C_STATUS") + ": " + (compOrder.getStatus().equals(EBISystem.gui().combo("orderStatusText", "Order").getSelectedItem().toString()) == true ? compOrder.getStatus() : compOrder.getStatus() + "$"));
        list.add(EBISystem.i18n("EBI_LANG_DESCRIPTION") + ": " + (compOrder.getDescription().equals(EBISystem.gui().textArea("orderDescription", "Order").getText()) == true ? compOrder.getDescription() : compOrder.getDescription() + "$"));

        list.add(EBISystem.i18n("EBI_LANG_CREATED_DATE") + ": " + (EBISystem.getInstance().getDateToString(compOrder.getOfferdate()).equals(EBISystem.gui().timePicker("orderCreatedText", "Order").getEditor().getText()) == true ? EBISystem.getInstance().getDateToString(compOrder.getOfferdate()) : EBISystem.getInstance().getDateToString(compOrder.getOfferdate()) + "$"));
        list.add(EBISystem.i18n("EBI_LANG_RECEIVED_DATE") + ": " + (EBISystem.getInstance().getDateToString(compOrder.getValidto()).equals(EBISystem.gui().timePicker("orderReceiveText", "Order").getEditor().getText()) == true ? EBISystem.getInstance().getDateToString(compOrder.getValidto()) : EBISystem.getInstance().getDateToString(compOrder.getValidto()) + "$"));
        list.add("*EOR*"); // END OF RECORD


        if (!compOrder.getCompanyorderdocses().isEmpty()) {

            final Iterator iter = compOrder.getCompanyorderdocses().iterator();
            while (iter.hasNext()) {
                final Companyorderdocs obj = (Companyorderdocs) iter.next();
                list.add(obj.getName() == null ? EBISystem.i18n("EBI_LANG_FILENAME") + ": " : EBISystem.i18n("EBI_LANG_FILENAME") + ": " + obj.getName());
                list.add(EBISystem.getInstance().getDateToString(obj.getCreateddate()) == null ? EBISystem.i18n("EBI_LANG_C_ADDED_DATE") + ": " : EBISystem.i18n("EBI_LANG_C_ADDED_DATE") + ": " + EBISystem.getInstance().getDateToString(obj.getCreateddate()));
                list.add(obj.getCreatedfrom() == null ? EBISystem.i18n("EBI_LANG_ADDED_FROM") + ": " : EBISystem.i18n("EBI_LANG_ADDED_FROM") + ": " + obj.getCreatedfrom());
                list.add("*EOR*");
            }
        }

        if (!compOrder.getCompanyorderpositionses().isEmpty()) {

            final Iterator iter = compOrder.getCompanyorderpositionses().iterator();

            while (iter.hasNext()) {
                final Companyorderpositions obj = (Companyorderpositions) iter.next();
                list.add(EBISystem.i18n("EBI_LANG_QUANTITY") + ": " + String.valueOf(obj.getQuantity()));
                list.add(EBISystem.i18n("EBI_LANG_PRODUCT_NUMBER") + ": " + obj.getProductnr());
                list.add(obj.getProductname() == null ? EBISystem.i18n("EBI_LANG_NAME") + ":" : EBISystem.i18n("EBI_LANG_NAME") + ": " + obj.getProductname());
                list.add(obj.getCategory() == null ? EBISystem.i18n("EBI_LANG_CATEGORY") + ":" : EBISystem.i18n("EBI_LANG_CATEGORY") + ": " + obj.getCategory());
                list.add(obj.getTaxtype() == null ? EBISystem.i18n("EBI_LANG_TAX") + ":" : EBISystem.i18n("EBI_LANG_TAX") + ": " + obj.getTaxtype());
                list.add(String.valueOf(obj.getPretax()) == null ? EBISystem.i18n("EBI_LANG_PRICE") + ":" : EBISystem.i18n("EBI_LANG_PRICE") + ": " + String.valueOf(obj.getPretax()));
                list.add(String.valueOf(obj.getDeduction()) == null ? EBISystem.i18n("EBI_LANG_DEDUCTION") + ":" : EBISystem.i18n("EBI_LANG_DEDUCTION") + ": " + String.valueOf(obj.getDeduction()));
                list.add(obj.getDescription() == null ? EBISystem.i18n("EBI_LANG_DESCRIPTION") + ":" : EBISystem.i18n("EBI_LANG_DESCRIPTION") + ": " + obj.getDescription());
                list.add("*EOR*");

            }
        }

        if (!compOrder.getCompanyorderreceivers().isEmpty()) {

            final Iterator iter = compOrder.getCompanyorderreceivers().iterator();
            while (iter.hasNext()) {
                final Companyorderreceiver obj = (Companyorderreceiver) iter.next();
                list.add(obj.getReceivervia() == null ? EBISystem.i18n("EBI_LANG_C_SEND_TYPE") + ":" : EBISystem.i18n("EBI_LANG_C_SEND_TYPE") + ": " + obj.getReceivervia());
                list.add(obj.getGender() == null ? EBISystem.i18n("EBI_LANG_C_GENDER") + ":" : EBISystem.i18n("EBI_LANG_C_GENDER") + ": " + obj.getGender());
                list.add(obj.getSurname() == null ? EBISystem.i18n("EBI_LANG_NAME") + ":" : EBISystem.i18n("EBI_LANG_NAME") + ": " + obj.getSurname());
                list.add(obj.getName() == null ? EBISystem.i18n("EBI_LANG_C_CNAME") + ":" : EBISystem.i18n("EBI_LANG_C_CNAME") + ": " + obj.getName());
                list.add(obj.getPosition() == null ? EBISystem.i18n("EBI_LANG_CONTACT_POSITION") + ":" : EBISystem.i18n("EBI_LANG_CONTACT_POSITION") + ": " + obj.getPosition());
                list.add(obj.getStreet() == null ? EBISystem.i18n("EBI_LANG_C_STREET_NR") + ":" : EBISystem.i18n("EBI_LANG_C_STREET_NR") + ": " + obj.getStreet());
                list.add(obj.getZip() == null ? EBISystem.i18n("EBI_LANG_C_ZIP") + ":" : EBISystem.i18n("EBI_LANG_C_ZIP") + ": " + obj.getZip());
                list.add(obj.getLocation() == null ? EBISystem.i18n("EBI_LANG_C_LOCATION") + ":" : EBISystem.i18n("EBI_LANG_C_LOCATION") + ": " + obj.getLocation());
                list.add(obj.getPbox() == null ? EBISystem.i18n("EBI_LANG_C_POST_CODE") + ":" : EBISystem.i18n("EBI_LANG_C_POST_CODE") + ": " + obj.getPbox());
                list.add(obj.getCountry() == null ? EBISystem.i18n("EBI_LANG_C_COUNTRY") + ":" : EBISystem.i18n("EBI_LANG_C_COUNTRY") + ": " + obj.getCountry());
                list.add("*EOR*");
            }
        }

        try {
            EBISystem.getCRMModule().hcreator.setDataToCreate(new EBICRMHistoryDataUtil(com.getCompanyid(), "Order", list));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void dataNewDoc() {
        final File fs = EBISystem.getInstance().getOpenDialog(JFileChooser.FILES_ONLY);
        if (fs != null) {
            final byte[] file = EBISystem.getInstance().readFileToByte(fs);
            if (file != null) {
                final Companyorderdocs docs = new Companyorderdocs();
                docs.setOrderdocid((compOrder.getCompanyorderdocses().size()+1) * -1);
                docs.setCompanyorder(compOrder);
                docs.setName(fs.getName());
                docs.setCreateddate(new java.sql.Date(new java.util.Date().getTime()));
                docs.setCreatedfrom(EBISystem.ebiUser);
                docs.setFiles(file);
                compOrder.getCompanyorderdocses().add(docs);
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

            final Iterator iter = this.compOrder.getCompanyorderdocses().iterator();
            while (iter.hasNext()) {

                final Companyorderdocs doc = (Companyorderdocs) iter.next();

                if (id == doc.getOrderdocid()) {
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
        if (this.compOrder.getCompanyorderdocses().size() > 0) {
            EBISystem.getCRMModule().getOrderPane().tabModDoc.data = new Object[this.compOrder.getCompanyorderdocses().size()][4];

            final Iterator itr = this.compOrder.getCompanyorderdocses().iterator();
            int i = 0;
            while (itr.hasNext()) {
                final Companyorderdocs obj = (Companyorderdocs) itr.next();
                EBISystem.getCRMModule().getOrderPane().tabModDoc.data[i][0] = obj.getName() == null ? "" : obj.getName();
                EBISystem.getCRMModule().getOrderPane().tabModDoc.data[i][1] = EBISystem.getInstance().getDateToString(obj.getCreateddate()) == null ? "" : EBISystem.getInstance().getDateToString(obj.getCreateddate());
                EBISystem.getCRMModule().getOrderPane().tabModDoc.data[i][2] = obj.getCreatedfrom() == null ? "" : obj.getCreatedfrom();
                EBISystem.getCRMModule().getOrderPane().tabModDoc.data[i][3] = obj.getOrderdocid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getOrderPane().tabModDoc.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", ""}};
        }
        EBISystem.getCRMModule().getOrderPane().tabModDoc.fireTableDataChanged();
    }

    public void dataShowProduct() {

        if (compOrder.getCompanyorderpositionses().size() > 0) {
            EBISystem.getCRMModule().getOrderPane().tabModProduct.data = new Object[this.compOrder.getCompanyorderpositionses().size()][9];

            final Iterator itr = compOrder.getCompanyorderpositionses().iterator();
            int i = 0;

            final NumberFormat currency = NumberFormat.getCurrencyInstance();

            while (itr.hasNext()) {
                final Companyorderpositions obj = (Companyorderpositions) itr.next();
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][0] = String.valueOf(obj.getQuantity());
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][1] = obj.getProductnr();
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][2] = obj.getProductname() == null ? "" : obj.getProductname();
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][3] = obj.getCategory() == null ? "" : obj.getCategory();
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][4] = obj.getTaxtype() == null ? "" : obj.getTaxtype();
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][5] = currency.format(EBISystem.getCRMModule().dynMethod.calculatePreTaxPrice(obj.getNetamount(), String.valueOf(obj.getQuantity()), String.valueOf(obj.getDeduction()))) == null ? "" : currency.format(EBISystem.getCRMModule().dynMethod.calculatePreTaxPrice(obj.getNetamount(), String.valueOf(obj.getQuantity()), String.valueOf(obj.getDeduction())));
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][6] = obj.getDeduction().equals("") ? "" : obj.getDeduction() + "%";
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][7] = obj.getDescription() == null ? "" : obj.getDescription();
                EBISystem.getCRMModule().getOrderPane().tabModProduct.data[i][8] = obj.getPositionid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getOrderPane().tabModProduct.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", "", ""}};
        }
        EBISystem.getCRMModule().getOrderPane().tabModProduct.fireTableDataChanged();
    }

    public void dataShowReceiver() {
        if (this.compOrder.getCompanyorderreceivers().size() > 0) {
            EBISystem.getCRMModule().getOrderPane().tabModReceiver.data = new Object[this.compOrder.getCompanyorderreceivers().size()][12];
            final Iterator itr = this.compOrder.getCompanyorderreceivers().iterator();
            int i = 0;
            while (itr.hasNext()) {
                final Companyorderreceiver obj = (Companyorderreceiver) itr.next();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][0] = obj.getReceivervia() == null ? "" : obj.getReceivervia();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][1] = obj.getGender() == null ? "" : obj.getGender();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][2] = obj.getSurname() == null ? "" : obj.getSurname();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][3] = obj.getName() == null ? "" : obj.getName();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][4] = obj.getPosition() == null ? "" : obj.getPosition();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][5] = obj.getStreet() == null ? "" : obj.getStreet();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][6] = obj.getZip() == null ? "" : obj.getZip();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][7] = obj.getLocation() == null ? "" : obj.getLocation();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][8] = obj.getPbox() == null ? "" : obj.getPbox();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][9] = obj.getCountry() == null ? "" : obj.getCountry();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][10] = obj.getEmail() == null ? "" : obj.getEmail();
                EBISystem.getCRMModule().getOrderPane().tabModReceiver.data[i][11] = obj.getReceiverid();
                i++;
            }
        } else {
            EBISystem.getCRMModule().getOrderPane().tabModReceiver.data = new Object[][]{{EBISystem.i18n("EBI_LANG_PLEASE_SELECT"), "", "", "", "", "", "", "", "", "",""}};
        }
        EBISystem.getCRMModule().getOrderPane().tabModReceiver.fireTableDataChanged();
    }

    public void dataDeleteDoc(final int id) {
        final Iterator iter = this.compOrder.getCompanyorderdocses().iterator();
        while (iter.hasNext()) {

            final Companyorderdocs doc = (Companyorderdocs) iter.next();

            if (doc.getOrderdocid() == id) {
                this.compOrder.getCompanyorderdocses().remove(doc);
                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                EBISystem.hibernate().session("EBICRM_SESSION").delete(doc);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                break;
            }
        }
    }

    public void dataDeleteReceiver(final int id) {
        final Iterator iter = this.compOrder.getCompanyorderreceivers().iterator();
        while (iter.hasNext()) {
            final Companyorderreceiver orderrec = (Companyorderreceiver) iter.next();
            if (orderrec.getReceiverid() == id) {
                this.compOrder.getCompanyorderreceivers().remove(orderrec);
                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                EBISystem.hibernate().session("EBICRM_SESSION").delete(orderrec);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                break;
            }
        }
    }

    public void dataEditReceiver(final int id) {
        final Iterator iter = this.compOrder.getCompanyorderreceivers().iterator();
        while (iter.hasNext()) {
            final Companyorderreceiver orderrec = (Companyorderreceiver) iter.next();
            if (orderrec.getReceiverid() == id) {
                final EBICRMAddContactAddressType addCo = new EBICRMAddContactAddressType(this, orderrec);
                addCo.setVisible();
                break;
            }
        }
    }

    public void dataDeleteProduct(final int id) {
        final Iterator iter = this.compOrder.getCompanyorderpositionses().iterator();
        while (iter.hasNext()) {
            final Companyorderpositions orderpro = (Companyorderpositions) iter.next();
            if (orderpro.getPositionid() == id) {
                this.compOrder.getCompanyorderpositionses().remove(orderpro);
                EBISystem.hibernate().transaction("EBICRM_SESSION").begin();
                EBISystem.hibernate().session("EBICRM_SESSION").delete(orderpro);
                EBISystem.hibernate().transaction("EBICRM_SESSION").commit();
                break;
            }
        }
    }

    public Companyorder getCompOrder() {
        return compOrder;
    }

    public void setOfferID(final int offerID) {
        this.offerID = offerID;
    }

    private String getOrderNamefromId(final int id) {
        String name = "";
        final Iterator iter = EBISystem.getInstance().company.getCompanyorders().iterator();

        while (iter.hasNext()) {
            final Companyorder order = (Companyorder) iter.next();
            if (order.getOrderid() == id) {
                name = order.getName();
                break;
            }
        }
        return name;
    }
}