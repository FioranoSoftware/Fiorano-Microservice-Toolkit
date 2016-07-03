/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

/**
 * Date: Mar 13, 2007
 * Time: 5:04:35 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
    /**
     * The Swing component being edited.
     */
    protected JSpinner editorComponent;

//
//  Constructors
//

    /**
     * Constructs a <code>SpinnerEditor</code> that uses a text field.
     */
    public SpinnerEditor(final JSpinner spinner) {
        editorComponent = spinner;
        spinner.setBorder(new EmptyBorder(0, 0, 0, 0));
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SpinnerEditor.this.stopCellEditing();
            }
        });
    }

    private void setValue(Object value) {
        editorComponent.setValue(value);
    }

    /**
     * Returns a reference to the editor component.
     *
     * @return the editor <code>Component</code>
     */
    public Component getComponent() {
        return editorComponent;
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     */
    public Object getCellEditorValue() {
        return editorComponent.getValue();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     */
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     */
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     */
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     */
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    /**
     * Implements the <code>TableCellEditor</code> interface.
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        setValue(value);
        return editorComponent;
    }


}
