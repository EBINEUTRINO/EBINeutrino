package ebiNeutrino.core.settings;

import ebiNeutrino.core.EBIMain;
import ebiNeutrinoSDK.EBISystem;

import javax.swing.*;
import java.awt.*;

public class EBIListSettingName extends JPanel {

    public JList jListnames = null;
    public DefaultListModel myListmodel = null;
    public JPanel cpanel = null;
    public EBIMain ebiMain = null;
    public EBISystemSettingPanel einstp = null;
    public EBIReportSetting report = null;

    /**
     * This is the default constructor
     */
    public EBIListSettingName(final EBIMain main, final JPanel start) {
        super();
        ebiMain = main;
        cpanel = start;

        myListmodel = new DefaultListModel();
        final EBIListItem item0 = new EBIListItem(new ImageIcon(getClass().getClassLoader().getResource("start.png")), EBISystem.i18n("EBI_LANG_START")),
                item1 = new EBIListItem(new ImageIcon(getClass().getClassLoader().getResource("sys.png")), EBISystem.i18n("EBI_LANG_SYSTEM_SETTING")),
                item3 = new EBIListItem(new ImageIcon(getClass().getClassLoader().getResource("report.png")), EBISystem.i18n("EBI_LANG_REPORT_SETTING"));

        myListmodel.addElement(item0);
        myListmodel.addElement(item1);
        myListmodel.addElement(item3);

        initialize();
        jListnames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListnames.setSelectedIndex(0);

    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJListnames(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jListnames
     *
     * @return javax.swing.JList
     */
    private JList getJListnames() {
        if (jListnames == null) {
            jListnames = new JList(myListmodel);
            jListnames.setCellRenderer(new EBIListCellRenderer());
            jListnames.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(final java.awt.event.MouseEvent e) {
                    final int index = jListnames.locationToIndex(e.getPoint());

                    switch (index) {
                        case 0:
                            setStart();
                            break;
                        case 1:
                            cpanel.removeAll();
                            cpanel.updateUI();
                            einstp = new EBISystemSettingPanel(ebiMain);
                            cpanel.add(einstp, java.awt.BorderLayout.CENTER);
                            ebiMain.systemSetting.setPreferredSize(new Dimension(950, 700));
                            einstp.updateUI();
                            ebiMain.systemSetting.updateUI();
                            break;
                        case 2:
                            cpanel.removeAll();
                            cpanel.updateUI();
                            report = new EBIReportSetting(ebiMain);
                            cpanel.add(report, java.awt.BorderLayout.CENTER);
                            ebiMain.systemSetting.setPreferredSize(new Dimension(950, 700));
                            report.updateUI();
                            ebiMain.systemSetting.updateUI();
                            break;
                        default:
                            setStart();
                            break;
                    }
                }
            });
        }
        return jListnames;
    }

    public void setStart() {
        cpanel.removeAll();
        cpanel.updateUI();
        final EBISystemSettingStart start = new EBISystemSettingStart();
        cpanel.add(start, java.awt.BorderLayout.CENTER);
        start.updateUI();
        ebiMain.systemSetting.setPreferredSize(new Dimension(900, 500));
        ebiMain.systemSetting.updateUI();
    }
}
