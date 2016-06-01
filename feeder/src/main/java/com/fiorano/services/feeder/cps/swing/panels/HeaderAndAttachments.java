/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.cps.swing.panels;

import com.fiorano.swing.table.XTable;
import com.fiorano.uif.filechooser.TifosiFileChooser;
import com.fiorano.uif.filechooser.TifosiFileFilter;
import com.fiorano.uif.ui.ExceptionDisplayDialog;
import fiorano.esb.util.CPSUtil;
import fiorano.esb.utils.RBUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by phani.
 * Date: Feb 22, 2008
 * Time: 11:32:15 PM
 */
public class HeaderAndAttachments extends JPanel {
    private Logger logger = CPSUtil.getAnonymousLogger();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private BorderLayout borderLayout4 = new BorderLayout();
    private JPanel headerPanel = new JPanel();
    private JPanel attachPanel = new JPanel();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private HeaderTableModel hmodel = new HeaderTableModel(new String[]{"Name", "Type", "Value"}, 0);
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private JTable hTable = new XTable(hmodel);
    private JButton haddButton = new JButton();
    private JButton hdelButton = new JButton();
    private JButton hdelAllButton = new JButton();
    private GridLayout gridLayout1 = new GridLayout();
    private DefaultComboBoxModel typeComboBoxModel = new DefaultComboBoxModel();
    private JComboBox typeComboBox = new JComboBox(typeComboBoxModel);
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private AttachmentTableModel amodel = new AttachmentTableModel(new String[]{"Name", "Size"}, 0);
    private JPanel jPanel3 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JTable aTable = new XTable(amodel);
    private JScrollPane jScrollPane2 = new JScrollPane();
    private JButton aaddButton = new JButton();
    private JButton adelButton = new JButton();
    private JButton adelAllButton = new JButton();
    private JPanel parentPanel;

