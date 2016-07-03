/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

import com.fiorano.openesb.microservice.ccp.event.ControlEvent;
import com.fiorano.services.common.util.RBUtil;

import javax.jms.BytesMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCCPEventGenerator implements ICCPEventGenerator {

    private MessageProducer producer;
    private Session session;
    private Logger logger;

    public AbstractCCPEventGenerator(MessageProducer producer, Session session, Logger logger) {
        this.producer = producer;
        this.session = session;
        this.logger = logger;
    }

    /**
     * Sends the event as a JMS message to the peer server. The event type and service ID
     * are set as properties on the message being sent.
     *
     * @param event The event that has to be sent to the peer server.
     */
    public void sendEvent(ControlEvent event) {
        if (producer == null || session == null) {
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.EVENT_GENERATOR_NOT_READY));
            return;
        }
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SENDING_EVENT, new Object[]{event}));
        try {
            BytesMessage message = session.createBytesMessage();
            event.toMessage(message);
            message.setStringProperty(ControlEvent.SOURCE_OBJECT, getComponentID());
            message.setStringProperty(ControlEvent.EVENT_TYPE_HEADER, event.getEventType().toString());
            producer.send(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SEND_CCP_EVENT, new Object[]{e.getMessage()}), e);
        }
    }

    void setSession(Session session) {
        this.session = session;
    }

    void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    protected abstract String getComponentID();
}
