/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.internal.transport.jms.JMSTransportProvider;
import com.fiorano.edbc.framework.service.jms.ports.AbstractInputPortHandler;
import com.fiorano.edbc.framework.service.jms.ports.OutputPortHandler;
import com.fiorano.esb.wrapper.InputPortInstanceAdapter;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.esb.wrapper.SessionConfiguration;
import com.fiorano.fw.error.FrameWorkException;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.*;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JMSHandler class holds the MessageProducer, MessageConsumer, InputPortHandler and session for every session
 * on the InputPort.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @created Thu, 22 Mar 2007
 */

public abstract class AbstractJMSHandler {

    /**
     * service configuration.
     */
    protected IServiceConfiguration serviceConfiguration;
    /**
     * Exception Handler
     */
    protected ServiceExceptionHandler exceptionHandler;
    /**
     * MessageProducer to send messages to a destination.
     */
    protected MessageProducer messageProducer;
    /**
     * Session for producing and consuming messages.
     */
    protected Session session;
    /**
     * Logger used for logging.
     */
    protected Logger logger;
    /**
     * Contains all the port properties of an input port.
     */
    protected AbstractInputPortHandler inputPortHandler;
    MessageConsumer messageConsumer;
    /**
     * Session index of an input port.
     */
    private int index = -1;
    private ServiceDetails serviceDetails;
    private HashMap<Destination, HashMap> filterMap = new HashMap<>();
    private int transactionSize = 1;
    private int messageCount = 0;

    /**
     * Creates a JMSHandler object which is used in creating the JMS objects like Session, MessageProducer,
     * MessageConsumer etc to start the service.
     *
     * @param inportHandler        InputPortHandler object
     * @param index                sessionIndex of an input port
     * @param connection           connection object
     * @param serviceConfiguration service configuration object
     * @param exceptionHandler     used to handle exceptions.
     * @throws JMSException    if the JMSProvider fails to create JMS objects due to some internal error
     * @throws NamingException if a naming exception is encountered
     */
    protected AbstractJMSHandler(AbstractInputPortHandler inportHandler,
                                 int index,
                                 Connection connection,
                                 IServiceConfiguration serviceConfiguration,
                                 ServiceExceptionHandler exceptionHandler) throws JMSException, NamingException {
        this.inputPortHandler = inportHandler;
        this.index = index;
        this.serviceConfiguration = serviceConfiguration;
        this.exceptionHandler = exceptionHandler;
        this.logger = inportHandler.getLogger();

        createObjects(connection);
        if ((inputPortHandler.getDestination() instanceof Queue) || ((inputPortHandler.getDestination() instanceof Topic) && (inportHandler.getSessionCountToUse() <= 1))) {
            MessageListener messageListener = createMessageListener(exceptionHandler);
            setMessageListener(messageListener);
        } else if (session != null) {
            MessageListener messageListener = createMessageListener(exceptionHandler);
            session.setMessageListener(messageListener);
        }

        if (inputPortHandler.getOutputPortHandlers() != null) {
            for (Object o : inportHandler.getOutputPortHandlers()) {
                OutputPortHandler portHandler = (OutputPortHandler) o;
                filterMap.put(portHandler.getDestination(), portHandler.getOutputPortInstanceAdapter().getMessageFilters());
            }
        }
    }

    /**
     * Creates a JMSHandler object which is used in creating the JMS objects like Session, MessageProducer,
     * MessageConsumer etc to start the service.
     *
     * @param inportHandler        InputPortHandler object
     * @param connection           connection object
     * @param serviceConfiguration service configuration object
     * @param exceptionHandler     used to handle exceptions.
     * @throws JMSException    if the JMSProvider fails to create JMS objects due to some internal error
     * @throws NamingException if a naming exception is encountered
     */
    protected AbstractJMSHandler(AbstractInputPortHandler inportHandler,
                                 Connection connection,
                                 IServiceConfiguration serviceConfiguration,
                                 ServiceExceptionHandler exceptionHandler) throws JMSException, NamingException {
        this.inputPortHandler = inportHandler;
        this.serviceConfiguration = serviceConfiguration;
        this.exceptionHandler = exceptionHandler;
        this.logger = inportHandler.getLogger();
    }

    public void setServiceDetails(ServiceDetails serviceDetails) {
        this.serviceDetails = serviceDetails;
    }

    protected abstract MessageListener createMessageListener(ServiceExceptionHandler exceptionHandler);

    protected ServiceExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Gets the inputporthandler object
     *
     * @return inputPortHandler
     */
    public AbstractInputPortHandler getInputPortHandler() {
        return inputPortHandler;
    }

