/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.exceptionlistener.engine;

import com.fiorano.edbc.framework.service.internal.transport.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.exceptionlistener.Bundle;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.utils.RBUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created July 08, 2005
 */

public class MessageReceiver implements MessageListener, javax.jms.ExceptionListener {

    ExceptionListenerEngine engine;
    private Logger logger;
    private final HashSet<String> topicsSet;
    private String nodeName;

    public MessageReceiver(ExceptionListenerEngine engine, HashSet<String> set) {
        this.engine = engine;
        logger = engine.getLogger();
        topicsSet = set;
    }

    /**
     * Message Listener
     *
     * @param msg
     */
    public void onMessage(Message msg) {
        long inTimeMillis = System.currentTimeMillis();
        try {
            MessageUtil.makeMessageReadWrite(msg);
            msg.setStringProperty(Constants.COMPONENT_IN_TIME, String.valueOf(inTimeMillis));
            String text = ((TextMessage) msg).getText();

            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RECIEVED_MESSAGE, new Object[]{text}));
            msg.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTimeMillis));
            engine.getTransportManager().getOutTransport().send(msg);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_SENDING_MESSAGE, new Object[]{ex}));
            try {
                engine.getTransportManager().getErrorTransport().sendError(ServiceErrorID.TRANSPORT_ERROR.getName(), RBUtil.getMessage(Bundle.class, Bundle.ERROR_SENDING_MESSAGE, new Object[]{0}), ex, msg);
            } catch (Exception e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_EXCEP_ERROR), e);
            }
        } catch (Throwable e) {
            if (logger != null) {
                logger.log(Level.SEVERE, RBUtil.getMessage(com.fiorano.edbc.framework.service.internal.transport.jms.Bundle.class,
                        com.fiorano.edbc.framework.service.internal.transport.jms.Bundle.UNEXPECTED_ERROR), e);
            }
            if (e instanceof OutOfMemoryError && !engine.isInMemory())
                System.exit(101);
        }
    }

    /**
     * @param jmsException
     */
    public void onException(JMSException jmsException) {
        String errorStr = jmsException.toString();

        if (errorStr.contains("Topic Deleted")) {
            int startIndex = errorStr.indexOf("TopicName ::");
            int finalIndex = errorStr.indexOf(", ", startIndex);
            String topicName = errorStr.substring(startIndex + 13, finalIndex);
            synchronized (topicsSet) {
                if (topicsSet.contains(topicName)) {
                    topicsSet.remove(topicName);
                }
            }
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TOPIC_FORCIBLY_CLOSED, new Object[]{topicName}));
        } else if (errorStr.contains("KERNEL_IO_ERROR")) {
            synchronized (topicsSet) {
                topicsSet.clear();
            }
        } else
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_SERVER_CONNECTION_RESET, new Object[]{jmsException}));
    }

}