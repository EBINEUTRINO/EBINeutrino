package ebiNeutrino.core;

import ebiCRM.EBICRMModule;
import ebiNeutrino.core.guiRenderer.EBIGUIRenderer;
import ebiNeutrino.core.gui.dialogs.EBISplashScreen;
import ebiNeutrino.core.gui.component.EBIExtensionContainer;
import ebiNeutrino.core.gui.component.EBILogin;
import ebiNeutrino.core.gui.component.EBIStatusBar;
import ebiNeutrino.core.gui.component.EBIToolbar;
import ebiNeutrino.core.gui.lookandfeel.MoodyBlueTheme;
import ebiNeutrino.core.module.management.EBIModuleManagement;
import ebiNeutrino.core.settings.EBISystemSetting;
import ebiNeutrino.core.settings.EBISystemSettingPanel;
import ebiNeutrino.core.user.management.EBIUserManagement;
import ebiNeutrinoSDK.EBISystem;
import ebiNeutrinoSDK.gui.dialogs.EBIDialogExt;
import ebiNeutrinoSDK.gui.dialogs.EBIExceptionDialog;
import ebiNeutrinoSDK.gui.dialogs.EBIMessage;
import ebiNeutrinoSDK.interfaces.IEBISecurity;
import ebiNeutrinoSDK.interfaces.IEBIToolBar;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * Description: This is the main class for EBI Neutrino
 *
 * JVM start point: public static void main(String[] args)
 */
public class EBIMain extends JFrame {

    public EBILogin login = null;
    public EBIUserManagement user_management = null;
    public static boolean canReleaseUser = false;
    public String appTitle = "EBI Neutrino R1 CRM / ERP Framework";
    public EBISystemSetting systemSetting = null;
    public static Logger logger = Logger.getLogger(EBIMain.class.getName());
    public EBIToolbar ebiBar = null;
    public EBIExtensionContainer container = null;
    protected static boolean showUpdateInfo = false;
    protected IEBISecurity iSecurity = null;
    protected IEBIToolBar bar = null;
    public EBISplashScreen splash = null;
    public EBIModuleManagement mng = null;
    public EBIDialogExt frameSetting = null;
    public EBIStatusBar stat = null;
    public JPanel panAllert = null;
    public JScrollPane pallert = null;
    private final JLabel stName = null;
    public int USER_DELETE_ID = -1;
    public EBIToolbar userSysBar = null;

    private EBICRMModule ebiModule = null;

