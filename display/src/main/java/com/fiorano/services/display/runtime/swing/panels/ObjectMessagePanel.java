/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;

import com.fiorano.uif.util.TextEditor;
import fiorano.esb.utils.RBUtil;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel showing the JMS message
 *
 * @author FSIPL
 * @version 1.0
 * @created august 10, 2010
 */
public class ObjectMessagePanel extends JPanel implements IPanelLoader<ObjectMessage> {

    private TextEditor textEditor = new TextEditor(TextEditor.XML);
    private JPanel jPanel = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    private Logger logger;

    public ObjectMessagePanel(Logger logger) {
        this.logger = logger;
        try {
            jbInit();
            textEditor.setEditable(false);
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_OBJECT_MESSAGE_PANEL), ex);
        }
    }

    public void jbInit() throws Exception {

        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        jPanel.setLayout(borderLayout2);
        jPanel.setBorder(border);
        jPanel.add(textEditor, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(jPanel, BorderLayout.CENTER);
    }

    public void clear() {
        textEditor.setText("");
    }

    public void load(ObjectMessage objectMessage) throws JMSException {
        String str = "";
        try{
            Object object = objectMessage.getObject();
            if (object instanceof Collection) {
                for (Object o : ((Collection) object)) {
                    str += o.toString() + "\n";
                }
            } else {
                str += object.toString();
            }
        }catch(MessageFormatException ex){
            str=ex.getErrorCode();
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_OBJECT_MESSAGE_PANEL));
        }
        textEditor.setText(str);
    }

    public void saveMessage(ObjectMessage objectMessage, File file) throws IOException, JMSException {

        Object object;
        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream = null;
        try {
            object = objectMessage.getObject();
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
        } finally {
            if (objectOutputStream != null)
                objectOutputStream.close();
        }        
    }


}
