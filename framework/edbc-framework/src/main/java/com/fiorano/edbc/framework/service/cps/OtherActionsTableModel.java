/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.services.common.util.RBUtil;

import javax.swing.table.DefaultTableModel;

/**
 * Date: Mar 13, 2007
 * Time: 4:22:43 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class OtherActionsTableModel extends DefaultTableModel {
    static final int ACTION_INDEX = 0;
    static final int RETRIES_INDEX = 1;
    private static final String[] COLUMN_NAMES = {RBUtil.getMessage(Bundle.class, Bundle.ACTION),
            RBUtil.getMessage(Bundle.class, Bundle.RETRIES_BEFORE_ACTION)};
    private static final Class[] COLUMN_CLASS = {ErrorHandlingAction.class, Integer.class};

    public OtherActionsTableModel() {
        super(COLUMN_NAMES, 0);
    }

    public Class getColumnClass(int columnIndex) {
        return COLUMN_CLASS[columnIndex];
    }

    public boolean isCellEditable(int row, int column) {
        if (column == RETRIES_INDEX) {
            return true;
        } else {
            return ((ErrorHandlingAction) getValueAt(row, ACTION_INDEX)).isEnabled();
        }

    }

    public void enableAction(ErrorHandlingAction action, boolean enabled) {
        for (int index = 0; index < getRowCount(); index++) {
            ErrorHandlingAction actionToEnable = (ErrorHandlingAction) getValueAt(index, ACTION_INDEX);
            if (actionToEnable.equals(action)) {
                actionToEnable.setEnabled(enabled);
                fireTableCellUpdated(index, ACTION_INDEX);
            }
        }
    }
}
