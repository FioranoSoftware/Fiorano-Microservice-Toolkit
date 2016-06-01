/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.runtime.swing;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.feeder.FeederService;
import com.fiorano.services.feeder.engine.FeederMessage;
import com.fiorano.services.feeder.runtime.swing.panels.TifosiDocPanel;
import com.fiorano.swing.table.XTable;
import com.fiorano.uif.ui.ExceptionDisplayDialog;
import com.fiorano.uif.util.*;
import fiorano.esb.utils.RBUtil;

import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.logging.Level;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 */
public class FeederFrame extends JFrame {
    public int historySize = 10;
    TifosiDocPanel docPanel = null;
    private JPanel jPanel5 = new JPanel();
    private JLabel historyLabel = new JLabel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JButton removeAllButton = new JButton();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JSplitPane jSplitPane1 = new JSplitPane();
    private HistoryModel model = new HistoryModel(historySize);
    private JPanel jPanel1 = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel bottomPanel = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JPanel jPanel2 = new JPanel();
    private JButton sendButton = new JButton();
    private JButton clearButton = new JButton();

    private FeederService service = null;
    private FeederPM configuration;
    private TextMessage defaultDoc = null;
    private JPanel jPanel3 = new JPanel();
    private BorderLayout borderLayout3 = new BorderLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private TablePopup m_tblPopup = new TablePopup();

    private JTextField m_timesField = TextUtils.createPositiveIntegerTextField();
    private JTextField m_sleepField = TextUtils.createPositiveIntegerTextField();

    private int totalSentMessages = 0;
    private JButton defaultButton = new JButton();
    private JButton sendNButton = new JButton();

    private WaitDialog waitDlg = new WaitDialog(this);
    private FeederMessage feederMsg;
    private XTable historyTable = new XTable(model);
    private long inTimeMillis;

    // set look and feel for the frame
    static {
        try {
            LAFController.setLookAndFeel(null);
        }
        catch (Throwable ignore) {
            //ignore
        }
    }

