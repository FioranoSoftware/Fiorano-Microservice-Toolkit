/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.swing.editors.panels;

import com.fiorano.services.cache.engine.dmi.XML;
import com.fiorano.services.common.util.RBUtil;

import javax.swing.table.DefaultTableModel;

/**
 * TableModel which can be used for table used to show/provide Field Definitions
 *
 * @author Venkat
 */
public class FieldDefinitionTableModel extends DefaultTableModel {
    static final int NAME_INDEX = 0;
    static final int TYPE_INDEX = 1;
    static final int IS_KEY_INDEX = 2;
    static final int XSD_INDEX = 3;
    private static final String[] COLUMN_NAMES = {RBUtil.getMessage(Bundle.class, Bundle.NAME),
            RBUtil.getMessage(Bundle.class, Bundle.TYPE),
            RBUtil.getMessage(Bundle.class, Bundle.KEY), RBUtil.getMessage(Bundle.class, Bundle.XSD)};
    private static final Class[] COLUMN_CLASS = {String.class, Class.class, Boolean.class, Class.class};

    public FieldDefinitionTableModel() {
        super(COLUMN_NAMES, 0);
    }

    public Class getColumnClass(int columnIndex) {
        return COLUMN_CLASS[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (!getValueAt(row, TYPE_INDEX).equals(XML.class) && column == XSD_INDEX)
            return false;
        return true;
    }
}
