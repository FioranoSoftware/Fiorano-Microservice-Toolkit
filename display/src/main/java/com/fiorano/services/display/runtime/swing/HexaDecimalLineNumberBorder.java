/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing;

import com.fiorano.uif.util.LineNumberBorder;
import com.fiorano.uif.util.TextUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 20 Aug, 2010
 * Time: 5:22:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class HexaDecimalLineNumberBorder extends LineNumberBorder {
    protected String getLineNumber(int line) {
        return TextUtils.toHex(16 * (line - 1), 8) + "h";
    }

    public Insets getBorderInsets(Component c) {

        FontMetrics fm = c.getFontMetrics(c.getFont());
        int margin = fm.stringWidth(((JEditorPane) c).getDocument().getDefaultRootElement().getElementCount() + 10 + "");
        return new Insets(5, margin - 10, 5, 5);
    }
}
