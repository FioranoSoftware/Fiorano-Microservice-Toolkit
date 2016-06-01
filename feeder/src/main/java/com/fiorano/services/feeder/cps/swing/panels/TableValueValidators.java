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
 * Time: 6:29:55 PM
 */
public class TableValueValidators {

    public static class IntValidator implements TableCellValidator {

        public void validate(Object value) throws Exception {
            Integer.parseInt((String) value);
        }

        public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
            validate(value);
        }
    }

    public static class FloatValidator implements TableCellValidator {

        public void validate(Object value) throws Exception {
            Float.parseFloat((String) value);
        }

        public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
            validate(value);
        }
    }

    public static class DoubleValidator implements TableCellValidator {

        public void validate(Object value) throws Exception {
            Double.parseDouble((String) value);
        }

        public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
            validate(value);
        }
    }

    public static class LongValidator implements TableCellValidator {

        public void validate(Object value) throws Exception {
            Long.parseLong((String) value);
        }

        public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
            validate(value);
        }
    }

    public static class BooleanValidator implements TableCellValidator {

        public void validate(Object value) throws Exception {
            Boolean.valueOf((String) value).booleanValue();
        }

        public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
            validate(value);
        }
    }

    public static class StringValidator implements TableCellValidator {

        public void validate(Object value) throws Exception {
            //
        }

        public void validate(JTable table, Object value, boolean isSelected, int row, int column) throws Exception {
            validate(value);
        }
    }

}
