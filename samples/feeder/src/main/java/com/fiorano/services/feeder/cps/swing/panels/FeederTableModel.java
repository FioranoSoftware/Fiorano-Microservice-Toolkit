/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.cps.swing.panels;

import javax.swing.table.DefaultTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Dec 10, 2007
 * Time: 11:19:05 AM
 */
public class FeederTableModel extends DefaultTableModel {

    public FeederTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    /**
     * Sets value at for object
     */
    public void setValueAt(Object obj, int row, int col) {
        if (obj.toString().trim().length() == 0) {
            return;
        }
        if (col == FeederConstants.NAME_COLUMN) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0).equals(obj)) {
                    return;
                }
            }
        }
        super.setValueAt(obj, row, col);
    }

    public void removeAllRows() {
        int count = getRowCount();
        for (int j = count - 1; j >= 0; j--)
            removeRow(j);
    }
}


