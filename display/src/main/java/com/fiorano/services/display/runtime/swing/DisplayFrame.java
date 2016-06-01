/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing;


import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.services.display.DisplayService;
import com.fiorano.services.display.runtime.swing.panels.JMSDocPanel;
import com.fiorano.swing.table.XTable;
import com.fiorano.uif.util.LAFController;
import com.fiorano.uif.util.UIFPopupMenu;
import fiorano.esb.utils.RBUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.logging.Level;

/**
 * The display runtime UI
 *
 * @author FSIPL
 * @version 1.0
 * @created April 15, 2005
 */
public class DisplayFrame extends JFrame {

    private int historySize;
    private JMSDocPanel docPanel;
    private JButton m_removeAllButton = new JButton();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JSplitPane jSplitPane1 = new JSplitPane();
    private HistoryModel model = new HistoryModel();
    private Border border1;
    private JPanel jPanel1 = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private Border border3;

    private DisplayService service = null;
    private JPanel jPanel3 = new JPanel();
    private BorderLayout borderLayout3 = new BorderLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JTable table = new XTable(model);
    private TablePopup tblPopup = new TablePopup();

    private int totalRecvdMessages = 0;
    private JPanel jPanel2 = new JPanel();
    private JLabel historyLabel = new JLabel();
    private JCheckBox selectCB = new JCheckBox();

    //set look and feel for the frame

    static {
        try {
            LAFController.setLookAndFeel(null);
        }
        catch (Throwable ignore) {
        }
    }

    /**
     * @param service
     */

    public DisplayFrame(final DisplayService service) {
        super(service.getLaunchConfiguration().getApplicationName() + "__" + service.getLaunchConfiguration().getApplicationVersion() + "__" + service.getLaunchConfiguration().getServiceInstanceName());
        this.service = service;
        this.historySize = ((ConfigurationPM)service.getConfiguration()).getMaxBufferedMessages();
        try {
            jbInit();
            table.addMouseListener(
                    new java.awt.event.MouseAdapter() {

                        public void mouseReleased(MouseEvent e) {
                            m_messageTable_mouseReleased(e);
                        }
                    });

            setSize(680, 400);

            int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() /
                    2 -
                    getWidth() / 2);
            int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().
                    getHeight() /
                    2 - getHeight() / 2);

            setLocation(x, y);

            if (service.getLaunchConfiguration().isInmemoryLaunchable()) {
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            } else {
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }

