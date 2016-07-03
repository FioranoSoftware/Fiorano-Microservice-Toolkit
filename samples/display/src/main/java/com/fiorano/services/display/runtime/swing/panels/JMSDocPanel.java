/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;

import com.fiorano.services.display.runtime.swing.CustomTableModel;
import com.fiorano.uif.filechooser.TifosiFileChooser;
import com.fiorano.uif.filechooser.TifosiFileFilter;
import com.fiorano.uif.images.ImageReference;
import com.fiorano.uif.ui.ExceptionDisplayDialog;
import com.fiorano.uif.ui.TifosiImage;
import com.fiorano.uif.ui.TifosiTable;
import com.fiorano.uif.util.LAFController;
import com.fiorano.uif.util.TextEditor;
import com.fiorano.uif.xml.util.XMLTreeTable;
import fiorano.esb.util.CarryForwardContext;
import fiorano.esb.util.MessagePropertyNames;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.utils.RBUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Panel showing the JMS message
 *
 * @author FSIPL
 * @version 1.0
 * @created April 15, 2005
 */
public class JMSDocPanel extends JPanel {
    ImageIcon textIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "textView.gif"));
    ImageIcon treeIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "treeView.gif"));


    private BorderLayout borderLayout1 = new BorderLayout();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JPanel bodyPanel1 = new JPanel();
    private BorderLayout borderLayout9 = new BorderLayout();
    private JPanel cardPanel1 = new JPanel();
    private JPanel jPanel1_1 = new JPanel();
    private CardLayout cardLayout1_1 = new CardLayout();
    private JTabbedPane viewTab1 = new JTabbedPane(JTabbedPane.BOTTOM);
    private JPanel jPanel8 = new JPanel();
    private BorderLayout borderLayout10 = new BorderLayout();
    private JPanel jPanel1 = new JPanel();
    private Border border1;
    private CustomTableModel hmodel = new CustomTableModel("Name", "Value");
    private Border border2;
    private Border border3;
    private CustomTableModel amodel = new CustomTableModel("Name", "Size");

    private Border border4;
    private Border border5;
    private JPanel jPanel6 = new JPanel();
    private GridLayout gridLayout3 = new GridLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private BorderLayout borderLayout4 = new BorderLayout();
    private JTable hTable = new TifosiTable(hmodel);
    private JPanel headerPanel = new JPanel();
    private JButton saveAttachmentButton = new JButton();
    private JScrollPane jScrollPane2 = new JScrollPane();
    private BorderLayout borderLayout7 = new BorderLayout();
    private BorderLayout borderLayout6 = new BorderLayout();
    private JTable aTable = new TifosiTable(amodel);
    private GridLayout gridLayout2 = new GridLayout();
    private JPanel attachmentsPanel = new JPanel();
    private JPanel jPanel5 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private Border border7;
    private Border border8;
    private TextEditor textEditor1 = new TextEditor(TextEditor.XML);
    private XMLTreeTable tree1 = new XMLTreeTable();
    private GridLayout gridLayout1 = new GridLayout();
    private GridLayout gridLayout1_1 = new GridLayout();
    private JMSDocBody bodyPanel;
    private JPanel jpanel = new JPanel();
    private Logger logger;

    /**
     */
    public JMSDocPanel(Logger logger) {
        this.logger = logger;
        try {
            jbInit();
            textEditor1.setEditable(false);
            aTable.getColumn("Size").setCellRenderer(
                    new DefaultTableCellRenderer() {
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                                                       boolean isSelected, boolean hasFocus,
                                                                       int row, int column) {
                            float size = ((byte[]) value).length;

                            if (size != 0) {
                                size = size / 1024;
                            }

                            String name;

                            if (size >= 1) {
                                name = size + " KB";
                            } else {
                                name = ((byte[]) value).length + " bytes";
                            }
                            return super.getTableCellRendererComponent(table, name, isSelected,
                                    hasFocus, row, column);
                        }
                    });


            viewTab1.setIconAt(0, textIcon);
            viewTab1.setIconAt(1, treeIcon);
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_DOC_PANEL), ex);
        }
    }

    /**
     * The main program for the JMSDocPanel class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) throws Exception {
        LAFController.setLookAndFeel(null);

        JDialog dlg = new JDialog();

        dlg.setContentPane(new JMSDocPanel(null));
        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.show();
    }

    /**
     * Sets document for object
     */


    /**
     */
    public void clear() {
        textEditor1.setText("");
        hmodel.setRowCount(0);
        amodel.setRowCount(0);
        bodyPanel.clear();
        viewTab_stateChanged(null);
    }

    void jbInit() throws Exception {
        border1 = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        border2 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        border3 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        border4 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        border5 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        border7 = BorderFactory.createEmptyBorder(5, 0, 0, 0);
        border8 = BorderFactory.createEmptyBorder(10, 1, 1, 1);
        this.setLayout(borderLayout1);
        bodyPanel1.setLayout(borderLayout9);
        jPanel1_1.setLayout(gridLayout1_1);
        jPanel1.setLayout(gridLayout1);
        bodyPanel = new JMSDocBody(logger);


        jpanel.setLayout(new GridLayout(2, 1));


        jPanel1.setBorder(border1);
        jPanel1_1.setBorder(border8);
        cardPanel1.setLayout(cardLayout1_1);


        viewTab1.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        viewTab_stateChanged(e);
                    }
                });
        jPanel6.setLayout(gridLayout3);
        gridLayout3.setColumns(2);
        jScrollPane1.setBorder(border5);
        borderLayout4.setHgap(5);
        headerPanel.setLayout(borderLayout4);
        headerPanel.setBorder(border2);
        saveAttachmentButton.setText("Save to file");
        saveAttachmentButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveAttachmentButton_actionPerformed(e);
                    }
                });
        jScrollPane2.setBorder(border4);
        borderLayout6.setHgap(5);
        attachmentsPanel.setLayout(borderLayout6);
        attachmentsPanel.setBorder(border3);
        jPanel5.setLayout(gridLayout2);
        jPanel4.setLayout(borderLayout7);
        gridLayout2.setHgap(5);
        jLabel1.setText(" Header Properties:");
        jLabel2.setText(" Attachments:");
        jPanel4.setBorder(border7);
        jPanel8.setLayout(borderLayout10);

        this.add(jTabbedPane1, BorderLayout.CENTER);
        jTabbedPane1.add(bodyPanel, "Body");
        jTabbedPane1.add(jPanel6, "Header & Attachments");
        jTabbedPane1.add(bodyPanel1, "Application Context");
        jPanel6.add(headerPanel, null);
        headerPanel.add(jLabel1, BorderLayout.NORTH);
        headerPanel.add(jScrollPane1, BorderLayout.CENTER);
        jPanel6.add(attachmentsPanel, null);
        jPanel5.add(saveAttachmentButton, null);
        attachmentsPanel.add(jLabel2, BorderLayout.NORTH);
        attachmentsPanel.add(jScrollPane2, BorderLayout.CENTER);
        jScrollPane2.getViewport().add(aTable, null);
        attachmentsPanel.add(jPanel4, BorderLayout.SOUTH);
        jPanel4.add(jPanel5, BorderLayout.EAST);
        jScrollPane1.getViewport().add(hTable, null);


        // Added for Bug 8149
        bodyPanel1.add(jPanel1_1, BorderLayout.NORTH);
        bodyPanel1.add(cardPanel1, BorderLayout.CENTER);
        cardPanel1.add(viewTab1, "text");
        viewTab1.add(jPanel8, "");
        jPanel8.add(textEditor1, BorderLayout.CENTER);
        viewTab1.add(tree1, "");
        // Added for Bug 8149

        saveAttachmentButton.setEnabled(false);
    }

    void viewTab_stateChanged(ChangeEvent e) {
        if (viewTab1.getSelectedIndex() == 1) {
            try {
                tree1.setXML(textEditor1.getText());
            }
            catch (Exception ignore) {
            }
        }
    }

    void saveAttachmentButton_actionPerformed(ActionEvent e) {
        int row = aTable.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please Select an attachment first!");
            return;
        }

        String name = amodel.getValueAt(row, 0).toString();
        File file = TifosiFileChooser.showSaveFileDialog(this, null, (TifosiFileFilter) null, "Save to file...", ".",
                name);

        if (file == null) {
            return;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);

            fout.write((byte[]) amodel.getValueAt(row, 1));
            fout.close();
        }
        catch (IOException ex) {
            ExceptionDisplayDialog.showException(this, ex);
        }

    }

    public void loadDocPanel(Message message) {
        
        bodyPanel.loadBody(message);
        hmodel.setRowCount(0);
        //Header properties
        try {
            HashMap header = MessageUtil.getAllProperties(message);

            if (header != null) {
                Iterator iter = header.entrySet().iterator();

                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();

                    if (MessagePropertyNames.ATTACHMENT_TABLE.equalsIgnoreCase(key)) {
                        continue;
                    }
                    if (MessagePropertyNames.CARRY_FORWARD_CONTEXT.equalsIgnoreCase(key)) {
                        CarryForwardContext cfc = (CarryForwardContext) entry.getValue();
                        if (cfc != null) {
                            textEditor1.setText(cfc.getAppContext());
                        }
                    }
                    Object value = entry.getValue();

                    hmodel.addRow(new Object[]{key, value});
                }
            }
            amodel.setRowCount(0);
            Hashtable hash = MessageUtil.getAttachments(message);
            if (hash != null) {
                Enumeration keys = hash.keys();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();

                    amodel.addRow(new Object[]
                            {key, hash.get(key)});
                }
            }
            viewTab_stateChanged(null);
            if (amodel.getRowCount() > 0) {
                saveAttachmentButton.setEnabled(true);
            }
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOADING_DOC_PANEL), ex);
        }
    }


}
