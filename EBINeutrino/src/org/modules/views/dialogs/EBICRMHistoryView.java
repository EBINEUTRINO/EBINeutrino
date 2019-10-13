package org.modules.views.dialogs;

import org.modules.utils.EBICRMHistoryDataUtil;
import org.sdk.EBISystem;

import java.util.Iterator;
import java.util.List;

public class EBICRMHistoryView {

    private List<EBICRMHistoryDataUtil> list = null;

    public EBICRMHistoryView(final List<EBICRMHistoryDataUtil> list) {
        this.list = list;
        EBISystem.gui().loadGUI("CRMDialog/crmHistoryDialog.xml");
    }

    public void setVisible() {

        EBISystem.gui().dialog("historyViewDialog").setTitle(EBISystem.i18n("EBI_LANG_HISTORY_VIEW"));
        EBISystem.gui().vpanel("historyViewDialog").setModuleTitle(EBISystem.i18n("EBI_LANG_HISTORY_VIEW"));
        EBISystem.gui().label("history", "historyViewDialog").setText(EBISystem.i18n("EBI_LANG_HISTORY"));
        EBISystem.gui().combo("historyText", "historyViewDialog").addItem(EBISystem.i18n("EBI_LANG_PLEASE_SELECT"));
        EBISystem.gui().button("viewHistory", "historyViewDialog").setText(EBISystem.i18n("EBI_LANG_VIEW"));
        EBISystem.gui().button("viewHistory", "historyViewDialog").addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                if (!EBISystem.i18n("EBI_LANG_PLEASE_SELECT").equals(EBISystem.gui().combo("historyText", "historyViewDialog").getSelectedItem().toString())) {
                    showValueAsHTML();
                }
            }
        });

        EBISystem.gui().button("closeButton", "historyViewDialog").setText(EBISystem.i18n("EBI_LANG_CLOSE"));
        EBISystem.gui().button("closeButton", "historyViewDialog").addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                EBISystem.gui().dialog("historyViewDialog").setVisible(false);
            }
        });
        fillComboHistory();
        EBISystem.gui().showGUI();

    }

    private void fillComboHistory() {
        final Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            final EBICRMHistoryDataUtil util = (EBICRMHistoryDataUtil) iter.next();
            EBISystem.gui().combo("historyText", "historyViewDialog").addItem(String.valueOf(++i) + " " + util.getChangedFrom() + " | " + util.getChangedDate() + " | " + util.getCategory());
        }
    }

    private void showValueAsHTML() {

        final StringBuffer buf = new StringBuffer();
        final EBICRMHistoryDataUtil util = list.get(EBISystem.gui().combo("historyText", "historyViewDialog").getSelectedIndex() - 1);
        final Iterator iter = util.getField().iterator();

        boolean trClose = false;
        buf.append("<table style=\"font-family: Verdana, serif;color:#fff;font-size: 10px; border: solid 1px #a0f0ff; width:100%\" border=\"0\">");

        while (iter.hasNext()) {

            final String str = (String) iter.next();
            if ("*EOR*".equals(str)) {
                buf.append("<tr><td><hr></td></tr>");
                trClose = true;
            } else {
                if (str.endsWith("$")) {
                    buf.append("<tr><td bgcolor=\"red\"><font color=\"#ffffff\">" + str.substring(0, str.length() - 1) + "</font></td></tr>");
                } else {
                    buf.append("<tr><td>" + str + "</td></tr>");
                }
            }
            if (trClose) {
                trClose = false;

            }
        }
        buf.append("</table>");
        EBISystem.gui().getEditor("viewhistoryText", "historyViewDialog").setText(buf.toString());
    }

}
