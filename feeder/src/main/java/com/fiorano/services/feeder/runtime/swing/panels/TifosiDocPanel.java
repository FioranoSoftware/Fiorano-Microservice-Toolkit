/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.runtime.swing.panels;

import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.xmlsecurity.encryption.XMLEncrypter;
import com.fiorano.services.feeder.cps.swing.panels.XMLHandler;
import com.fiorano.services.feeder.cps.swing.panels.FeederConstants;
import com.fiorano.services.feeder.runtime.swing.Bundle;
import com.fiorano.services.feeder.cps.swing.panels.HeaderAndAttachments;
import com.fiorano.uif.images.ImageReference;
import com.fiorano.uif.ui.ExceptionDisplayDialog;
import com.fiorano.uif.ui.TifosiImage;
import com.fiorano.uif.util.RolloverEffect;
import com.fiorano.uif.util.TextEditor;
import com.fiorano.uif.util.TifosiBytesEditorPanel;
import com.fiorano.uif.util.TextUtils;
import com.fiorano.uif.xml.util.XMLTreeTable;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.util.MessagePropertyNames;
import fiorano.esb.utils.RBUtil;
import fiorano.tifosi.util.xmlutils.XMLUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 */
public class TifosiDocPanel extends JPanel {

    ImageIcon textIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "textView.gif"));
    ImageIcon treeIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "treeView.gif"));

    private BorderLayout borderLayout1 = new BorderLayout();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JPanel bodyPanel = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout8 = new BorderLayout();
    private JPanel radioButtonsPanel = new JPanel();
    private BorderLayout borderLayout3 = new BorderLayout();
    private JRadioButton textRadio = new JRadioButton();
    private JRadioButton bytesRadio = new JRadioButton();
    private JPanel cardPanel = new JPanel();
    private CardLayout cardLayout1 = new CardLayout();
    private ButtonGroup buttonGroup1 = new ButtonGroup();
    private JTabbedPane viewTab = new JTabbedPane();
    private TifosiBytesEditorPanel bytesEditor = new TifosiBytesEditorPanel();
    private JPanel headerAttachmentsPanel = new JPanel();
    private GridLayout gridLayout3 = new GridLayout();
    private JPanel jPanel7 = new JPanel();
    private TextEditor textEditor = new TextEditor(TextEditor.XML);
    private XMLTreeTable tree = new XMLTreeTable();
    private JPanel toolbar = new JPanel();
    private JPanel checksPanel = new JPanel();
    private JPanel jButtonsPanel = new JPanel();
    private JButton validateButton = new JButton(new ImageIcon(getClass().getResource("checkmapper2.gif")));
    private JSeparator jSeparator2 = new JSeparator();
    private JButton undoButton = new JButton();
    private JButton loadButton = new JButton();
    private JButton beautifyButton = new JButton(new ImageIcon(getClass().getResource("beautify.gif")));
    private JButton redoButton = new JButton();
    private JButton saveButton = new JButton();
    private JButton sampleButton = new JButton(new ImageIcon(getClass().getResource("sample.gif")));
    private JSeparator jSeparator3 = new JSeparator();
    private JSeparator jSeparator4 = new JSeparator();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private GridLayout gridLayout4 = new GridLayout();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JCheckBox clearCB = new JCheckBox();
    private JCheckBox replaceCB = new JCheckBox();
    private JPanel clearReplacePanel = new JPanel();
    private JButton sendButton = new JButton();

    private Session jmsSession;
    private int totalSentMessages = 0;
    private ESBRecordDefinition schema = null;
    private Logger logger;
    private HeaderAndAttachments headerAndAttachments;
    private HeaderAndAttachments.HeaderTableModel hmodel;
    private HeaderAndAttachments.AttachmentTableModel amodel;
    private EncryptDecryptElements encryptDecryptElements;
    private XMLEncrypter xmlEncrypter;

    /**
     * @param jmsSession
     */
    public TifosiDocPanel(Session jmsSession, Logger logger) {
        this.jmsSession = jmsSession;
        this.logger = logger;
        headerAndAttachments = new HeaderAndAttachments(this);
        try {
            jbInit();

            hmodel = headerAndAttachments.getHmodel();
            amodel = headerAndAttachments.getAmodel();

            viewTab.setIconAt(0, textIcon);
            viewTab.setIconAt(1, treeIcon);
            sampleButton.setEnabled(false);
            validateButton.setEnabled(false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.LOAD_HEADER_FAIL), e);
        }
    }

    /**
     * Returns document for object
     */
    public TextMessage getDocument()
            throws JMSException {
        TextMessage doc = jmsSession.createTextMessage();

        stopEditors();
        if (textRadio.isSelected()) {
            doc.setText(encryptDecryptElements != null ? encryptDocument(textEditor.getText()) : textEditor.getText());
        } else {
            byte bytes[] = bytesEditor.getBytes();

            if (bytes != null) {
                MessageUtil.setBytesData(doc, bytes);
            }
        }

        if (hmodel.getRowCount() > 0) {
            updateJMSProps(doc);
        }

        if (amodel.getRowCount() > 0) {
            Hashtable hash = new Hashtable();
            for (int i = 0; i < amodel.getRowCount(); i++) {
                hash.put(amodel.getValueAt(i, 0).toString(), (byte[]) amodel.getValueAt(i, 1));
            }

            MessageUtil.setAttachments(doc, hash);
        }
        try {
            if (replaceCB.isSelected() && doc.getText() != null) {
                doc.setText(TextUtils.replaceAll(doc.getText(), "$index", totalSentMessages + 1 + ""));
            }
        }
        catch (Exception ignore) {
            logger.log(Level.FINEST, RBUtil.getMessage(Bundle.class, Bundle.UPDATE_DATA_FAIL), ignore);
        }
        if (clearCB.isSelected())
            clear();
        totalSentMessages++;
        return doc;
    }

    private String encryptDocument(String xml) {

        if (xmlEncrypter == null) {
            xmlEncrypter = new XMLEncrypter();
            xmlEncrypter.setLogger(logger);
        }

        try {
            xml = xmlEncrypter.encrypt(XMLEncrypter.encryptionConfiguration, xml, encryptDecryptElements.getElements(), encryptDecryptElements.getNamespaces());
        } catch (Exception e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ENCRYPTION_FAILED));
        }

        return xml;
    }

    /**
     * Sets document for object
     */
    public void setDocument(TextMessage doc) throws JMSException {
        stopEditors();

        String text = doc.getText();
        byte bytes[] = MessageUtil.getBytesData(doc);

        textEditor.setText(doc.getText());
        textEditor.setCaretPosition(0);
        textEditor.getUndoManager().discardAllEdits();

        bytesEditor.setBytes(bytes);

        boolean hasText = text != null && text.length() > 0;
        boolean hasBytes = bytes != null && bytes.length > 0;

        textRadio.setSelected(true);
        if (!hasText && hasBytes) {
            bytesRadio.setSelected(true);
        }
        hmodel.setRowCount(0);
        HashMap header = MessageUtil.getAllProperties(doc);
        if (header != null) {
            updateHeaderModel(header);
        }
        amodel.setRowCount(0);
        Hashtable attachment = MessageUtil.getAttachments(doc);
        if (attachment != null) {
            Enumeration keys = attachment.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                amodel.addRow(new Object[]
                        {key, attachment.get(key)});
            }
        }
        headerAndAttachments.setButtonsVisibility();

        viewTab_stateChanged(null);
    }

    public void setSchema(ESBRecordDefinition schema) {
        this.schema = schema;
    }

    public void setEncryptDecryptConfig(EncryptDecryptElements elements) {
        this.encryptDecryptElements = elements;
    }

    /**
     * Sets checks panel for object
     */
    public void setChecksPanel(JPanel checksPanel) {
        this.checksPanel = checksPanel;
    }

    public void clear() {
        textEditor.setText("");
        bytesEditor.setBytes(null);
        hmodel.setRowCount(0);
        amodel.setRowCount(0);
        viewTab_stateChanged(null);
    }

    void jbInit() {
        Border border1 = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        this.setLayout(borderLayout1);
        bodyPanel.setLayout(borderLayout2);
        textRadio.setSelected(true);
        textRadio.setText("Text");
        textRadio.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        textRadio_stateChanged(e);
                    }
                });
        bytesRadio.setText("Bytes");
        bytesRadio.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        bytesRadio_stateChanged(e);
                    }
                });
        radioButtonsPanel.setLayout(borderLayout3);
        radioButtonsPanel.setBorder(border1);
        radioButtonsPanel.add(bytesRadio, BorderLayout.EAST);
        radioButtonsPanel.add(textRadio, BorderLayout.WEST);
        cardPanel.setLayout(cardLayout1);
        viewTab.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        viewTab_stateChanged(e);
                    }
                });
        headerAttachmentsPanel.setLayout(gridLayout3);
        gridLayout3.setColumns(2);
        jPanel7.setLayout(borderLayout8);
        toolbar.setLayout(gridBagLayout1);
        viewTab.setTabPlacement(JTabbedPane.BOTTOM);
        validateButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        validateButton_actionPerformed(e);
                    }
                });
        validateButton.setToolTipText("Validate XML");
        jSeparator2.setPreferredSize(new Dimension(2, 20));
        jSeparator2.setOrientation(JSeparator.VERTICAL);
        jSeparator2.setOrientation(JSeparator.VERTICAL);
        jSeparator2.setPreferredSize(new Dimension(2, 20));
        undoButton.setAction(textEditor.getUndoAction());
        undoButton.setText("");
        undoButton.setToolTipText("Undo");
        loadButton.setAction(textEditor.getLoadAction());
        loadButton.setText("");
        loadButton.setToolTipText("Load from file...");
        beautifyButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        beautifyButton_actionPerformed(e);
                    }
                });
        beautifyButton.setToolTipText("Beautify XML");
        clearCB.setMaximumSize(new Dimension(107, 17));
        clearCB.setMinimumSize(new Dimension(107, 17));
        clearCB.setPreferredSize(new Dimension(107, 17));
        clearCB.setText("Clear after send");
        replaceCB.setMaximumSize(new Dimension(107, 17));
        replaceCB.setMinimumSize(new Dimension(107, 17));
        replaceCB.setPreferredSize(new Dimension(107, 17));
        replaceCB.setText("Replace \"$index\"");
        clearReplacePanel.add(clearCB);
        clearReplacePanel.add(replaceCB);
        setChecksPanel(clearReplacePanel);

        redoButton.setAction(textEditor.getRedoAction());
        redoButton.setText("");
        redoButton.setToolTipText("Redo");
        saveButton.setAction(textEditor.getSaveAction());
        saveButton.setText("");
        saveButton.setToolTipText("Save to file...");
        sampleButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sampleButton_actionPerformed(e);
                    }
                });
        sampleButton.setToolTipText("Generate sample XML");
        jSeparator3.setOrientation(JSeparator.VERTICAL);
        jSeparator3.setPreferredSize(new Dimension(2, 20));
        jSeparator4.setPreferredSize(new Dimension(2, 20));
        jSeparator4.setOrientation(JSeparator.VERTICAL);
        checksPanel.setLayout(gridLayout4);
        jButtonsPanel.setLayout(gridBagLayout2);
        this.add(jTabbedPane1, BorderLayout.CENTER);
        jTabbedPane1.add(bodyPanel, "Body");
        jTabbedPane1.add(headerAttachmentsPanel, "Headers & Attachments");
        headerAttachmentsPanel.add(headerAndAttachments.getHeaderPanel(), null);
        headerAttachmentsPanel.add(headerAndAttachments.getAttachPanel(), null);

        bodyPanel.add(radioButtonsPanel, BorderLayout.NORTH);
        bodyPanel.add(cardPanel, BorderLayout.CENTER);
        cardPanel.add(viewTab, "text");
        viewTab.add(jPanel7, "");
        jPanel7.add(textEditor, BorderLayout.CENTER);
        jPanel7.add(toolbar, BorderLayout.NORTH);
        viewTab.add(tree, "");
        cardPanel.add(bytesEditor, "bytes");
        buttonGroup1.add(textRadio);
        buttonGroup1.add(bytesRadio);
        sendButton.setText("send");
        toolbar.add(jButtonsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(loadButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(saveButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(jSeparator2, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 2, 0, 2), 0, 0));
        jButtonsPanel.add(undoButton, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(redoButton, new GridBagConstraints(4, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(jSeparator3, new GridBagConstraints(5, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 2, 0, 2), 0, 0));
        jButtonsPanel.add(beautifyButton, new GridBagConstraints(6, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(validateButton, new GridBagConstraints(7, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jButtonsPanel.add(sampleButton, new GridBagConstraints(8, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));
        toolbar.add(jSeparator4, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        toolbar.add(checksPanel, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

        loadButton.setAction(textEditor.getLoadAction());
        saveButton.setAction(textEditor.getSaveAction());
        undoButton.setAction(textEditor.getUndoAction());
        redoButton.setAction(textEditor.getRedoAction());
        loadButton.setText("");
        saveButton.setText("");
        undoButton.setText("");
        redoButton.setText("");

        RolloverEffect.enableRolloverEffect(loadButton, true);
        RolloverEffect.enableRolloverEffect(saveButton, true);
        RolloverEffect.enableRolloverEffect(undoButton, true);
        RolloverEffect.enableRolloverEffect(redoButton, true);
        RolloverEffect.enableRolloverEffect(beautifyButton, true);
        RolloverEffect.enableRolloverEffect(validateButton, true);
        RolloverEffect.enableRolloverEffect(sampleButton, true);

        loadButton.setToolTipText("Load from file...");
        saveButton.setToolTipText("Save to file...");
        undoButton.setToolTipText("Undo");
        redoButton.setToolTipText("Redo");
        beautifyButton.setToolTipText("Beautify XML");
        validateButton.setToolTipText("Validate XML");
        sampleButton.setToolTipText("Generate sample XML");

    }

    void textRadio_stateChanged(ChangeEvent e) {
        cardLayout1.show(cardPanel, textRadio.isSelected() ? "text" : "bytes");
    }

    void bytesRadio_stateChanged(ChangeEvent e) {
        cardLayout1.show(cardPanel, textRadio.isSelected() ? "text" : "bytes");
    }

    void viewTab_stateChanged(ChangeEvent e) {
        if (viewTab.getSelectedIndex() == 0) {
            return;
        }
        tree.setXML(textEditor.getText());
    }

    void beautifyButton_actionPerformed(ActionEvent e) {
        String text = textEditor.getText();
        int pos = textEditor.getCaretPosition();

        try {
            text = XMLUtils.beautifyXML(text);
            textEditor.setText(text);
            if (pos > text.length()) {
                pos = text.length();
            }
            textEditor.setCaretPosition(pos);
        }
        catch (Exception ex) {
            // no need to do this. if we cant beautify forget it.
        }
    }

    void validateButton_actionPerformed(ActionEvent e) {
        try {
            XMLHandler.validateXML(textEditor.getText(), schema);
            JOptionPane.showMessageDialog(this, RBUtil.getMessage(Bundle.class, Bundle.VALIDATION_SUCCESSFUL));
        }
        catch (Throwable ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.INVALID_XML), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), RBUtil.getMessage(Bundle.class, Bundle.INVALID_XML),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void sampleButton_actionPerformed(ActionEvent e) {
        try {
            Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
            String str = XMLHandler.generateXML(frame, schema);
            if (str != null) {
                textEditor.setText(str);
            }
            textEditor.setCaretPosition(0);
        }
        catch (Exception ex) {
            ExceptionDisplayDialog.showException(this, ex);
        }
    }

    void stopEditors() {
        JTable hTable = headerAndAttachments.gethTable();
        JTable aTable = headerAndAttachments.getaTable();
        if (hTable.getCellEditor() != null) {
            hTable.getCellEditor().stopCellEditing();
        }
        if (aTable.getCellEditor() != null) {
            aTable.getCellEditor().stopCellEditing();
        }
    }

    private void updateJMSProps(Message doc)
            throws JMSException {
        for (int i = 0; i < hmodel.getRowCount(); i++) {
            String headerName = (String) hmodel.getValueAt(i, 0);

            if ((headerName.indexOf(MessagePropertyNames.PROP_NAME_PREFIX) != -1) || (headerName.indexOf("JMSX")
                    != -1)) {
                continue;
            }

            String type = (String) hmodel.getValueAt(i, 1);
            String value = (String) hmodel.getValueAt(i, 2);
            if (type.equalsIgnoreCase(FeederConstants.STRING_PROPERTY_TYPE)) {
                doc.setStringProperty(headerName, value);
            } else if (type.equalsIgnoreCase(FeederConstants.INT_PROPERTY_TYPE)) {
                int val = Integer.parseInt(value);

                doc.setIntProperty(headerName, val);
            } else if (type.equalsIgnoreCase(FeederConstants.FLOAT_PROPERTY_TYPE)) {
                float val = Float.parseFloat(value);

                doc.setFloatProperty(headerName, val);
            } else if (type.equalsIgnoreCase(FeederConstants.DOUBLE_PROPERTY_TYPE)) {
                double val = Double.parseDouble(value);

                doc.setDoubleProperty(headerName, val);
            } else if (type.equalsIgnoreCase(FeederConstants.LONG_PROPERTY_TYPE)) {
                long val = Long.parseLong(value);

                doc.setLongProperty(headerName, val);
            } else if (type.equalsIgnoreCase(FeederConstants.OBJECT_PROPERTY_TYPE)) {
                doc.setObjectProperty(headerName, value);
            }
        }
    }

    private void updateHeaderModel(HashMap header) {
        if (header == null) {
            return;
        }

        Iterator iter = header.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();

            if (key == null || (key.indexOf(MessagePropertyNames.PROP_NAME_PREFIX) != -1) ||
                    (key.indexOf("JMSX") != -1) || MessagePropertyNames.ATTACHMENT_TABLE.equalsIgnoreCase(key)) {
                continue;
            }

            Object value = entry.getValue();
            if (value instanceof String) {
                hmodel.addRow(new Object[]{key, FeederConstants.STRING_PROPERTY_TYPE, value});
            } else if (value instanceof Integer) {
                hmodel.addRow(new Object[]{key, FeederConstants.INT_PROPERTY_TYPE, value.toString()});
            } else if (value instanceof Float) {
                hmodel.addRow(new Object[]{key, FeederConstants.FLOAT_PROPERTY_TYPE, value.toString()});
            } else if (value instanceof Double) {
                hmodel.addRow(new Object[]{key, FeederConstants.DOUBLE_PROPERTY_TYPE, value.toString()});
            } else if (value instanceof Long) {
                hmodel.addRow(new Object[]{key, FeederConstants.LONG_PROPERTY_TYPE, value.toString()});
            } else if (value != null) {
                hmodel.addRow(new Object[]{key, FeederConstants.OBJECT_PROPERTY_TYPE, value.toString()});
            }
        }
    }

}