    public static void main(final String[] args) throws Exception {
        try {
            final EBIMain application = new EBIMain();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ToolTipManager.sharedInstance().setInitialDelay(0);

                    try {
                        application.pack();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                    application.setSize(d.width, d.height);
                    application.setExtendedState(Frame.MAXIMIZED_BOTH);
                    application.addLoginModul();
                }
            });
            // Try to read the update xml file from a server for checking if a new version
            // is available
            /*
			 * EBISocketDownloader fileLoader = new EBISocketDownloader();
			 * fileLoader.SysPath = EBIPGFactory.updateServer; fileLoader.setConnection();
			 * 
			 * if(fileLoader.readConfig()){ // ONLINE UPDATE FUNCTIONALITY
			 * 
			 * final EBIDialogExt ext = new EBIDialogExt(null);
			 * ext.setName("SystemUpdateDialog"); ext.storeLocation(true);
			 * ext.storeSize(true);
			 * ext.setTitle(EBIPGFactory.getLANG("EBI_LANG_SYSTEM_UPDATE_DIALOG"));
			 * ext.setModal(true); ext.setSize(200,150); ext.setLayout(null);
			 * 
			 * JLabel title = new JLabel("<html><body><b>"+EBIPGFactory.getLANG(
			 * "EBI_LANG_UPDATE_FOR_SYSTEM_AVAILABLE")+"</b></body></html>");
			 * title.setBounds(170,10,260,30); ext.add(title,null);
			 * 
			 * JLabel body = new
			 * JLabel("<html><body>"+EBIPGFactory.getLANG("EBI_LANG_NEW_VERSION_AVAILABLE")+
			 * "" + "<br><br>" +
			 * ""+EBIPGFactory.getLANG("EBI_LANG_LOCAL_VERSION")+": "+fileLoader.localVer+
			 * "<br>" +
			 * ""+EBIPGFactory.getLANG("EBI_LANG_ONLINE_VERSION")+": "+fileLoader.onlineVer+
			 * "<br><br>" +
			 * ""+EBIPGFactory.getLANG("EBI_LANG_WOULD_YOU_UPDATE_YOUR_SYSTEM")+
			 * "</body></html>"); body.setBounds(170,20,260,180); ext.add(body,null);
			 * 
			 * 
			 * JLabel img = new JLabel(new ImageIcon("images/update.png"));
			 * img.setBounds(10,30, 120, 120); ext.add(img, null);
			 * 
			 * // Action Yes no JButton yes = new
			 * JButton(EBIPGFactory.getLANG("EBI_LANG_YES")); yes.setBounds(220,200,100,25);
			 * yes.addActionListener(new ActionListener(){ public void
			 * actionPerformed(ActionEvent e){ try {
			 * 
			 * if(EBIPGFactory.isWindows()){
			 * Runtime.getRuntime().exec("update/updateNeutrinoWindows.bat "+EBIPGFactory.
			 * updateServer); }else if(EBIPGFactory.isMac()){
			 * Runtime.getRuntime().exec("update/updateNeutrinoMAC.sh "+EBIPGFactory.
			 * updateServer); }else if(EBIPGFactory.isUnix()){
			 * Runtime.getRuntime().exec("update/updateNeutrinoLinux.sh "+EBIPGFactory.
			 * updateServer); }
			 * 
			 * } catch (IOException e1) { e1.printStackTrace(); } System.exit(0); } });
			 * ext.add(yes, null);
			 * 
			 * JButton no = new JButton(EBIPGFactory.getLANG("EBI_LANG_NO"));
			 * no.setBounds(325,200,100,25); no.addActionListener(new ActionListener(){
			 * public void actionPerformed(ActionEvent e){ ext.setVisible(false); } });
			 * ext.add(no, null); ext.setVisible(true);
			 * 
			 * }
             */
            // SwingUtilities.updateComponentTreeUI(application);
            // Load CRM module ebiCRM.jar

        } catch (final Exception exx) {
            exx.printStackTrace();
            logger.error(EBISystem.printStackTrace(exx));
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            EBIExceptionDialog.getInstance(EBISystem.printStackTrace(exx)).Show(EBIMessage.NEUTRINO_DEBUG_MESSAGE);
            System.exit(1);
        }

    }

    /**
     * EBIMain default constructor it initialize the system functionality
     */
    public EBIMain() {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            MetalLookAndFeel.setCurrentTheme(new MoodyBlueTheme());
            SwingUtilities.updateComponentTreeUI(this);

            splash = new EBISplashScreen();
            PropertyConfigurator.configure(ClassLoader.getSystemResource("ebiLogger.config"));
            splash.setVisible(true);

            final EBIDatabase conn = new EBIDatabase();
            EBISystem.getInstance().setIEBIDatabase(conn);

            new EBINeutrinoSystemInit(splash);

            if (EBINeutrinoSystemInit.isConfigured) {
                /**
                 * ******************
                 */
                // Initialize xml gui renderer
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                EBISystem.getInstance().setIEBIGUIRendererInstance(new EBIGUIRenderer(EBIMain.this));
                            }
                        });
                    }
                }).start();

                /**
                 * *****************
                 */
                initialize();
                // Initialize Container, tab panel
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                container = new EBIExtensionContainer(EBIMain.this);
                                container.initContainer();
                                EBISystem.getInstance().setIEBIContainerInstance(container);
                            }
                        });
                    }
                }).start();
                /**
                 * ******************
                 */
                // Create toolbars
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                ebiBar = new EBIToolbar(EBIMain.this, true);
                                ebiBar.addToolBarToEBIMain();
                                EBISystem.getInstance().setIEBIToolBarInstance(ebiBar);

                                iSecurity = EBISystem.getInstance().getIEBISecurityInstance();
                                bar = EBISystem.getInstance().getIEBIToolBarInstance();
                            }
                        });
                    }
                }).start();


                ebiModule = new EBICRMModule();
                EBISystem.getInstance().setIEBIModule(ebiModule);
                mng = new EBIModuleManagement(EBIMain.this, ebiModule);

                /**
                 * ******************
                 */
                // Initialize report system
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                final EBIReportSystem reportSystem = new EBIReportSystem();
                                EBISystem.getInstance().setIEBIReportSystemInstance(reportSystem);
                            }
                        });
                    }
                }).start();
            }

            // CREATE TASKBAR
            stat = new EBIStatusBar();
            stat.setSystemVersion(EBIVersion.getInstance().getVersion());
            stat.setSystemHost(EBISystem.host);
            stat.setSystemDatabaseText(EBISystem.DATABASE_SYSTEM);
            pallert = new JScrollPane();
            panAllert = new JPanel();
            panAllert.setVisible(false);
            pallert.setVisible(false);
            pallert.setViewportView(panAllert);
            stat.addAllert(pallert);
            getContentPane().add(stat, BorderLayout.SOUTH);
            EBISystem.getInstance().addMainFrame(this);

        } catch (final Exception ex) {
            ex.printStackTrace();
            EBISystem.getInstance().message.debug(EBISystem.printStackTrace(ex));
            logger.error("Exception", ex.fillInStackTrace());
        }
    }

    private void initialize() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setTitle(appTitle);
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent winEvt) {
                try {
                    if (iSecurity.checkCanReleaseModules()) {
                        if (EBIExceptionDialog.getInstance(EBISystem.i18n("EBI_LANG_MESSAGE_CLOSE"))
                                .Show(EBIMessage.INFO_MESSAGE_YESNO) == true) {
                            if (mng != null) {
                                mng.onExit();
                            }
                            System.exit(0);
                        }
                    }
                } catch (final Exception ex) {
                    logger.error("Exception", ex.fillInStackTrace());
                    ex.printStackTrace();
                }
            }
        });
    }

    public void showBusinessModule() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mng.showModule();
            }
        }).start();
    }

    public void addLoginModul() {
        login = new EBILogin(this);
        if (this.getWindowListeners() != null && this.getWindowListeners().length > 0) {
            login.addWindowListener(this.getWindowListeners()[0]);
        }
        final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        login.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        login.setName("EBILoginDialog");
        login.setResizable(false);
        login.setSize(524, 257);
        login.setLocation(((int) d.getWidth() - login.getWidth()) / 2, ((int) d.getHeight() - login.getHeight()) / 2);
        //hide splash if login dialog is visible
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (splash != null) {
                    splash.setVisible(false);
                }
            }
        });
        login.setVisible(true);
    }

    public void addSystemSetting(final int selectedList) {
        frameSetting = new EBIDialogExt(this);
        frameSetting.getContentPane().setLayout(new BorderLayout(0, 0));
        systemSetting = new EBISystemSetting(this);
        systemSetting.listName.setStart();
        final EBIToolbar sysBar = new EBIToolbar(this, false);

        final int NEW_ID = sysBar.addToolButton(new ImageIcon(getClass().getClassLoader().getResource("new.png")), new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                if (EBISystemSetting.selectedModule == -1) {
                    addSystemSetting(selectedList);
                } else {
                    if (EBISystemSetting.selectedModule == 4) {
                        try {
                            systemSetting.listName.report.newReport();
                        } catch (final Exception ex) {
                            logger.error("Exception", ex.fillInStackTrace());
                            ex.printStackTrace();
                        }
                    }
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        ((JButton) sysBar.getToolbarComponent(NEW_ID)).setMnemonic(KeyEvent.VK_N);
        sysBar.setComponentToolTipp(NEW_ID, EBISystem.i18n("EBI_LANG_T_LOAD_ALL_SETTING"));

        final int SAVE_ID = sysBar.addToolButton(new ImageIcon(getClass().getClassLoader().getResource("save.png")), new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                systemSetting.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                if (EBISystemSetting.selectedModule == 2) {

                    systemSetting.listName.einstp.saveSystemSetting();
                    mng.reloadSelectedModule();
                    frameSetting.setVisible(false);

                    addSystemSetting(1);
                    frameSetting.requestFocus();

                } else if (EBISystemSetting.selectedModule == 3) {
                    systemSetting.listName.report.saveReport();
                }
                systemSetting.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        ((JButton) sysBar.getToolbarComponent(SAVE_ID)).setMnemonic(KeyEvent.VK_S);
        sysBar.setComponentToolTipp(SAVE_ID, EBISystem.i18n("EBI_LANG_T_SAVE_SETTING"));

        final int DELETE_ID = sysBar.addToolButton(new ImageIcon(getClass().getClassLoader().getResource("delete.png")), new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                setCursor(new Cursor(Cursor.WAIT_CURSOR));

                if (EBISystemSetting.selectedModule == 4) {
                    systemSetting.listName.report.deleteReport();
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        ((JButton) sysBar.getToolbarComponent(DELETE_ID)).setMnemonic(KeyEvent.VK_D);
        sysBar.setComponentToolTipp(DELETE_ID, EBISystem.i18n("EBI_LANG_T_DELETE_SETTING"));
        sysBar.showToolBar(false);

        systemSetting.setChangePropertiesVisible(false);
        systemSetting.getPanel().add(sysBar.getJToolBar(), BorderLayout.NORTH);

        if (selectedList != -1) {
            systemSetting.listName.jListnames.setSelectedIndex(selectedList);
            systemSetting.listName.cpanel.removeAll();
            systemSetting.listName.einstp = new EBISystemSettingPanel(this);
            systemSetting.listName.cpanel.add(systemSetting.listName.einstp, java.awt.BorderLayout.CENTER);
        }

        frameSetting.setName("SystemSetting");
        frameSetting.storeLocation(true);
        frameSetting.storeSize(true);
        frameSetting.setSize(new Dimension(1100, 600));
        frameSetting.setResizable(true);
        systemSetting.setModuleTitle(EBISystem.i18n("EBI_LANG_SYSTEM_SETTING"));
        systemSetting.setModuleIcon(new ImageIcon("images/folder_config64.png"));

        JScrollPane scrollPane = new JScrollPane(systemSetting);
        scrollPane.setPreferredSize(new Dimension(1100, 600));
        scrollPane.setSize(new Dimension(1100, 600));
        scrollPane.getViewport().setPreferredSize(new Dimension(1100, 600));
        scrollPane.getViewport().setSize(new Dimension(1100, 600));

        frameSetting.getContentPane().add(scrollPane, BorderLayout.CENTER);

        if (frameSetting != null && !frameSetting.isVisible()) {
            frameSetting.setVisible(true);
        }
    }

    public void addUsermanagement() {
        user_management = new EBIUserManagement(this);
        userSysBar = new EBIToolbar(this, false);
        final int NEW_ID = userSysBar.addToolButton(new ImageIcon(getClass().getClassLoader().getResource("new.png")), new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                user_management.resetFields();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        ((JButton) userSysBar.getToolbarComponent(NEW_ID)).setMnemonic(KeyEvent.VK_N);
        userSysBar.setComponentToolTipp(NEW_ID, EBISystem.i18n("EBI_LANG_T_NEW_USER"));

        final int SAVE_ID = userSysBar.addToolButton(new ImageIcon(getClass().getClassLoader().getResource("save.png")), new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                if (user_management.isSaveOrUpdate == false) {
                    user_management.saveUser();
                    canReleaseUser = true;
                } else if (user_management.isSaveOrUpdate == true) {
                    user_management.updateUser();
                    canReleaseUser = true;
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        ((JButton) userSysBar.getToolbarComponent(SAVE_ID)).setMnemonic(KeyEvent.VK_S);
        userSysBar.setComponentToolTipp(SAVE_ID, EBISystem.i18n("EBI_LANG_T_SAVE_USER"));

        USER_DELETE_ID = userSysBar.addToolButton(new ImageIcon(getClass().getClassLoader().getResource("delete.png")), new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                user_management.userDelete();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        ((JButton) userSysBar.getToolbarComponent(USER_DELETE_ID)).setMnemonic(KeyEvent.VK_D);
        userSysBar.setComponentToolTipp(USER_DELETE_ID, EBISystem.i18n("EBI_LANG_T_DELETE_USER"));
        userSysBar.setComponentToolBarEnabled(USER_DELETE_ID, false);
        userSysBar.showToolBar(false);

        user_management.DELETE_BUTTON_ID = USER_DELETE_ID;
        user_management.bar = userSysBar;
        user_management.getPanel().add(userSysBar, BorderLayout.NORTH);
        user_management.setChangePropertiesVisible(false);

        frameSetting = new EBIDialogExt(this);
        frameSetting.setTitle(EBISystem.i18n("EBI_LANG_USER_SETTING"));
        frameSetting.getContentPane().setLayout(new BorderLayout(0, 0));
        frameSetting.setName("Usermanagement");
        frameSetting.setModal(true);
        frameSetting.storeLocation(true);
        frameSetting.storeSize(true);
        frameSetting.setSize(new Dimension(1100, 600));
        frameSetting.setResizable(true);
        user_management.setPreferredSize(new Dimension(1000, 500));
        JScrollPane scrollPane = new JScrollPane(user_management);
        scrollPane.setPreferredSize(new Dimension(1100, 600));
        scrollPane.setSize(new Dimension(1100, 600));
        scrollPane.getViewport().setPreferredSize(new Dimension(1100, 600));
        scrollPane.getViewport().setSize(new Dimension(1100, 600));

        frameSetting.getContentPane().add(scrollPane, BorderLayout.CENTER);

        if (frameSetting != null && !frameSetting.isVisible()) {
            frameSetting.setVisible(true);
        }
    }

    public Object getActiveModule() {
        return mng.getActiveModule();
    }

}