            table.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {
                        Message documentDisplayed = null;

                        public void valueChanged(ListSelectionEvent e) {
                            int row = table.getSelectedRow();
                            Rectangle cellRect = table.getCellRect(row, table.getSelectedColumn(), false);

                            if (cellRect != null) {
                                table.scrollRectToVisible(cellRect);
                            }
                            if (row < 0) {
                                return;
                            }
                            if (model.getDocument(row) != documentDisplayed) {
                                docPanel.loadDocPanel(model.getDocument(row));
                                documentDisplayed = model.getDocument(row);
                            }
                        }
                    });

            TableColumn col = table.getColumnModel().getColumn(0);

            col.setPreferredWidth(20);
            col.setMinWidth(20);
            col.setMaxWidth(50);
            col = table.getColumnModel().getColumn(1);
            col.setPreferredWidth(200);
            col.setMinWidth(200);
            col.setMaxWidth(200);
            table.getColumnModel().getColumn(2).setCellRenderer(
                    new
                            DefaultTableCellRenderer() {
                                protected void setValue(Object value) {
                                    String str = (value == null) ? "" : value.toString().trim();

                                    if (value instanceof TextMessage) {
                                        TextMessage tifOb = (TextMessage) value;

                                        try {
                                            String text = tifOb.getText();
                                            str = text != null ? text.trim() : "";
                                        }
                                        catch (JMSException ex) {

                                            service.getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class,
                                                    Bundle.INGNORING_JMSE, new Object[]{ex.getLocalizedMessage()}), ex);
                                        }
                                    }
                                    if (str.length() > 6 &&
                                            str.substring(0, 6).equalsIgnoreCase("<html>")) {
                                        str = str.substring(6);
                                    }
                                    setText(str);
                                }
                            });

            setIconImage(Toolkit.getDefaultToolkit().createImage(DisplayFrame.class.getResource("DisplayIcon.gif")));
            docPanel.setPreferredSize(new Dimension(10, 10));
            show();
        }
        catch (Exception e) {
            service.getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_FRAME), e);
        }
    }

    /**
     * Adds a feature to the Document attribute of the DisplayFrame object
     *
     * @param doc The feature to be added to the Document attribute
     */
    public void addDocument(Message doc) {
        totalRecvdMessages++;
        model.addRow(new Object[]{"", new Date(), doc});
        if (historySize != -1 && model.getRowCount() > historySize) {
            model.removeRow(0);
        }
        historyLabel.setText("History( last " + model.getRowCount() + " of " + totalRecvdMessages +
                "):    (Select a row to load the message)");

        if (selectCB.isSelected()) {
            table.setRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
        } else if (model.getRowCount() == 1) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    /**
     * Overridden so that Display Service can exit when window is closed
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

    /**
     * handles mouse event
     *
     * @param e MouseEvent
     */
    void m_messageTable_mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (table.getRowCount() > 0) {
                if (table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();

                    Rectangle rect = table.getCellRect(row, 0, true);

                    table.scrollRectToVisible(rect);

                    tblPopup.show(table, e.getX(), e.getY());
                }
            }
        }
    }

    void m_removeAllButton_actionPerformed(ActionEvent e) {
        (new TablePopup()).actionPerformed(new ActionEvent(m_removeAllButton, 0, TablePopup.removeall_command));
    }

    private void jbInit()
            throws Exception {
        border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        border3 = BorderFactory.createEmptyBorder(0, 5, 5, 5);
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setBorder(border1);
        jPanel1.setLayout(borderLayout1);
        borderLayout1.setVgap(5);
        jPanel1.setBorder(border3);
        jPanel3.setLayout(borderLayout3);
        jScrollPane1.setBorder(border1);
        historyLabel.setText("History( last 0 of 0):    (Select a row to view the message)");
        jPanel2.setLayout(gridBagLayout1);
        selectCB.setText("Select message when received");
        m_removeAllButton.setMaximumSize(new Dimension(80, 17));
        m_removeAllButton.setMinimumSize(new Dimension(80, 17));
        m_removeAllButton.setPreferredSize(new Dimension(80, 17));
        m_removeAllButton.setMargin(new Insets(2, 5, 2, 5));
        m_removeAllButton.setText("remove all");
        m_removeAllButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        m_removeAllButton_actionPerformed(e);
                    }
                });
        docPanel = new JMSDocPanel(service.getLogger());
        docPanel.setMinimumSize(new Dimension(562, 400));
        docPanel.setPreferredSize(new Dimension(562, 400));
        this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
        jSplitPane1.add(jPanel1, JSplitPane.BOTTOM);
        jPanel1.add(docPanel, BorderLayout.CENTER);
        jSplitPane1.add(jPanel3, JSplitPane.TOP);
        jPanel3.add(jScrollPane1, BorderLayout.CENTER);
        jPanel3.add(jPanel2, BorderLayout.NORTH);
        jPanel2.add(historyLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 0), 0, 0));
        jPanel2.add(selectCB, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        jPanel2.add(m_removeAllButton,
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
                        , GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 5), 0, 0));
        jScrollPane1.setViewportView(table);
        jSplitPane1.setDividerLocation(100);
        table.setAutoscrolls(true);
        jPanel1.setMinimumSize(new Dimension(50, 50));
        jPanel3.setMinimumSize(new Dimension(50, 50));
    }

    /**
     * <p><strong> </strong> represents </p>
     *
     * @author FSIPL
     * @version 1.0
     * @created April 15, 2005
     */
    class HistoryModel extends DefaultTableModel {
        public HistoryModel() {
            super(new String[]{" # ", "Received                      ", "Message"}, 0);
        }

        /**
         * Returns value at for object
         */
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return row + 1 + "";
                case 1:
                    return super.getValueAt(row, col);
                case 2:
                    try {
                        return (super.getValueAt(row, col));
                    }
                    catch (Exception ex) {

                        return "Exception occured " + ex.getMessage();
                    }
            }
            return null;
        }

        /**
         * Returns cell editable for object
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        /**
         * Returns document for object
         * @param row
         * @return
         */
        public Message getDocument(int row) {
            return (Message) super.getValueAt(row, 2);
        }

    }

    /**
     * PopupMenu items
     *
     * @author Madhav Vodnala.
     * @version 2.0
     * @created October 26, 2002
     */
    class TablePopup
            extends UIFPopupMenu
            implements ActionListener {
        public final static String remove_command = "Remove";
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
            int selectedRows[];

            if (actionCommand.equalsIgnoreCase(remove_command)) {
                //remove message from the frame
                if (table.getSelectedRow() != -1) {
                    row = table.getSelectedRow();
                    selectedRows = table.getSelectedRows();
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        model.removeRow(selectedRows[i]);
                    }
                }

                try {
                    if (row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                    } else if (row - 1 < table.getRowCount()) {
                        table.setRowSelectionInterval(row - 1, row - 1);
                    } else if (table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    }
                }
                catch (Throwable thr) {
                }

                if (table.getSelectedRow() < 0) {
                    docPanel.clear();
                }
            } else if (actionCommand.equalsIgnoreCase(removeall_command)) {
                //remove all messages from the frame
                try {
                    model.setRowCount(0);
                    docPanel.clear();

                }
                catch (Throwable ignore) {
                }
            }
            historyLabel.setText("History( last " + model.getRowCount() + " of " + totalRecvdMessages +
                    "):    (Select a row to load the message)");
        }
    }

    public void close() {
        setVisible(false);
    }
}