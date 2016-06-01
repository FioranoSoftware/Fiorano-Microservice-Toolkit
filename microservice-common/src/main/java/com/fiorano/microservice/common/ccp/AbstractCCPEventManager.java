/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

import com.fiorano.openesb.microservice.ccp.event.ControlEvent;
import com.fiorano.services.common.util.RBUtil;

import javax.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCCPEventManager implements ICCPEventManager {

    protected static final String P2C_TOPIC = "CCP_PEER_TO_COMPONENT_TRANSPORT";
    protected static final String C2P_TOPIC = "CCP_COMPONENT_TO_PEER_TRANSPORT";

    protected ICCPEventHandler ccpEventHandler;
    protected ICCPEventGenerator ccpEventGenerator;
    protected Logger logger;
    protected MessageConsumer consumer;
    protected MessageProducer producer;
    protected Session session;

    /**
     * Starts the event manager. creates the JMS Objects required for communication with the peer server.
     * creates CCPEventHandler and CCPEventGenerator and assigns the requisite producer and consumer objects.
     *
     * @throws Exception if there is an error while creating the objects.
     * @link com.fiorano.edbc.framework.service.ccp.jms.CCPEventGenerator
     * @link com.fiorano.edbc.framework.service.ccp.jms.CCPEventHandler
     */
    public void start() throws Exception {
        try {
            session = createSession();
            String componentID = getComponentID();
            Destination p2cTopic = session.createTopic(P2C_TOPIC);
            consumer = session.createConsumer(p2cTopic, ControlEvent.TARGET_OBJECTS + " LIKE '" + componentID + ";'");
            Destination c2pTopic = session.createTopic(C2P_TOPIC);
            producer = session.createProducer(c2pTopic);
            ccpEventGenerator = createCCPEventGenerator();
            ccpEventHandler = createCCPEventHandler();
            consumer.setMessageListener((AbstractCCPEventHandler) ccpEventHandler);
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.CCP_EVENT_MANAGER_START_COMPLETE));
        } catch (JMSException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.CCP_EVENT_MANAGER_START_FAILED,
                    new Object[]{e.getMessage()}), e);
            stop();
            throw new Exception("SERVICE_LAUNCH_ERROR", e);
        }
    }

    /**
     * Performs cleanup of the objects created for communication with the peer server.
     * The consumer created to listen to messsages on peer is closed.
     * Cleanup of CCPEventHandler is performed.
     *
     * @link com.fiorano.edbc.framework.service.ccp.jms.CCPEventGenerator
     * @link com.fiorano.edbc.framework.service.ccp.jms.CCPEventHandler
     */
    public void stop() throws Exception {
        if (consumer != null) {
            try {
                consumer.setMessageListener(null);
                consumer.close();
            } catch (JMSException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, RBUtil.getMessage(Bundle.class,
                        Bundle.CCP_EVENT_HANDLER_STOP_FAILED, new String[]{e.getMessage()})));
            }
        }
        if (producer != null) {
            try {
                producer.close();
                producer = null;
            } catch (JMSException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, RBUtil.getMessage(Bundle.class,
                        Bundle.CCP_EVENT_HANDLER_STOP_FAILED, new String[]{e.getMessage()})));
            }
        }
        if (ccpEventHandler != null) {
            ccpEventHandler.stop();
        }
        ccpEventGenerator = null;
        ccpEventHandler = null;
        session.close();
    }

    /**
     * Return the ccpEventGenerator used by this object. Note that the returned object will be null
     * if the start is not called by the time this method is invoked.
     *
     * @return
     */
    public ICCPEventGenerator getCCPEventGenerator() {
        return ccpEventGenerator;
    }

    /**
     * Sets the CCPEventGenerator that will be used to send the events to the peer server.
     *
     * @param ccpEventGenerator used to send events to the peer server.
     */
    public void setCCPEventGenerator(ICCPEventGenerator ccpEventGenerator) {
        this.ccpEventGenerator = ccpEventGenerator;
    }

    /**
     * Return the ccpEventHandler used by this object. Note that the returned object will be null
     * if the start is not called by the time this method is invoked.
     *
     * @return ICCPEventHandler
     */
    public ICCPEventHandler getCCPEventHandler() {
        return ccpEventHandler;
    }

    /**
     * Sets the CCPEventHandler that will be used to handle the events received from the peer server.
     *
     * @param ccpEventHandler used to handle the events from peer server.
     */
    public void setCCPEventHandler(ICCPEventHandler ccpEventHandler) {
        this.ccpEventHandler = ccpEventHandler;
    }

    protected abstract String getComponentID();

    protected abstract ICCPEventGenerator createCCPEventGenerator();

    protected abstract ICCPEventHandler createCCPEventHandler() throws JMSException;

    protected abstract Session createSession() throws JMSException;
}