    public HeaderAndAttachments() {
        try {
            jbinit();
            aTable.getColumn("Size").setCellRenderer(
                    new DefaultTableCellRenderer() {
                        public Component getTableCellRendererComponent(JTable table,
                                                                       Object value,
                                                                       boolean isSelected, boolean hasFocus, int row,
                                                                       int column) {
                            float size = ((byte[]) value).length;

                            if (size != 0) {
                                size = size / 1024;
                            }

                            String name = "";
                            if (size >= 1) {
                                name = size + " KB";
                            } else {
                                name = ((byte[]) value).length + " bytes";
                            }
                            return super.getTableCellRendererComponent(table, name, isSelected, hasFocus, row, column);
                        }
                    });
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(com.fiorano.services.feeder.runtime.swing.Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.LOAD_HEADER_FAIL), e);

        }
    }

    public HeaderAndAttachments(JPanel mainPanel) {
        this();
        this.parentPanel = mainPanel;
    }

    public HeaderTableModel getHmodel() {
        return hmodel;
    }

    public AttachmentTableModel getAmodel() {
        return amodel;
    }

    void jbinit() {

        Border border1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border border2 = BorderFactory.createEmptyBorder(5, 0, 0, 0);
        Border border3 = BorderFactory.createEtchedBorder(Color.white,
                new Color(148, 145, 140));
        Border border4 = BorderFactory.createEmptyBorder(5, 0, 0, 0);
        Border border5 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border border6 = BorderFactory.createEtchedBorder(Color.white,
                new Color(148, 145, 140));
        jPanel1.setLayout(borderLayout1);
        jPanel1.setBorder(border2);
        borderLayout2.setHgap(5);
        borderLayout3.setHgap(5);
        gridLayout1.setHgap(5);
        jScrollPane1.setBorder(border3);

        jLabel1.setText(" Header Properties ");
        jLabel2.setText(" Attachments ");
        haddButton.setText("Add");

        hdelButton.setText("Delete");
        haddButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        haddButton_actionPerformed(e);
                    }
                });
        hdelButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        hdelButton_actionPerformed(e);
                    }
                }
        );
        hdelAllButton.setText("Delete All");
        hdelAllButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        hdelAllButton_actionPerformed(e);
                    }
                }
        );
        headerPanel.setLayout(borderLayout2);
        headerPanel.setBorder(border1);
        headerPanel.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(hTable, null);
        headerPanel.add(jLabel1, BorderLayout.NORTH);
        headerPanel.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jPanel2, BorderLayout.EAST);
        jPanel2.setLayout(gridLayout1);
        jPanel2.add(haddButton, null);
        jPanel2.add(hdelButton, null);
        jPanel2.add(hdelAllButton, null);
        jScrollPane2.setBorder(border6);
        aaddButton.setText("Add...");
        aaddButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        aaddButton_actionPerformed(e);
                    }
                });
        adelButton.setText("Delete");
        adelButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adelButton_actionPerformed(e);
                    }
                });
        adelAllButton.setText("Delete All");
        adelAllButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adelAllButton_actionPerformed(e);
                    }
                });
        jPanel3.setLayout(gridLayout1);
        jPanel3.add(aaddButton, null);
        jPanel3.add(adelButton, null);
        jPanel3.add(adelAllButton, null);
        jPanel4.setLayout(borderLayout3);
        jPanel4.setBorder(border4);
        jPanel4.add(jPanel3, BorderLayout.EAST);
        attachPanel.setLayout(borderLayout4);
        attachPanel.setBorder(border5);
        attachPanel.add(jLabel2, BorderLayout.NORTH);
        attachPanel.add(jScrollPane2, BorderLayout.CENTER);
        jScrollPane2.getViewport().add(aTable, null);
        attachPanel.add(jPanel4, BorderLayout.SOUTH);

        TableCellEditor cellEditor = new DefaultCellEditor(typeComboBox);
        hTable.setDefaultEditor(ArrayList.class, cellEditor);
        hTable.getColumnModel().getColumn(FeederConstants.VALUE_COLUMN).setCellEditor(new ValidationEditor(new JTextField(), new ValueColumnValidator()));

        typeComboBoxModel.addElement(FeederConstants.STRING_PROPERTY_TYPE);
        typeComboBoxModel.addElement(FeederConstants.INT_PROPERTY_TYPE);
        typeComboBoxModel.addElement(FeederConstants.FLOAT_PROPERTY_TYPE);
        typeComboBoxModel.addElement(FeederConstants.DOUBLE_PROPERTY_TYPE);
        typeComboBoxModel.addElement(FeederConstants.LONG_PROPERTY_TYPE);
        typeComboBoxModel.addElement(FeederConstants.OBJECT_PROPERTY_TYPE);
    }

    public void setButtonsVisibility() {
        adelButton.setEnabled(amodel.getRowCount() > 0);
        adelAllButton.setEnabled(amodel.getRowCount() > 0);
        hdelButton.setEnabled(hmodel.getRowCount() > 0);
        hdelAllButton.setEnabled(hmodel.getRowCount() > 0);
    }

    void aaddButton_actionPerformed(ActionEvent e) {
        File file = TifosiFileChooser.showOpenFileDialog(parentPanel, null,
                (TifosiFileFilter) null, "Select file to be attached", ".",
                null);

        if (file == null) {
            return;
        }
        String name = file.getName();

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte bytes[] = new byte[1024];

            while (true) {
                int read = fis.read(bytes);

                if (read == -1) {
                    break;
                }
                bout.write(bytes, 0, read);
            }
            fis.close();
            bout.close();

            int row = amodel.getRowCount();

            amodel.addRow(new String[]{"", ""});
            amodel.setValueAt(name, row, 0);
            if (!amodel.getValueAt(row, 0).equals(name)) {  //todo
                for (int i = 1; ; i++) {
                    amodel.setValueAt(name + i, row, 0);
                    if (amodel.getValueAt(row, 0).equals(name + i)) {
                        break;
                    }
                }
            }
            aTable.setRowSelectionInterval(row, row);
            aTable.setColumnSelectionInterval(1, 1);
            amodel.setValueAt(bout.toByteArray(), row, 1);

            // bug 9275
            if (amodel.getRowCount() > 0) {
                adelButton.setEnabled(true);
                adelAllButton.setEnabled(true);
            }
        }
        catch (IOException ex) {
            ExceptionDisplayDialog.showException(this, ex);
        }
    }

    void adelButton_actionPerformed(ActionEvent e) {
        if (aTable.getCellEditor() != null) {
            aTable.getCellEditor().stopCellEditing();
        }

        int row[] = aTable.getSelectedRows();

        if (row == null) {
            return;
        }
        for (int i = row.length - 1; i >= 0; i--) {
            amodel.removeRow(row[i]);
        }
        // bug 9275
        if (amodel.getRowCount() == 0) {
            adelAllButton.setEnabled(false);
            adelButton.setEnabled(false);
        }
    }

    void adelAllButton_actionPerformed(ActionEvent e) {
        if (aTable.getCellEditor() != null) {
            aTable.getCellEditor().stopCellEditing();
        }
        amodel.setRowCount(0);
        //bug 9275
        adelAllButton.setEnabled(false);
        adelButton.setEnabled(false);
    }

    void haddButton_actionPerformed(ActionEvent e) {
        int row = hmodel.getRowCount();

        hmodel.addRow(new String[]{"", FeederConstants.STRING_PROPERTY_TYPE, ""});
        for (int i = 1; ; i++) {
            hmodel.setValueAt("Name" + i, row, 0);
            if (hmodel.getValueAt(row, 0).equals("Name" + i)) {
                break;
            }
        }

        hTable.setRowSelectionInterval(row, row);
        hTable.setColumnSelectionInterval(2, 2);
        // bug 9275
        if (hmodel.getRowCount() > 0) {
            hdelButton.setEnabled(true);
            hdelAllButton.setEnabled(true);
        }
    }

    void hdelButton_actionPerformed(ActionEvent e) {
        if (hTable.getCellEditor() != null) {
            hTable.getCellEditor().stopCellEditing();
        }

        int row[] = hTable.getSelectedRows();

        if (row == null) {
            return;
        }
        for (int i = row.length - 1; i >= 0; i--) {
            hmodel.removeRow(row[i]);
        }
        // bug 9275
        if (hmodel.getRowCount() == 0) {
            hdelAllButton.setEnabled(false);
            hdelButton.setEnabled(false);
        }
    }

    void hdelAllButton_actionPerformed(ActionEvent e) {
        if (hTable.getCellEditor() != null) {
            hTable.getCellEditor().stopCellEditing();
        }
        hmodel.setRowCount(0);
        //bug 9275
        hdelAllButton.setEnabled(false);
        hdelButton.setEnabled(false);
    }

    //Get methods.
    public JPanel getHeaderPanel() {
        return headerPanel;
    }

    public JPanel getAttachPanel() {
        return attachPanel;
    }

    public JTable gethTable() {
        return hTable;
    }

    public JTable getaTable() {
        return aTable;
    }

    public class AttachmentTableModel extends FeederTableModel {
        public AttachmentTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }
    }

    public class HeaderTableModel extends FeederTableModel {
        public HeaderTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        /**
         * Returns column class for object
         */
        public Class getColumnClass(int index) {
            if (index == 1) {
                return ArrayList.class;
            }

            return Object.class;
        }

    }

}