    /**
     * Gets the service configuration.
     *
     * @return serviceConfiguration
     */
    public IServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    /**
     * This method creates a Session - a single-threaded context for sending and receiving messages on the connection.
     * It creates a MessageProducer for sending messages to a destination and a MessageConsumer to receive messages from a destination.
     * In case of durable subscription, a durable subscriber is created on the session so that if the client gets
     * disconnected from the Topic, after re-connecting, it can receive the messages that arrived while it was disconnected.
     *
     * @param connection connection object
     * @throws javax.jms.JMSException       if the JMSProvider fails to create JMS objects due to some internal error
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    protected void createObjects(Connection connection) throws JMSException, NamingException {

        if (connection == null) {
            throw new JMSException(RBUtil.getMessage(Bundle.class, Bundle.CONN_NOT_FOUND));
        }
        InputPortInstanceAdapter inputPortInstanceAdapter = inputPortHandler.getInputPortInstanceAdapter();
        createSession(connection);
        inputPortHandler.setSession(session);

        // producer does not have a specified destination
        messageProducer = session.createProducer(null);
        if (logger.isLoggable(Level.ALL)) {
            logger.log(Level.ALL, RBUtil.getMessage(Bundle.class, Bundle.PRODUCER_CREATED));
        }

        Destination receiveDest = inputPortHandler.getDestination();

        if ((receiveDest instanceof Queue) || (receiveDest instanceof Topic && inputPortHandler.getSessionCountToUse() <= 1)) {
            // Lookup destination for input port. The Destination should be a Topic

            if (inputPortInstanceAdapter.isDurableSubscription()) {
                if (!(receiveDest instanceof Topic)) {
                    logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_DURABLE_SUBSCRIPTION, new Object[]{receiveDest}));
                }
            }

            if (receiveDest instanceof Topic && inputPortInstanceAdapter.isDurableSubscription()) {
                String subscriptionName = inputPortInstanceAdapter.getSubscriptionName();
                if (subscriptionName == null) {
                    subscriptionName = inputPortInstanceAdapter.getName() + "_" + index;
                }
                messageConsumer = session.createDurableSubscriber((Topic) receiveDest, subscriptionName,
                        JMSTransportProvider.createSelectorFromFilters(inputPortInstanceAdapter.getMessageSelector(), inputPortInstanceAdapter.getMessageFilters()), false);
            } else {
                messageConsumer = session.createConsumer(receiveDest, JMSTransportProvider.createSelectorFromFilters(inputPortInstanceAdapter.getMessageSelector(), inputPortInstanceAdapter.getMessageFilters()), false);
            }
            if (logger.isLoggable(Level.ALL)) {
                logger.log(Level.ALL, RBUtil.getMessage(Bundle.class, Bundle.CONSUMER_CREATED,
                        new Object[]{inputPortInstanceAdapter.getName(),
                                new Integer(index)}));
            }
        }
    }

    /**
     * Sets the message listener on the message consumer.
     *
     * @throws JMSException
     */
    protected void setMessageListener(MessageListener messageListener) throws JMSException {
        if (messageConsumer != null)
            messageConsumer.setMessageListener(messageListener);
    }

    protected MessageProducer getMessageProducer() {
        return messageProducer;
    }

    protected int getTransactionSize() {
        return transactionSize;
    }

    protected int getMessageCount() {
        return messageCount;
    }

