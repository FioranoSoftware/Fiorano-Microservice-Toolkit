/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingActionFactory;
import com.fiorano.edbc.framework.service.exception.RetryConfiguration;
import com.fiorano.edbc.framework.service.exception.RetryConfigurationConstants;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.table.XTableHeader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;

/**
 * Date: Mar 13, 2007
 * Time: 1:35:08 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class RetryPanel extends JPanel implements RetryConfigurationConstants {

    private static final RetryConfiguration EMPTY_CONFIGURATION = new RetryConfiguration();
    private JLabel retryIntervalLabel = new JLabel(RBUtil.getMessage(Bundle.class, Bundle.RETRY_INTERVAL));
    private SpinnerNumberModel retryIntervalModel = new SpinnerNumberModel(new Long(1), new Long(1), new Long(Long.MAX_VALUE), new Long(1));
    private JSpinner retryIntervalSpinner = new JSpinner(retryIntervalModel);
    private JLabel retryCountLabel = new JLabel(RBUtil.getMessage(Bundle.class, Bundle.RETRY_COUNT));
    private SpinnerNumberModel retryCountModel;
    private JSpinner retryCountSpinner = new JSpinner();
    private JLabel actionsDuringRetries = new JLabel(RBUtil.getMessage(Bundle.class, Bundle.ACTIONS_DURING_RETRES));
    private OtherActionsTableModel otherActionTableModel = new OtherActionsTableModel();
    private JTable otherActionsTable = new JTable(otherActionTableModel);
    private SpinnerNumberModel otherActionSpinnerModel = new SpinnerNumberModel(new Integer(1), new Integer(1), new Integer(Integer.MAX_VALUE), new Integer(1));
    private JScrollPane scrollPane = new JScrollPane(otherActionsTable);


    public RetryPanel() {
        this(null);
    }

    public RetryPanel(RetryConfiguration configuration) {
        super(new GridBagLayout());
        createUI();
        loadConfiguration(configuration);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();

        }
        final JDialog dlg = new JDialog();
        dlg.getContentPane().setLayout(new BorderLayout());
        RetryConfiguration configuration = new RetryConfiguration();
        configuration.addOtherAction(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT), new Integer(2));
        ErrorHandlingAction errorHandlingAction = ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.LOG);
        errorHandlingAction.setEnabled(true);
        configuration.addOtherAction(errorHandlingAction, new Integer(2));
        final RetryPanel comp = new RetryPanel(configuration);
        dlg.getContentPane().add(comp);
        dlg.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        dlg.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                RetryConfiguration conf = comp.getConfiguration();

                System.exit(-1);
            }

            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                dlg.dispose();
            }
        });
        dlg.pack();
        dlg.show();
    }

    private void createUI() {
        this.add(retryIntervalLabel, new GridBagConstraints(0, 0,
                1, 1,
                0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 0, 5),
                0, 0));
        this.add(retryIntervalSpinner, new GridBagConstraints(1, 0,
                1, 1,
                1, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 0, 5),
                0, 0));
        this.add(retryCountLabel, new GridBagConstraints(0, 1,
                1, 1,
                0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 0, 5),
                0, 0));
        this.add(retryCountSpinner, new GridBagConstraints(1, 1,
                1, 1,
                1, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 0, 5),
                0, 0));
        this.add(actionsDuringRetries, new GridBagConstraints(0, 2,
                2, 1,
                1, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 0, 5),
                0, 0));
        this.add(scrollPane, new GridBagConstraints(0, 3,
                2, GridBagConstraints.REMAINDER,
                1, 1,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 0, 5),
                0, 0));
        removeXTableHeaderMouseListener();
        prepareTable();
        retryCountModel = new SpinnerNumberModel(new Integer(1), new Integer(-1), new Integer(Integer.MAX_VALUE), new Integer(1)) {
            public Object getNextValue() {
                if (getNumber().intValue() == -1) {
                    setValue(super.getNextValue());
                }
                return super.getNextValue();
            }

            public Object getPreviousValue() {
                if (getNumber().intValue() == 1) {
                    setValue(super.getPreviousValue());
                }
                return super.getPreviousValue();
            }
        };
        retryCountSpinner.setModel(retryCountModel);
        retryCountSpinner.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof SpinnerNumberModel) {
                    SpinnerNumberModel spinnerModel = (SpinnerNumberModel) e.getSource();
                    Comparable currentValue = (Comparable) spinnerModel.getNumber();
                    if (currentValue.compareTo(otherActionSpinnerModel.getMinimum()) >= 0) {
                        otherActionSpinnerModel.setMaximum(currentValue);
                        synchronizeRetriesBeforeActionValues(currentValue);
                    } else if (currentValue.compareTo(spinnerModel.getMinimum()) == 0) {
                        otherActionSpinnerModel.setMaximum(new Integer(Integer.MAX_VALUE));
                    }
                }
            }

            private void synchronizeRetriesBeforeActionValues(Comparable currentValue) {
                if (!Comparable.class.isAssignableFrom(otherActionTableModel.getColumnClass(OtherActionsTableModel.RETRIES_INDEX))) {
                    return;
                }
                for (int index = 0; index < otherActionTableModel.getRowCount(); index++) {
                    Comparable otherActionCurrentValue = (Comparable) otherActionTableModel.getValueAt(index, OtherActionsTableModel.RETRIES_INDEX);
                    if (currentValue.compareTo(otherActionCurrentValue) < 0) {
                        otherActionTableModel.setValueAt(currentValue, index, OtherActionsTableModel.RETRIES_INDEX);
                    }
                }
            }
        });
    }

    public RetryConfiguration getConfiguration() {
        RetryConfiguration configuration = new RetryConfiguration();
        configuration.setRetryCount(((Number) retryCountSpinner.getValue()).intValue());
        configuration.setRetryInterval(((Number) retryIntervalSpinner.getValue()).intValue());
        for (int index = 0; index < otherActionsTable.getRowCount(); index++) {
            configuration.addOtherAction((ErrorHandlingAction) otherActionsTable.getValueAt(index, OtherActionsTableModel.ACTION_INDEX),
                    ((Integer) otherActionsTable.getValueAt(index, OtherActionsTableModel.RETRIES_INDEX)));
        }
        return configuration;
    }

    public void loadConfiguration(RetryConfiguration configuration) {
        if (configuration == null) {
            configuration = EMPTY_CONFIGURATION;
        }
        retryCountSpinner.setValue(new Integer(configuration.getRetryCount()));
        retryIntervalSpinner.setValue(new Long(configuration.getRetryInterval()));
        removeAllOtherActions();
        Map otherActions = configuration.getOtherActions();
        if (otherActions != null) {
            DefaultTableModel tableModel = (DefaultTableModel) otherActionsTable.getModel();
            Iterator entriesIterator = otherActions.entrySet().iterator();
            while (entriesIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) entriesIterator.next();
                if (entry.getKey() instanceof ErrorHandlingAction && entry.getValue() instanceof Integer) {
                    tableModel.addRow(new Object[]{(ErrorHandlingAction) entry.getKey(), (Integer) entry.getValue()});
                }
            }
        }
    }

    public void enableAction(ErrorHandlingAction action, boolean isEnabled) {
        OtherActionsTableModel tableModel = (OtherActionsTableModel) otherActionsTable.getModel();
        tableModel.enableAction(action, isEnabled);
    }

    private void removeAllOtherActions() {
        DefaultTableModel tableModel = (DefaultTableModel) otherActionsTable.getModel();
        for (int index = 0; index < tableModel.getRowCount(); index++) {
            tableModel.removeRow(index);
        }
    }

    public void reset() {
        retryCountSpinner.setValue(new Integer(RETRY_COUNT));
        retryIntervalSpinner.setValue(new Long(RETRY_INTERVAL));
        removeAllOtherActions();
    }

    public void setEnabled(boolean enabled) {
        retryCountSpinner.setEnabled(enabled);
        retryIntervalSpinner.setEnabled(enabled);
        otherActionsTable.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    private void prepareTable() {
        TableColumn tableColumn;
        tableColumn = otherActionsTable.getColumnModel().getColumn(OtherActionsTableModel.RETRIES_INDEX);
        tableColumn.setCellEditor(new SpinnerEditor(new JSpinner(otherActionSpinnerModel)));
        tableColumn.setCellRenderer(new SpinnerRenderer());

        tableColumn = otherActionsTable.getColumnModel().getColumn(OtherActionsTableModel.ACTION_INDEX);
        tableColumn.setCellRenderer(new ErrorHandlingActionRenderer());
    }

    private void removeXTableHeaderMouseListener() {
        MouseListener[] mouseListeners = otherActionsTable.getTableHeader().getMouseListeners();
        for (int iCnt = 0; iCnt < mouseListeners.length; iCnt++) {
            if (mouseListeners[iCnt] instanceof XTableHeader) {
                otherActionsTable.getTableHeader().removeMouseListener(mouseListeners[iCnt]);
            }
        }
    }

    private static class ErrorHandlingActionRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, value instanceof ErrorHandlingAction ? ((ErrorHandlingAction) value).getName() : value,
                    isSelected, hasFocus, row, column);
        }
    }

    private static class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        public SpinnerRenderer() {
            super();
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected && !hasFocus) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setValue(value);
            return this;
        }
    }

}