    /**
     * @param service

     */
    public FeederFrame( FeederService service) {

        super(service.getLaunchConfiguration().getApplicationName() + "__" + service.getLaunchConfiguration().getApplicationVersion() + "__" + service.getLaunchConfiguration().getServiceInstanceName());
         this.service = service;

        this.configuration = (FeederPM)service.getConfiguration();
        docPanel = new TifosiDocPanel(service.getFeederEngine().getTransportManager().getFeederTransport().getSession(), service.getLogger());
        this.service = service;
        feederMsg = new FeederMessage(service.getFeederEngine());
        try {
            feederMsg.setMessage(configuration);
        } catch (ServiceExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.defaultDoc = feederMsg.getDefaultDoc();
        docPanel.setSchema(configuration.getSchema());
        docPanel.setEncryptDecryptConfig(configuration.getElementsToEncrypt());
        try {
            jbInit();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize((int) (screenSize.width * 0.65), (int) (screenSize.height * 0.55));

            int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 -
                    getWidth() / 2);
            int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() /
                    2 - getHeight() / 2);

            setLocation(x, y);

            if (service.getLaunchConfiguration().isInmemoryLaunchable()) {
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            } else {
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }

            model.setMaxSize(configuration.getHistorySize());
            TableColumn col = historyTable.getColumnModel().getColumn(0);
            col.setPreferredWidth(20);
            col.setMinWidth(20);
            col.setMaxWidth(50);
            col.setResizable(true);
            col = historyTable.getColumnModel().getColumn(1);
            col.setPreferredWidth(200);
            col.setMinWidth(200);
            col.setMaxWidth(200);
            historyTable.getColumnModel().getColumn(2).setCellRenderer(new MessageCellRenderer());


            setIconImage(Toolkit.getDefaultToolkit().createImage(FeederFrame.class.getResource("feeder.gif")));
            defaultButton.setEnabled(defaultDoc != null);
            if (defaultDoc != null) {
                docPanel.setDocument(defaultDoc);
            }
            docPanel.setPreferredSize(new Dimension(10, 10));

            setVisible(true);
        }
        catch (Exception e) {
            service.getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.EROOR_LOADING_FRAME), e);
        }
    }


    /**
     * Overridden so that Feeder Service can exit when window is closed
     *
     * @param e Window event
     */
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            try {
                service.getLauncher().terminate();
            }
            catch (Throwable thr) {
                JOptionPane.showMessageDialog(this, "HELP_LAUNCH_ERROR :: could not exit from chat", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        super.processWindowEvent(e);
    }


    void sendButton_actionPerformed(ActionEvent e) {
        try {
            sendDocument(docPanel.getDocument());
        } catch (Exception ex) {
            ExceptionDisplayDialog.showException(this, ex);
            service.getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class,Bundle.EXCEP_SENDING_MESSAGE), ex);
        }
    }

    void clearButton_actionPerformed(ActionEvent e) {
        docPanel.clear();
    }

    void table_mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (historyTable.getRowCount() > 0) {
                if (historyTable.getSelectedRow() != -1) {
                    int row = historyTable.getSelectedRow();

                    Rectangle rect = historyTable.getCellRect(row, 0, true);

                    historyTable.scrollRectToVisible(rect);

                    m_tblPopup.show(historyTable, e.getX(), e.getY());
                }
            }
        } else {
            try {
                if (e.getClickCount() < 2) {
                    return;
                }
                int row = historyTable.rowAtPoint(e.getPoint());

                if (row >= 0) {
                    docPanel.setDocument(model.getDocument(row));
                }
            }
            catch (Exception ex) {
                ExceptionDisplayDialog.showException(this, ex);
            }
        }
    }

    void defaultButton_actionPerformed(ActionEvent e) {
        try {
            docPanel.setDocument(defaultDoc);
        }
        catch (Exception ex) {
            ExceptionDisplayDialog.showException(this, ex);
        }
    }

    void sendNButton_actionPerformed(ActionEvent e) {

        while (true) {
            int result = JOptionPane.showOptionDialog(this,
                    new Object[]
                            {
                                    "No of times message to be sent:", m_timesField,
                                    "Interval beween successive sends in millisec:",
                                    m_sleepField
                            }
                    ,
                    "Information",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, null, null);

            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            int times = 0;

            try {
                times = Integer.parseInt(m_timesField.getText());
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid value for 'No of times'",
                        "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (times <= 0) {
                JOptionPane.showMessageDialog(this,
                        "No of times should be greater than zero.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            int sleep = 0;

            try {
                sleep = Integer.parseInt(m_sleepField.getText());
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid value for 'Time interval'",
                        "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (sleep < 0) {
                JOptionPane.showMessageDialog(this,
                        "Time interval should be greater than or equal zero.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            final int count = times;
            final int sleepTime = sleep;

            new WaitThread() {
                public Object runTask()
                        throws Exception {
                    for (int i = 0; i < count; i++) {
                        sendDocument(docPanel.getDocument());
                        if (isInterrupted()) {
                            return null;
                        }
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    }
                    return null;
                }

                public void updateGUI(Object object) {
                }

                public void exceptionOccured(Exception ex) {
                    service.getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class,Bundle.EXCEP_SENDING_MESSAGE));
                }
            }
                    .start(waitDlg, "Sending Messages", true);

            break;
        }
    }

    private void jbInit() throws Exception {
        Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        Border border3 = BorderFactory.createEmptyBorder(0, 5, 5, 5);
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setBorder(border1);
        jPanel1.setLayout(borderLayout1);
        bottomPanel.setLayout(borderLayout2);
        jPanel2.setLayout(gridBagLayout2);
        sendButton.setMaximumSize(new Dimension(143, 20));
        sendButton.setMinimumSize(new Dimension(143, 20));
        sendButton.setPreferredSize(new Dimension(143, 20));
        sendButton.setMargin(new Insets(2, 5, 2, 5));
        sendButton.setText("Send");
        sendButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sendButton_actionPerformed(e);
                    }
                });
        clearButton.setMaximumSize(new Dimension(143, 20));
        clearButton.setMinimumSize(new Dimension(143, 20));
        clearButton.setPreferredSize(new Dimension(143, 20));
        clearButton.setMargin(new Insets(2, 5, 2, 5));
        clearButton.setText("Clear");
        clearButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        clearButton_actionPerformed(e);
                    }
                });
        borderLayout1.setVgap(5);
        jPanel1.setBorder(border3);
        jPanel3.setLayout(borderLayout3);
        jScrollPane1.setBorder(border1);
        historyTable.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        table_mouseClicked(e);
                    }
                });
        defaultButton.setMaximumSize(new Dimension(143, 20));
        defaultButton.setMinimumSize(new Dimension(143, 20));
        defaultButton.setPreferredSize(new Dimension(143, 20));
        defaultButton.setMargin(new Insets(2, 5, 2, 5));
        defaultButton.setText("Load Default Message");
        defaultButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        defaultButton_actionPerformed(e);
                    }
                });
        sendNButton.setMaximumSize(new Dimension(143, 20));
        sendNButton.setMinimumSize(new Dimension(143, 20));
        sendNButton.setPreferredSize(new Dimension(143, 20));
        sendNButton.setMargin(new Insets(2, 5, 2, 5));
        sendNButton.setText("Send-N times");
        sendNButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sendNButton_actionPerformed(e);
                    }
                });
        historyLabel.setMaximumSize(new Dimension(306, 20));
        historyLabel.setMinimumSize(new Dimension(306, 20));
        historyLabel.setPreferredSize(new Dimension(306, 24));
        historyLabel.setText(
                "History( last 0 of 0):    (Double Click a row to load the message)");
        jPanel5.setLayout(gridBagLayout1);
        removeAllButton.setMaximumSize(new Dimension(73, 18));
        removeAllButton.setMinimumSize(new Dimension(73, 18));
        removeAllButton.setPreferredSize(new Dimension(73, 18));
        removeAllButton.setMargin(new Insets(2, 5, 2, 5));
        removeAllButton.setText("remove all");
        removeAllButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        (new TablePopup()).actionPerformed(new ActionEvent(removeAllButton, 0,
                                TablePopup.removeall_command));
                    }
                });
        jPanel2.setMinimumSize(new Dimension(570, 22));
        jPanel2.setPreferredSize(new Dimension(570, 22));
        this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
        jSplitPane1.add(jPanel1, JSplitPane.BOTTOM);
        jPanel1.add(docPanel, BorderLayout.CENTER);
        jPanel1.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(jPanel2, BorderLayout.SOUTH);
        jPanel2.add(defaultButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));
        jPanel2.add(sendButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));
        jPanel2.add(sendNButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));
        jPanel2.add(clearButton, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        jSplitPane1.add(jPanel3, JSplitPane.TOP);
        jPanel3.add(jScrollPane1, BorderLayout.CENTER);
        jPanel3.add(jPanel5, BorderLayout.NORTH);
        jPanel5.add(historyLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));
        jPanel5.add(removeAllButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        jScrollPane1.setViewportView(historyTable);
        jSplitPane1.setDividerLocation(100);
        jPanel1.setMinimumSize(new Dimension(50, 50));
        jPanel3.setMinimumSize(new Dimension(50, 50));
        m_timesField.setText("10");
        m_sleepField.setText("100");
    }

    private void sendDocument(TextMessage doc) throws Exception {
        inTimeMillis = System.currentTimeMillis();
        doc.setStringProperty(Constants.COMPONENT_IN_TIME, String.valueOf(inTimeMillis));
        doc.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTimeMillis));
        feederMsg.sendMessage(doc);
        totalSentMessages++;
        model.addRow(new Object[]{"", new Date(), doc});
        historyLabel.setText("History( last " + model.getRowCount() + " of " +
                totalSentMessages +
                "):    (Double Click a row to load the message)");

    }

    /**
     * PopupMenu items
     *
     * @author Madhav Vodnala.
     * @version 2.0
     * @created October 26, 2002
     */
    class TablePopup extends UIFPopupMenu implements ActionListener {
        public final static String remove_command = "Remove";

        /**
         * Description of the Field
         */
        public final static String removeall_command = "Remove All";

        /**
         * Constructor for the TablePanelPopup object
         */
        TablePopup() {
            add(remove_command);
            add(removeall_command);

            for (int i = 0; i < getComponentCount(); i++) {
                if (!(getComponent(i) instanceof JSeparator)) {
                    ((JMenuItem) getComponent(i)).addActionListener(this);
                }
            }
        }

        /**
         * action performed event
         *
         * @param a ActionEvent
         */
        public void actionPerformed(ActionEvent a) {
            String actionCommand = a.getActionCommand();
            int row = 0;

            if (actionCommand.equalsIgnoreCase(remove_command)) {
                //remove message from the frame
                if (historyTable.getSelectedRow() != -1) {
                    row = historyTable.getSelectedRow();

                    int[] selectedRows = historyTable.getSelectedRows();

                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        model.removeRow(selectedRows[i]);
                    }
                }
                if ((row - 1) >= 0) {
                    historyTable.setRowSelectionInterval(row - 1, row - 1);
                } else if (historyTable.getRowCount() > 0) {
                    historyTable.setRowSelectionInterval(0, 0);
                }
            } else if (actionCommand.equalsIgnoreCase(removeall_command)) {
                //remove all messages from the frame
                int count = model.getRowCount();
                for (int i = (count - 1); i >= 0; i--) {
                    model.removeRow(i);
                }
            }
            historyLabel.setText("History( last " + model.getRowCount() + " of " +
                    totalSentMessages +
                    "):    (Select a row to load the message)");
        }
    }

}