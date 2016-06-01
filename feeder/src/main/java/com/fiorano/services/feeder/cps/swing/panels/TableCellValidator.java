/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.cps.swing.panels;

import javax.swing.*;

/**
 * Created by phani.
 * Date: Feb 25, 2008
 * Time: 6:29:19 PM
 */
public interface TableCellValidator {

    void validate(Object value) throws Exception;
    void validate(JTable table, Object value, boolean isSelected, int row, int column)
            throws Exception;

}
