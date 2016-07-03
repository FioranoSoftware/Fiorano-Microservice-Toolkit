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
import javax.jms.MapMessage;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel showing the JMS message
 *
 * @author FSIPL
 * @version 1.0
 * @created August 10, 2010
 */

public class MapMessagePanel extends JPanel implements IPanelLoader<MapMessage> {

    private CustomTableModel mapModel = new CustomTableModel("Name", "Type", "Value");
    private JTable mapTable = new TifosiTable(mapModel);
    private JScrollPane jScrollPaneMap = new JScrollPane();
    private JPanel mapPanel = new JPanel();
    private BorderLayout borderLayoutMap1 = new BorderLayout();
    private Logger logger;

    public MapMessagePanel(Logger logger) {
        this.logger = logger;
        try {
            jbInit();
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_MAP_MESSAGE_PANEL), ex);

        }
    }

    private void jbInit()
            throws Exception {

        jScrollPaneMap.getViewport().add(mapTable, null);
        Border borderMap = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        mapPanel.setLayout(borderLayoutMap1);
        mapPanel.setBorder(borderMap);
        mapPanel.add(jScrollPaneMap, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);
    }

    public void clear() {
        mapModel.setRowCount(0);
    }

    public void load(MapMessage mapMessage) throws JMSException {
        Enumeration e;
        String name, type;
        Object object;
        byte[] byteData;
        StringBuffer stringBuffer = new StringBuffer("");
        mapModel.setRowCount(0);
        e = mapMessage.getMapNames();

        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            try {
                if (mapMessage.itemExists(name)) {
                    object = mapMessage.getObject(name);
                    type = ClassUtil.getShortClassName(object.getClass());
                    if (object instanceof byte[]) {
                        byteData = (byte[]) object;
                        for (byte aByteData : byteData) {
                            stringBuffer.append(aByteData).append(" ");
                        }
                        mapModel.addRow(new Object[]{name, type, stringBuffer});
                    } else {
                        mapModel.addRow(new Object[]{name, type, object});
                    }
                }
            } catch (javax.jms.MessageFormatException ex) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_MESSAGE_FORMAT_LOAD_MAP_MESSAGE_PANEL), ex);
            }
        }
    }


    public void saveMessage(MapMessage mapMessage, File file) throws IOException, JMSException {

        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        String name;
        Object object;
        byte[] byteData;
        StringBuffer stringBuffer = new StringBuffer("");
        Enumeration enumeration;
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            enumeration = mapMessage.getMapNames();

            while (enumeration.hasMoreElements()) {
                name = (String) enumeration.nextElement();
                try {
                    if (mapMessage.itemExists(name)) {
                        object = mapMessage.getObject(name);
                        if (object instanceof byte[]) {
                            byteData = (byte[]) object;
                            for (byte aByteData : byteData) {
                                stringBuffer.append(aByteData).append(" ");
                            }
                            bufferedWriter.write(name + " = " + stringBuffer);
                            bufferedWriter.newLine();
                        } else {
                            bufferedWriter.write(name + " = " + mapMessage.getObject(name));
                            bufferedWriter.newLine();
                        }
                    }
                } catch (javax.jms.MessageFormatException ex) {
                    logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_MESSAGE_FORMAT_SAVE_MAP_MESSAGE_PANEL), ex);
                }
            }
        } finally {

            if (bufferedWriter != null)
                bufferedWriter.close();
            if (fileWriter != null)
                fileWriter.close();

        }

    }


}
