/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.cps.swing;

import com.fiorano.services.common.swing.ConfigurationPanel;
import com.fiorano.services.distributionservice.DistributionServiceConstants;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;
import com.fiorano.uif.ui.TifosiTable;
import com.fiorano.uif.util.PositiveIntegerDocument;
import com.fiorano.uif.util.TifosiTableModel;
import com.fiorano.util.ErrorListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class DistributionConfigurationPanel extends ConfigurationPanel<DistributionServicePM> {

    private final static int MIN_PORT = 1;
    private final static int MAX_PORT = 100;
    private JLabel outputPortCountLabel = new JLabel("Number of output ports");
    private SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, MIN_PORT, MAX_PORT, 1);
    private JSpinner noOfPortsSpinner = new JSpinner(spinnerModel);
    private JPanel portPanel = new JPanel();
    private JPanel weightDistributionPanel = new JPanel();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private TifosiTable portWeightTable = new TifosiTable();
    private TifosiTableModel portWeightTableModel = new TifosiTableModel();
    private JTextField textField = new JTextField();
    private PositiveIntegerDocument positiveIntDoc = new PositiveIntegerDocument();
    private JCheckBox propagatePortSchemaCheckBox = new JCheckBox();
    private JLabel propagatePortSchemaLabel = new JLabel("Propagate Connected Component Schemas");

    public DistributionConfigurationPanel() {
        super(new BorderLayout());
        createUI();
        spinnerModel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int givenValue = (Integer) spinnerModel.getValue();
                int currentValue = portWeightTableModel.getRowCount();
                if (givenValue > currentValue) {
                    for (int i = currentValue; i < givenValue; i++) {
                        portWeightTableModel.addRow(new Object[]{"OUT_PORT_" + i, "" + 1});
                    }
                } else {
                    for (int i = currentValue; i > givenValue; i--) {
                        portWeightTableModel.removeRow(i - 1);
                    }
                }
                portWeightTable.repaint();
            }
        });
    }

    private void createUI() {
        portPanel.setLayout(new GridBagLayout());
        weightDistributionPanel.setLayout(new GridLayout());
        portPanel.add(propagatePortSchemaLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 10, 20), 0, 0));
        portPanel.add(propagatePortSchemaCheckBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 10, 0), 0, 0));
        portPanel.add(outputPortCountLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 10, 20), 0, 0));
        portPanel.add(noOfPortsSpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 10, 0), 0, 0));

        String[] tableHeader = {"Port Name", "Weight"};
        boolean[] editableColumn = {false, true};
        portWeightTableModel.setColumnIdentifiers(tableHeader);
        portWeightTableModel.setEditableColumns(editableColumn);
        portWeightTable.setModel(portWeightTableModel);
        portWeightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        portWeightTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(textField));
        textField.setDocument(positiveIntDoc);
        weightDistributionPanel.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(portWeightTable, null);

        add(portPanel, BorderLayout.NORTH);
        add(weightDistributionPanel, BorderLayout.CENTER);
    }

    @Override
    public DistributionServicePM getConfiguration() {
        DistributionServicePM distributionServicePM = new DistributionServicePM();
        distributionServicePM.setPortCount((Integer) noOfPortsSpinner.getValue());
        distributionServicePM.setPropagateSchema(propagatePortSchemaCheckBox.isSelected());
        int portCount = distributionServicePM.getPortCount();
        int[] aIWeightOnPorts = new int[portCount];

        for (int i = 0; i < portCount; ++i) {
            String weight = (String) portWeightTableModel.getValueAt(i, 1);
            try {
                aIWeightOnPorts[i] = Integer.parseInt(weight);
            } catch (NumberFormatException nfe) {
                aIWeightOnPorts[i] = 1;
            }
        }
        distributionServicePM.setPortWeights(aIWeightOnPorts);
        return distributionServicePM;
    }

    @Override
    public void loadConfiguration(DistributionServicePM distributionServicePM) {
        if (distributionServicePM == null) {
            return;
        }
        int portCount = distributionServicePM.getPortCount();
        noOfPortsSpinner.setValue(portCount);
        int j = distributionServicePM.getPortWeights().length;
        propagatePortSchemaCheckBox.setSelected(distributionServicePM.isPropagateSchema());

        portWeightTableModel.removeAllRows();
        for (int i = 0; i < portCount; ++i) {
            if (i < j) {
                portWeightTableModel.addRow(new Object[]{DistributionServiceConstants.OUT_PORT_PREFIX + i,
                        "" + distributionServicePM.getPortWeights()[i]});
            } else {
                portWeightTableModel.addRow(new Object[]{DistributionServiceConstants.OUT_PORT_PREFIX + i, "" + 1});
            }
        }
        portWeightTable.repaint();
    }

    @Override
    public void validate(ErrorListener listener) {
    }
}
