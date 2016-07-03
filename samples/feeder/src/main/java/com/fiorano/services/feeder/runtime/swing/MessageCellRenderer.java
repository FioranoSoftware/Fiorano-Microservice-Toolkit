/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.runtime.swing;

import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Nov 20, 2007
 * Time: 2:35:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String text = null;
        if (value != null) {
            try {
                text = value instanceof Message ? MessageUtil.getTextData((Message) value) : value.toString();
            } catch (JMSException e) {
                text = value.toString();
            }
        }
        setText(text);
        setToolTipText(text);
        return component;
    }

}
