/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.common.swing.EncryptDecryptPanel;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.uif.wizard.WizardPanel;
import com.fiorano.xml.ClarkName;
import com.fiorano.xml.XMLStructure;
import com.fiorano.xml.dtd.DTDXMLStructure;
import com.fiorano.xml.xsd.XSDUtil;
import com.fiorano.xml.xsd.XSDXMLStructure;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CPSUtil;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.openide.WizardValidationException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ramesh.
 * Date: Sep 2, 2012
 * Time: 11:32:15 AM
 *
 * @bundle $class.title= Encryption Configuration
 * @bundle $class.summary=Enter the details Encryption/Decryption
 */
public class EncryptDecryptConfigPanel extends WizardStep {
    private Logger logger = CPSUtil.getAnonymousLogger();
    private PortsTableModel portsTableModel = new PortsTableModel(new String[]{"Ports"}, 0);
    private JTable portsTable = new JTable(portsTableModel);
    private JButton refreshButton = new JButton();
    private JButton portDelButton = new JButton();
    private JScrollPane portScrollPane = new JScrollPane();
    private JPanel portsPanel = new JPanel();
    private EncryptDecryptPanel encryptDecryptPanel = new EncryptDecryptPanel();
    private CBRPropertyModel cbrPropertyModel;
    private WizardPanel panel = new WizardPanel();
    private List<String> portsList = new ArrayList<String>();
    private XMLStructure structure;
    private boolean isLoaded = false;
    private EncryptDecryptElements encryptElements;
    private EncryptDecryptElements decryptElements;
    private int previousSelection = -1;

    public EncryptDecryptConfigPanel(CBRPropertyModel cbrPropertyModel) {
        this.cbrPropertyModel = cbrPropertyModel;
        panel.setLayout(new BorderLayout());
        try {
            jbInit();
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.LOAD_ENCRYPT_PANEL_FAIL), e);

        }
    }

    @Override
    protected WizardPanel createComponent() {
        return panel;
    }

    void jbInit() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.add(portsPanel);
        splitPane.add(encryptDecryptPanel);
        splitPane.setResizeWeight(0.25);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize((int) (splitPane.getDividerSize() * 1.10));
        splitPane.setDividerLocation(150);

        panel.add(splitPane, BorderLayout.WEST);

        Border border6 = BorderFactory.createEtchedBorder(Color.white,
                new Color(148, 145, 140));

        portsPanel.setLayout(new BorderLayout());
        portsPanel.setPreferredSize(new Dimension(150, 400));
        JPanel portButtonPanel = new JPanel(new GridLayout(1, 2));
        portButtonPanel.add(portDelButton);
        portButtonPanel.add(refreshButton);
        portsPanel.add(portButtonPanel, BorderLayout.NORTH);
        portsPanel.add(portScrollPane, BorderLayout.CENTER);
        portScrollPane.setBorder(border6);
        portScrollPane.getViewport().add(portsTable, BorderLayout.CENTER);
        portDelButton.setText("Delete");
        refreshButton.setText("refresh");

        portDelButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        portDelButton_actionPerformed();
                    }
                });
        refreshButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        refresh();
                    }
                }
        );

        TableCellEditor cellEditor = new DefaultCellEditor(new JTextField());
        portsTable.setDefaultEditor(String.class, cellEditor);
        ListSelectionModel listSelectionModel = portsTable.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionHandler());
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        portsTable.setSelectionModel(listSelectionModel);
    }

    public XMLStructure createXMLStructure() {
        ESBRecordDefinition recordDefinition = cbrPropertyModel.getSchemaDefinition();
        XMLStructure structure = null;

        if (recordDefinition != null) {
            XSLoader xsLoader;
            try {
                xsLoader = XSDUtil.createXSLoader(recordDefinition, null);
                XSModel xsModel = xsLoader.load(new DOMInputImpl(null, null, null, recordDefinition.getStructure(), null));
                String rootElementClarkName = ClarkName.toClarkName(recordDefinition.getTargetNamespace(), recordDefinition.getRootElementName());
                if (recordDefinition.getDefinitionType() == ISchema.XSD) {
                    structure = new XSDXMLStructure(xsModel, rootElementClarkName);
                } else {
                    DTDParser parser = new DTDParser(new StringReader(recordDefinition.getStructure()));
                    DTD dtd = parser.parse(true);
                    structure = new DTDXMLStructure(dtd, rootElementClarkName);
                }

            } catch (Exception e) {
            }
        }
        return structure;
    }

    void portDelButton_actionPerformed() {

        if (portsTable.getCellEditor() != null) {
            portsTable.getCellEditor().stopCellEditing();
        }

        int row[] = portsTable.getSelectedRows();

        if (row == null) {
            return;
        }
        for (int i = row.length - 1; i >= 0; i--) {
            portsList.remove(portsTableModel.getValueAt(row[i], 0));
            portsTableModel.removeRow(row[i]);
        }
        isLoaded = false;

    }

    void refresh() {
        createPorts();

        portsTableModel.removeAllRows();
        for (String port : portsList) {
            portsTableModel.addRow(new String[]{port});
        }
        isLoaded = false;
        portsTable.setRowSelectionInterval(0, 0);
    }

    void createPorts() {
        String inPort = "IN_PORT";
        String outPort = "OUT_PORT";
        if (!portsList.contains(inPort)) {
            portsList.add(inPort);
        }
        if (!portsList.contains(outPort)) {
            portsList.add(outPort);
        }
        structure = createXMLStructure();
        if (encryptElements == null) {
            encryptElements = new EncryptDecryptElements();
        }
        if (decryptElements == null) {
            decryptElements = new EncryptDecryptElements();
        }
    }

    public void component2Model() {
        encryptDecryptPanel.component2Model();
        cbrPropertyModel.setElementsToEncrypt(encryptElements);
        cbrPropertyModel.setElementsToDecrypt(decryptElements);
    }

    public void model2Component() {
        encryptElements = cbrPropertyModel.getElementsToEncrypt();
        decryptElements = cbrPropertyModel.getElementsToDecrypt();

        refresh();
    }

    @Override
    public void fastValidate() throws WizardValidationException {
    }

    @Override
    public void lazyValidate() throws WizardValidationException {
    }

    public class PortsTableModel extends EncryptTableModel {
        public PortsTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

    }

    class ListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            int selectionIndex = lsm.getMinSelectionIndex();

            if (selectionIndex != -1) {
                encryptDecryptPanel.component2Model();
                if (previousSelection != selectionIndex || !isLoaded) {
                    String portName = (String) portsTableModel.getValueAt(selectionIndex, 0);
                    encryptDecryptPanel.model2Component("IN_PORT".equals(portName) ? decryptElements : encryptElements,
                            structure);
                    isLoaded = true;
                }
                previousSelection = selectionIndex;
            }
        }
    }

}
