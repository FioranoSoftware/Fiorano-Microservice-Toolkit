/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;


import com.fiorano.services.display.runtime.swing.CustomTableModel;
import com.fiorano.uif.ui.TifosiTable;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.utils.RBUtil;

import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.StreamMessage;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Panel showing the JMS message
 *
 * @author FSIPL
 * @version 1.0
 * @created August 13th, 2010
 */

public class StreamMessagePanel extends JPanel implements IPanelLoader<StreamMessage> {

    private CustomTableModel streamModel = new CustomTableModel("Type", "Value");
    private JTable streamTable = new TifosiTable(streamModel);
    private JScrollPane jScrollPaneStream = new JScrollPane();
    private JPanel streamPanel = new JPanel();
    private BorderLayout borderLayoutStream1 = new BorderLayout();
    private StreamMessage streamMsgSelected = null;
    Object object;

    public StreamMessagePanel(Logger logger) {
        try {
            jbInit();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_STREAM_MESSAGE_PANEL), ex);
        }

    }

    private void jbInit() throws Exception {


        jScrollPaneStream.getViewport().add(streamTable, null);
        Border borderStream = BorderFactory.createEmptyBorder(5, 5, 5, 5);


        streamPanel.setLayout(borderLayoutStream1);
        streamPanel.setBorder(borderStream);
        streamPanel.add(jScrollPaneStream, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(streamPanel, BorderLayout.CENTER);

    }

    public void clear() {

        streamModel.setRowCount(0);
    }

    public void load(StreamMessage streamMessage) throws JMSException {

        String type;
        byte[] byteData;
        StringBuffer stringBuffer = new StringBuffer("");
        streamModel.setRowCount(0);
        streamMsgSelected = streamMessage;
        streamMsgSelected.reset();
        try {
            while (true) {

                object = streamMsgSelected.readObject();
                type = ClassUtil.getShortClassName(object.getClass());
                if (object instanceof byte[]) {
                    byteData = (byte[]) object;
                    for (byte aByteData : byteData) {
                        stringBuffer.append(aByteData).append(" ");
                    }
                    streamModel.addRow(new Object[]{type, stringBuffer});
                } else {
                    streamModel.addRow(new Object[]{type, object});
                }
            }
        } catch (MessageEOFException exception) {
            // no need to handle exception here... as it occurs when stream end occurs
        }  

    }

    public void saveMessage(StreamMessage streamMessage, File file) throws IOException, JMSException {

        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(streamMsgSelected);
        } finally {
            if (objectOutputStream != null)
                objectOutputStream.close();
        }

    }


}
