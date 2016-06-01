/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.engine;


import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.bc.feeder.ps.panels.Attachment;
import com.fiorano.bc.feeder.ps.panels.Header;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.feeder.cps.swing.panels.FeederConstants;
import fiorano.esb.util.MessagePropertyNames;
import fiorano.esb.utils.RBUtil;
import fiorano.jms.services.msg.def.FioranoMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Nov 19, 2007
 * Time: 2:21:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeederMessage {
    private TextMessage defaultDoc = null;
    private FeederEngine engine;
    private Logger logger;
    private Session session;

    public FeederMessage(FeederEngine engine) {
        this.engine = engine;
        logger = engine.getLogger();
        session = engine.getTransportManager().getFeederTransport().getSession();
    }

    public TextMessage getDefaultDoc() {
        return defaultDoc;
    }

    public void setDefaultDoc(TextMessage defaultDoc) {
        this.defaultDoc = defaultDoc;
    }

    public void sendMessage(Message message) throws JMSException, ServiceExecutionException {

        Message document = null;
        try {
            document = (Message) ((FioranoMessage) message).clone();
        } catch (Exception e) {}

            engine.getTransportManager().getFeederTransport().send(document != null ? document : message);
            engine.getTransportManager().getFeederTransport().commit();
    }

    public void setMessage(FeederPM configuration) throws ServiceExecutionException {
        String modelString = configuration.getDefaultMessage();
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class,Bundle.MODEL_STRING), new Object[]{modelString});
        }

        Map headerTable = configuration.getHeader();
        Map attachTable = configuration.getAttachment();
        if (modelString != null) {
            try {
                defaultDoc = session.createTextMessage();
                defaultDoc.setText(modelString);
                setAllProperties(defaultDoc, headerTable);
                setAttachments(defaultDoc, attachTable);
            }
            catch (JMSException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class,Bundle.EXCEP_CREATING_COMP), e);
                engine.stop();
            }
        }
    }

    private void setAllProperties(Message jmsMessage, Map propertyTable) throws JMSException {
        if (jmsMessage == null || propertyTable == null) {
            return;
        }
        Iterator iter = propertyTable.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Header header = (Header) entry.getValue();
            Object name = header.getName();
            Object value = header.getValue();
            Object type = header.getType();
            if (name instanceof String) {
                setProperty((String) name, (String) type, value, jmsMessage);
            }
        }
    }

    private void setProperty(String name, String type, Object value, Message jmsMessage) throws JMSException {
        if (jmsMessage == null || name == null) {
            return;
        }
        if (FeederConstants.DOUBLE_PROPERTY_TYPE.equals(type)) {
            double val = Double.parseDouble((String) value);
            jmsMessage.setDoubleProperty(name, val);
        } else if (FeederConstants.FLOAT_PROPERTY_TYPE.equals(type)) {
            float val = Float.parseFloat((String) value);
            jmsMessage.setFloatProperty(name, val);
        } else if (FeederConstants.INT_PROPERTY_TYPE.equals(type)) {
            int val = Integer.parseInt((String) value);
            jmsMessage.setIntProperty(name, val);
        } else if (FeederConstants.LONG_PROPERTY_TYPE.equals(type)) {
            long val = Long.parseLong((String) value);
            jmsMessage.setLongProperty(name, val);
        } else if (FeederConstants.STRING_PROPERTY_TYPE.equals(type)) {
            jmsMessage.setStringProperty(name, (String) value);
        } else {
            jmsMessage.setObjectProperty(name, value);
        }
    }

    private void setAttachments(Message message, Map attachTable) throws JMSException {
        Hashtable aTable = new Hashtable();
        if (message == null || attachTable == null) {
            return;
        }
        Iterator iter = attachTable.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Attachment attachment = (Attachment) entry.getValue();
            Object value = attachment.fetchValue();
            Object name = attachment.getName();
            aTable.put(name, value);
        }
        message.setObjectProperty(MessagePropertyNames.ATTACHMENT_TABLE, aTable);
    }
}
