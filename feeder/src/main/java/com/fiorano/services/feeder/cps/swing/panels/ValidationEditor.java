/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.cps.swing.panels;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * Date: Dec 15, 2007
 * Time: 2:41:38 PM
 *
 * @author Venkat
 */
public class ValidationEditor extends DefaultCellEditor {
    private TableCellValidator validator;

    public ValidationEditor(final JCheckBox checkBox, TableCellValidator validator) {
        super(checkBox);
        this.validator = validator;
    }

    public ValidationEditor(final JComboBox comboBox, TableCellValidator validator) {
        super(comboBox);
        this.validator = validator;
    }

    public ValidationEditor(final JTextField textField, TableCellValidator validator) {
        super(textField);
        this.validator = validator;
    }

    private CellDetails previous;

    public boolean stopCellEditing() {
        try {
            if(previous != null) {
                validator.validate(previous.table, super.getCellEditorValue(), previous.selected, previous.row, previous.column);
            } else {
                validator.validate(null, super.getCellEditorValue(), false, -1, -1);
            }
        }
        catch (Exception e) {
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
            ((JComponent) getComponent()).setToolTipText(e.getMessage());
            return false;
        }
        return super.stopCellEditing();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
        ((JComponent) getComponent()).setToolTipText(null);
        Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        previous = new CellDetails(table, value, isSelected, row, column);
        return component;
    }

    private static class CellDetails {
        private JTable table;
        private Object value;
        private boolean selected;
        private int row;
        private int column;

        public CellDetails(JTable table, Object value, boolean selected, int row, int column) {
            this.table = table;
            this.value = value;
            this.selected = selected;
            this.row = row;
            this.column = column;
        }
    }

}