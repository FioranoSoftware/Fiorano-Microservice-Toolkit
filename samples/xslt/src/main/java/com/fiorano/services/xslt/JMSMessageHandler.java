/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.xslt;

import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;
import fiorano.jms.services.msg.def.FioranoMessage;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.CharArrayWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Mar 31, 2009
 * Time: 4:38:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class JMSMessageHandler extends DefaultHandler {

    private String propertyName, propertyType, propertyValue, removePropertyName;
    private Message message;
    private CharArrayWriter contents = new CharArrayWriter();
    private Logger logger;

    /**
     * @param message
     */
    public JMSMessageHandler(Message message, Logger logger) {
        this.message = message;
        this.logger = logger;
    }

    /**
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     */
    public final void startElement(String uri, String localName, String qName, Attributes attributes) {
        contents.reset();
        if ("Property".equals(qName)) {
            propertyName = attributes.getValue("name");
            propertyType = attributes.getValue("type");
        } else if ("RemoveProperty".equals(qName)) {
            removePropertyName = attributes.getValue("name");
        }
    }

    /**
     * @param ch
     * @param start
     * @param length
     */
    public final void characters(char[] ch, int start, int length) {
        // accumulate the contents into a buffer.
        contents.write(ch, start, length);
    }

    /**
     * @param uri
     * @param localName
     * @param qName
     */
    public void endElement(String uri, String localName, String qName) {
        if ("Property".equals(qName)) {
            propertyValue = contents.toString();
            setProperty();
        } else if ("CorrelationID".equals(qName)) {
            String value = contents.toString();
            try {
                message.setJMSCorrelationID(value);
            } catch (JMSException e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_SET_ID, new Object[]{value}));
            }
        } else if ("Text".equals(qName)) {
            String value = contents.toString();
            try {
                MessageUtil.setTextData(message, value);
            } catch (JMSException e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_SET_ID, new Object[]{value}));
            }
        } else if ("RemoveProperty".equals(qName)) {
            if (message instanceof FioranoMessage) {
                try {
                    ((FioranoMessage) message).removeProperty(removePropertyName);
                } catch (Exception e) {
                    logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_SET_ID, new Object[]{removePropertyName}));
                }
            }
        }
        contents.reset();
    }

    private void setProperty() {
        Object value = propertyValue;

        if ("Byte".equals(propertyType)) {
            value = new Byte(propertyValue);
        } else if ("Short".equals(propertyType)) {
            value = new Short(propertyValue);
        } else if ("Integer".equals(propertyType)) {
            value = new Integer(propertyValue);
        } else if ("Long".equals(propertyType)) {
            value = new Long(propertyValue);
        } else if ("Float".equals(propertyType)) {
            value = new Float(propertyValue);
        } else if ("Double".equals(propertyType)) {
            value = new Double(propertyValue);
        } else if ("Boolean".equals(propertyType)) {
            value = Boolean.valueOf(propertyValue);
        }

        try {
            MessageUtil.setProperty(propertyName, value, message);
        } catch (JMSException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_SET_PROP, new Object[]{propertyName, value}));
        }
        propertyName = propertyValue = propertyType = null;
    }
}
