/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.runtime.swing;

import javax.swing.table.DefaultTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 11 Aug, 2010
 */
public class CustomTableModel extends DefaultTableModel {

    public CustomTableModel(String column1, String column2) {
        setColumnIdentifiers(new String[]{column1, column2});
    }

    public CustomTableModel(String column1, String column2, String column3) {
        setColumnIdentifiers(new String[]{column1, column2, column3});
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
