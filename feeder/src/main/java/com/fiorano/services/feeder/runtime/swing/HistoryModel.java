/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.runtime.swing;

import fiorano.esb.utils.RBUtil;

import javax.jms.TextMessage;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Nov 19, 2007
 * Time: 3:47:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoryModel extends DefaultTableModel {
    private static final String[] COLUMN_NAMES = {RBUtil.getMessage(com.fiorano.services.feeder.runtime.swing.Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.INDEX),
            RBUtil.getMessage(com.fiorano.services.feeder.runtime.swing.Bundle.class, Bundle.SENT),
            RBUtil.getMessage(Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.MESSAGE)};
    private int maxSize = 0;

    public HistoryModel(int maxSize) {
        super(COLUMN_NAMES, 0);
        this.maxSize = maxSize;
    }

    /**
     * Returns value at for object
     */
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return row + 1 + "";
            case 1:
                return super.getValueAt(row, col);
            case 2:
                try {
                    return ((TextMessage) super.getValueAt(row, col)).getText();
                }
                catch (Exception ex) {
                    return null;
                }
        }
        return null;
    }

    /**
     * Returns cell editable for object
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void insertRow(int row, Vector rowData) {
        super.insertRow(row, rowData);
        if (maxSize != -1 && getRowCount() > maxSize) {
            removeRow(0);
        }
    }

    public int getMaxSize() {
           return maxSize;
       }
    /**
     * Returns document for object
     */
    public TextMessage getDocument(int row) {
        return (TextMessage) super.getValueAt(row, 2);
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
