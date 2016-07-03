/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.jms.ports.OutputPortHandler;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.esb.wrapper.SessionConfiguration;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class MessageSender implements IMessageSender {
    private Session session;
    private MessageProducer producer;
    private SessionConfiguration sessionConfiguration;
    private Map outputPorts;
    private int currentTxnSize = 0;
    private ServiceDetails serviceDetails;
    private HashMap<Destination, HashMap> filterMap = new HashMap<>();

    public MessageSender(Session session, SessionConfiguration sessionConfiguration, Map outputPorts) throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.SESSION_NULL));
        }
        this.session = session;
        this.producer = session.createProducer(null);
        this.outputPorts = outputPorts;
        this.sessionConfiguration = sessionConfiguration;
        if (outputPorts != null) {
            for (Object o : outputPorts.values()) {
                OutputPortHandler portHandler = (OutputPortHandler) o;
                filterMap.put(portHandler.getDestination(), portHandler.getOutputPortInstanceAdapter().getMessageFilters());
            }
        }
    }

    public MessageSender(Session session, SessionConfiguration sessionConfiguration) throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.SESSION_NULL));
        }
        this.session = session;
        this.producer = session.createProducer(null);
        this.sessionConfiguration = sessionConfiguration;
    }

    public void setServiceDetails(ServiceDetails serviceDetails) {
        this.serviceDetails = serviceDetails;
    }

    public Session getSession() {
        return session;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public SessionConfiguration getSessionConfiguration() {
        return sessionConfiguration;
    }

    public Map getOutputPorts() {
        return outputPorts;
    }

    public void send(Message outputMessage) throws JMSException {
        if (outputPorts == null) {
            return;
        }
        Collection<OutputPortHandler> outputPortHandlers = outputPorts.values();
        for (OutputPortHandler outputPortHandler : outputPortHandlers) {
            OutputPortInstanceAdapter outputPortInstanceAdapter = outputPortHandler.getOutputPortInstanceAdapter();
            _send(outputMessage, outputPortHandler.getDestination(), outputPortInstanceAdapter.getDeliveryMode(),
                    outputPortInstanceAdapter.getPriority(), outputPortInstanceAdapter.getTimeToLive());
        }
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage   output message to be sent.
     * @param sendDestination destination on which the message should be sent
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void send(Message outputMessage, Destination sendDestination) throws JMSException {
        _send(outputMessage, sendDestination);
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage   output message to be sent.
     * @param sendDestination destination on which the message should be sent
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void send(Message outputMessage, Destination sendDestination, int deliveryMode, int priority, long ttl) throws JMSException {
        _send(outputMessage, sendDestination, deliveryMode, priority, ttl);
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage  output message to be sent.
     * @param outputPortName name of the output port to send the message to
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void send(Message outputMessage, String outputPortName) throws JMSException {
        OutputPortHandler outputPortHandler = (OutputPortHandler) outputPorts.get(outputPortName);
        OutputPortInstanceAdapter outputPortInstanceAdapter = outputPortHandler.getOutputPortInstanceAdapter();
        _send(outputMessage, outputPortHandler.getDestination(), outputPortInstanceAdapter.getDeliveryMode(),
                outputPortInstanceAdapter.getPriority(), outputPortInstanceAdapter.getTimeToLive());
    }

    /**
     * Returns whether the session should be committed.
     *
     * @return boolean whether the currentTxnSize greater than transactionsize
     * @throws javax.jms.JMSException if there is any exception in getting the transacted property for the session.
     */
    protected boolean shouldCommit() throws JMSException {
        return session.getTransacted() && currentTxnSize == 0;
    }

    public void transactionComplete() throws JMSException {
        if (session.getTransacted()) {
            int txnSize = sessionConfiguration == null || sessionConfiguration.getTransactionSize() == 0
                    ? 1
                    : sessionConfiguration.getTransactionSize();
            currentTxnSize = (currentTxnSize + 1) % txnSize;
            if (shouldCommit()) {
                session.commit();
            }
        }
    }

    /**
     * Sends the message on to the specified destination.
     *
     * @param outputMessage   message to be sent.
     * @param sendDestination destination
     * @throws javax.jms.JMSException if there's any exception in sending the message.
     */
    private void _send(Message outputMessage, Destination sendDestination) throws JMSException {
        applyMessageFilters(outputMessage, sendDestination);
        producer.send(sendDestination, outputMessage);
    }

    private void _send(Message outputMessage, Destination sendDestination, int deliveryMode, int priority, long ttl)
            throws JMSException {
        applyMessageFilters(outputMessage, sendDestination);
        producer.send(sendDestination, outputMessage, deliveryMode, priority, ttl);
    }

    private void applyMessageFilters(Message message, Destination destination) throws JMSException {
        //set service instance properties
        if (serviceDetails != null) {
            MessageUtil.setCompInstName(message, serviceDetails.getServiceName());
            MessageUtil.setEventProcessName(message, serviceDetails.getApplicationName());
            MessageUtil.setEventProcessVersion(message, String.valueOf(serviceDetails.getApplicationVersion()));
        }
        //set message filters as properties
        HashMap filters = filterMap.get(destination);
        if (filters != null) {
            for (Object key : filters.keySet()) {
                message.setStringProperty((String) key, (String) filters.get(key));
            }
        }
    }

}
