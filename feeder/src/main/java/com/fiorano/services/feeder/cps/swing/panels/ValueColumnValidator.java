/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.cps.swing.panels;

import javax.swing.*;
import java.util.Hashtable;

/**
 * Created by phani.
 * Date: Feb 25, 2008
 * Time: 6:33:50 PM
 */
public class ValueColumnValidator implements TableCellValidator {
    private Hashtable validatorMap = new Hashtable(5);

    public ValueColumnValidator() {
        validatorMap.put(FeederConstants.STRING_PROPERTY_TYPE, new TableValueValidators.StringValidator());
        validatorMap.put(FeederConstants.INT_PROPERTY_TYPE, new TableValueValidators.IntValidator());
        validatorMap.put(FeederConstants.FLOAT_PROPERTY_TYPE, new TableValueValidators.FloatValidator());
        validatorMap.put(FeederConstants.DOUBLE_PROPERTY_TYPE, new TableValueValidators.DoubleValidator());
    }

    public void validate(Object value) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
        if(table == null) {
            return;
        }
        TableCellValidator validatorToUse = (TableCellValidator) validatorMap.get(table.getValueAt(row, FeederConstants.TYPE_COLUMN));
        if(validatorToUse == null) {
            return;
        }
        validatorToUse.validate(table, value, isSelected, row, column);
    }
}
