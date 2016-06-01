/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;

import com.fiorano.services.display.runtime.swing.HexaDecimalLineNumberBorder;
import com.fiorano.uif.ui.BytesEditorPane;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.utils.RBUtil;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel showing the JMS message
 *
 * @author FSIPL
 * @version 1.0
 * @created August 10, 2010
 */

public class BytesMessagePanel extends JPanel implements IPanelLoader<BytesMessage> {
    private JScrollPane jScrollPane = new JScrollPane();
    private BytesEditorPane bytesEditor = new BytesEditorPane(jScrollPane);
    private JPanel jPanel = new JPanel();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private BorderLayout borderLayout = new BorderLayout();
    private BorderLayout borderLayout1 = new BorderLayout();
    private GridLayout gridLayout = new GridLayout();
    final HexaDecimalLineNumberBorder hexaLineBorder = new HexaDecimalLineNumberBorder();

    private Logger logger;

    public BytesMessagePanel(Logger logger) {
        this.logger = logger;
        try {
            jbInit();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_BYTES_MESSAGE_PANEL), ex);
        }
    }

    public void jbInit() throws Exception {

        jPanel1.setLayout(gridLayout);
        jPanel2.setLayout(borderLayout1);
        jPanel2.add(jPanel1, BorderLayout.EAST);
        Border borderByte = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        jPanel.setLayout(borderLayout);
        jPanel.setBorder(borderByte);
        jPanel.add(jScrollPane, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(jPanel, BorderLayout.CENTER);

        JLabel lbl = new JLabel();
        lbl.setFont(new java.awt.Font("Monospaced", 0, 12));
        lbl.setForeground(Color.blue);
        lbl.setText(" 0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f");
        lbl.setBorder(
                new Border() {
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        g.setColor(Color.gray);
                        x += hexaLineBorder.getBorderInsets(bytesEditor).left - 5;
                        g.drawLine(x, y + height - 1, x + width, y + height - 1);
                    }

                    public Insets getBorderInsets(Component c) {
                        return new Insets(0, hexaLineBorder.getBorderInsets(bytesEditor).left, 0, 0);
                    }

                    public boolean isBorderOpaque() {
                        return true;
                    }
                });
        bytesEditor.setBackground(new Color(204, 204, 214));
        bytesEditor.setFont(new java.awt.Font("Monospaced", 0, 12));
        jScrollPane.getViewport().setView(bytesEditor);
        jScrollPane.setColumnHeaderView(lbl);
        bytesEditor.setBorder(hexaLineBorder);

    }

    public void clear() throws Exception {
        bytesEditor.showBytes(null);
    }

    public void load(BytesMessage bytesMessage) throws Exception {
        bytesMessage.reset();
        byte[] bytes = MessageUtil.getBytesData(bytesMessage);
        bytesEditor.showBytes(bytes);
    }

    public void saveMessage(BytesMessage bytesMessage, File file) throws IOException, JMSException {
        FileOutputStream fileOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(MessageUtil.getBytesData(bytesMessage));
            byteArrayOutputStream.writeTo(fileOutputStream);
        } finally {
            if (fileOutputStream != null)
                fileOutputStream.close();
            if (byteArrayOutputStream != null)
                byteArrayOutputStream.close();
        }

    }


}
