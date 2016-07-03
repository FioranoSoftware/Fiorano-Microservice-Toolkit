/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps.swing.panels;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.uif.UIFException;
import com.fiorano.uif.ui.ExceptionDisplayDialog;
import com.fiorano.uif.util.PositiveIntegerSpinner;
import com.fiorano.uif.util.TextEditor;
import com.fiorano.uif.wizard.ValidationException;
import com.fiorano.uif.wizard.WizardPanel;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CPSUtil;
import fiorano.esb.utils.RBUtil;
import org.openide.WizardValidationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.logging.Level;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 */

/**
 * @bundle $class.title=Default Message
 * @bundle $class.summary=Specify the default message and history size
 */
public class DefaultMessagePanel extends WizardStep {
    private JCheckBox infinityCheckBox = new JCheckBox();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();
    private PositiveIntegerSpinner historyField = new PositiveIntegerSpinner();
    private TextEditor editor = new TextEditor(TextEditor.XML);
    private JButton sampleButton = new JButton();
    private JButton validateButton = new JButton();
    private WizardPanel panel = new WizardPanel();
    private FeederPM configuration;

    public DefaultMessagePanel(FeederPM configuration) {
        this.configuration = configuration;

        try {
            jbInit();
        }
        catch (Exception ex) {
            CPSUtil.getAnonymousLogger().log(Level.SEVERE, RBUtil.getMessage(com.fiorano.services.feeder.cps.swing.panels.Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.LOAD_DEFAULT_FAIL), ex);
        }
    }

    @Override
    protected WizardPanel createComponent() {
        return panel;

    }

    @Override
    public void lazyValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void model2Component() {

        editor.setText(configuration.getDefaultMessage());

        int iHistorySize = configuration.getHistorySize();
        if (iHistorySize == -1) {
            infinityCheckBox.setSelected(true);
        } else {
            infinityCheckBox.setSelected(false);
            historyField.setValue(iHistorySize);
        }
        sampleButton.setEnabled(FeederPM.XML == configuration.getMessageFormat());
        validateButton.setEnabled(FeederPM.XML == configuration.getMessageFormat());
        editor.setCaretPosition(0);
    }

    public void fastValidate() throws WizardValidationException {

        try {
            historyField.getValue();
        }
        catch (UIFException ex) {
            try {
                throw new ValidationException(RBUtil.getMessage(com.fiorano.services.feeder.runtime.swing.Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.INVALID_HISTORY_SIZE),
                        RBUtil.getMessage(com.fiorano.services.feeder.runtime.swing.Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.INVALID_HISTORY_SIZE_DESC), historyField);
            } catch (ValidationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void component2Model() {
        configuration.setDefaultMessage(editor.getText());

        try {
            int iHistorySize = (infinityCheckBox.isSelected()) ? -1 : historyField.getValue();
            configuration.setHistorySize(iHistorySize);
        }
        catch (UIFException ignore) {
            //ignore
        }
    }

    private void jbInit() throws Exception {
        historyField.setMinimum(1);
        historyField.setValue(10);
        panel.setLayout(gridBagLayout1);
        jLabel2.setText("Type in default message here:");
        jLabel3.setText("History size  ");
        sampleButton.setText("Generate Sample");
        sampleButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sampleButton_actionPerformed(e);
                    }
                });
        validateButton.setText("Validate");
        validateButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        validateButton_actionPerformed(e);
                    }
                });
        infinityCheckBox.setText("infinite");
        infinityCheckBox.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        infinityCheck_itemStateChanged(e);
                    }
                });
        historyField.setMinimumSize(new Dimension(100, 19));
        historyField.setPreferredSize(new Dimension(100, 19));
        panel.add(jLabel2, new GridBagConstraints(0, 1, 5, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(10, 0, 0, 0), 0, 0));
        panel.add(jLabel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        panel.add(editor, new GridBagConstraints(0, 2, 5, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 10, 0), 0, 0));
        panel.add(historyField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        panel.add(sampleButton, new GridBagConstraints(3, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 5), 0, 0));
        panel.add(validateButton, new GridBagConstraints(4, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 5), 0, 0));
        panel.add(infinityCheckBox, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        historyField.setColumns(16);
    }

    private void sampleButton_actionPerformed(ActionEvent e) {
        ESBRecordDefinition schema = configuration.getSchema();
        try {
            String str = XMLHandler.generateXML(panel.getDialog(), schema);
            if (str != null) {
                editor.setText(str);
            }
            editor.setCaretPosition(0);
        }
        catch (Exception ex) {
            ExceptionDisplayDialog.SHOW_FULL_EXCEPTION = true;
            ExceptionDisplayDialog.showException(panel, ex);
        }
    }

    private void infinityCheck_itemStateChanged(ItemEvent e) {
        historyField.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
    }

    private void validateButton_actionPerformed(ActionEvent e) {
        if (verifyWithXSD()) {
            JOptionPane.showMessageDialog(editor, "Validation Successful", "Validation Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean verifyWithXSD() {
        ESBRecordDefinition schema = configuration.getSchema();
        try {
            return XMLHandler.validateXML(editor.getText(), schema);
        }
        catch (Exception ex) {
            ExceptionDisplayDialog.SHOW_FULL_EXCEPTION = true;
            ExceptionDisplayDialog.showException(panel, ex);
            return false;
        }
    }
}