    /**
     * Gets the logger
     *
     * @return logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Creates a session on a connection.
     *
     * @param connection connection object
     * @throws javax.jms.JMSException if there is any exception in session creation.
     */
    protected void createSession(Connection connection) throws JMSException {
        SessionConfiguration sessionConfiguration = inputPortHandler.getInputPortInstanceAdapter().getSessionConfiguration();
        boolean transacted;
        int acknowledgeMode;
        if (sessionConfiguration.isTransacted()) {
            transacted = true;
            acknowledgeMode = Session.SESSION_TRANSACTED;
        } else {
            transacted = false;
            acknowledgeMode = sessionConfiguration.getAcknowledgementMode();
        }
        session = connection.createSession(transacted, acknowledgeMode);
        transactionSize = sessionConfiguration.getTransactionSize();
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage output message to be sent.
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void sendMessage(Message outputMessage) throws JMSException {
        if (inputPortHandler.getInputPortInstanceAdapter().requestReplySupported() && outputMessage.getJMSReplyTo() != null) {
            _sendMessage(outputMessage, outputMessage.getJMSReplyTo());
        } else {
            Collection outputPortHandlers = inputPortHandler.getOutputPortHandlers();
            for (Object outputPortHandler1 : outputPortHandlers) {
                OutputPortHandler outputPortHandler = (OutputPortHandler) outputPortHandler1;
                OutputPortInstanceAdapter outputPortInstanceAdapter = outputPortHandler.getOutputPortInstanceAdapter();
                _sendMessage(outputMessage, outputPortHandler.getDestination(), outputPortInstanceAdapter.getDeliveryMode(),
                        outputPortInstanceAdapter.getPriority(), outputPortInstanceAdapter.getTimeToLive());
            }
        }
        transactionComplete();

        if (shouldCommit()) {
            session.commit();
        }
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage   output message to be sent.
     * @param sendDestination destination on which the message should be sent
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void sendMessage(Message outputMessage, Destination sendDestination) throws JMSException {
        if (inputPortHandler.getInputPortInstanceAdapter().requestReplySupported() && outputMessage.getJMSReplyTo() != null) {
            _sendMessage(outputMessage, outputMessage.getJMSReplyTo());
        } else {
            _sendMessage(outputMessage, sendDestination);
            transactionComplete();
            if (shouldCommit()) {
                session.commit();
            }
        }
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage   output message to be sent.
     * @param sendDestination destination on which the message should be sent
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void sendMessage(Message outputMessage, Destination sendDestination, int deliveryMode, int priority, long ttl) throws JMSException {
        if (inputPortHandler.getInputPortInstanceAdapter().requestReplySupported() && outputMessage.getJMSReplyTo() != null) {
            _sendMessage(outputMessage, outputMessage.getJMSReplyTo());
        } else {
            _sendMessage(outputMessage, sendDestination, deliveryMode, priority, ttl);
            transactionComplete();
            if (shouldCommit()) {
                session.commit();
            }
        }
    }

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage  output message to be sent.
     * @param outputPortName name of the output port to send the message to
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    public void sendMessage(Message outputMessage, String outputPortName) throws JMSException {
        if (inputPortHandler.getInputPortInstanceAdapter().requestReplySupported() && outputMessage.getJMSReplyTo() != null) {
            _sendMessage(outputMessage, outputMessage.getJMSReplyTo());
        } else {
            Collection outputPortHandlers = inputPortHandler.getOutputPortHandlers();
            for (Object outputPortHandler1 : outputPortHandlers) {
                OutputPortHandler outputPortHandler = (OutputPortHandler) outputPortHandler1;
                if (outputPortHandler.getOutputPortInstanceAdapter().getName().equals(outputPortName)) {
                    OutputPortInstanceAdapter outputPortInstanceAdapter = outputPortHandler.getOutputPortInstanceAdapter();
                    _sendMessage(outputMessage, outputPortHandler.getDestination(), outputPortInstanceAdapter.getDeliveryMode(),
                            outputPortInstanceAdapter.getPriority(), outputPortInstanceAdapter.getTimeToLive());
                }
            }
        }
        transactionComplete();

        if (shouldCommit()) {
            session.commit();
        }
    }

    private void transactionComplete() {
        SessionConfiguration sessionConfiguration = inputPortHandler.getInputPortInstanceAdapter().getSessionConfiguration();
        int txnSize = sessionConfiguration.getTransactionSize() == 0 ? 1 : sessionConfiguration.getTransactionSize();
        messageCount = (messageCount + 1) % txnSize;
    }

    /**
     * Sends the exception encountered on to the default ON_EXCEPTION port
     *
     * @param message   input message
     * @param exception the actual exception to be sent
     */
    public void sendError(Message message, Exception exception) {
        if (exceptionHandler != null) {
            exceptionHandler.sendError(exception, message);
        } else if (inputPortHandler.getEventGenerator() != null && inputPortHandler.getEventSession() != null) {
            try {
                inputPortHandler.getEventGenerator().sendError(exception.getMessage(), exception, message, inputPortHandler.getEventSession().createTextMessage());
            } catch (FrameWorkException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_ERROR_FAILED, new Object[]{e.getMessage()}), e);
            } catch (JMSException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_ERROR_FAILED, new Object[]{e.getMessage()}), e);
            }
        }
    }

    /**
     * Returns whether the session should be committed.
     *
     * @return boolean whether the messageCount greater than transactionsize
     * @throws javax.jms.JMSException if there is any exception in getting the transacted property for the session.
     */
    private boolean shouldCommit() throws JMSException {
        return session.getTransacted() && messageCount == 0;
    }

    /**
     * Sends the message on to the specified destination.
     *
     * @param outputMessage   message to be sent.
     * @param sendDestination destination
     * @throws javax.jms.JMSException if there's any exception in sending the message.
     */
    private void _sendMessage(Message outputMessage, Destination sendDestination) throws JMSException {
        applyMessageFilters(outputMessage, sendDestination);
        messageProducer.send(sendDestination, outputMessage);
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

    private void _sendMessage(Message outputMessage, Destination sendDestination, int deliveryMode, int priority, long ttl)
            throws JMSException {
        applyMessageFilters(outputMessage, sendDestination);
        messageProducer.send(sendDestination, outputMessage, deliveryMode, priority, ttl);
    }

    /**
     * Gets the session object
     *
     * @return session
     */
    public Session getSession() {
        return session;
    }
}
