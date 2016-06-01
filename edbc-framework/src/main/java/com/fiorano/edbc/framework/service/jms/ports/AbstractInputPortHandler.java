/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms.ports;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.transport.jms.JMSTransportProvider;
import com.fiorano.edbc.framework.service.jms.AbstractJMSHandler;
import com.fiorano.edbc.framework.service.jms.Bundle;
import com.fiorano.edbc.framework.service.jms.ServiceExceptionHandler;
import com.fiorano.esb.wrapper.InputPortInstanceAdapter;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.transaction.api.IResourceManager;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.pubsub.ServerSessionPool;
import com.fiorano.services.libraries.jms.pubsub.StaticSessionProvider;
import fiorano.esb.util.EventGenerator;
import fiorano.esb.util.LoggerUtil;

import javax.jms.*;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler which holds reference to input port and different JMSHandlers based on
 * session configuration details on input ports
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @created Thu, 22 Mar 2007
 */
public abstract class AbstractInputPortHandler {

    /**
     * Logger used for logging.
     */
    private Logger logger;
    /**
     * Destination for input port.
     */
    private Destination destination;
    /**
     * InputPortInstanceAdapter which contains the input port properties.
     */
    private InputPortInstanceAdapter inputPortInstanceAdapter;
    /**
     * Contains key-value pairs of output port name and OutputPortHandler.
     */
    private Collection outputPortHandlers;
    /**
     * Used for sending exception messages and events
     */
    private EventGenerator eventGenerator;
    /**
     * Used for creating messages for sending exceptions and events
     */
    private Session eventSession;
    /**
     * Array of JMSHandler objects for an input port.
     */
    private AbstractJMSHandler[] jmsHandlers;
    private IResourceManager resourceManager;
    /**
     * Session object
     */
    private Session session;

    /**
     * Creates an InputPortHandler object.
     *
     * @param destination              destination object
     * @param inputPortInstanceAdapter input port instance adapter
     * @param outputPortHandlers       collection of output port names and their corresponding OutputPortHandlers
     */
    public AbstractInputPortHandler(Destination destination, InputPortInstanceAdapter inputPortInstanceAdapter, Collection outputPortHandlers) {
        this.destination = destination;
        this.inputPortInstanceAdapter = inputPortInstanceAdapter;
        this.outputPortHandlers = outputPortHandlers != null ? outputPortHandlers : Collections.EMPTY_LIST;
        this.logger = Logger.getLogger(this.getClass().getName());
        LoggerUtil.addFioranoConsoleHandler(this.logger);
    }

    /**
     * Creates an InputPortHandler object.
     *
     * @param destination              destination object
     * @param inputPortInstanceAdapter input port instance adapter
     * @param outputPortHandlers       collection of output port names and their corresponding OutputPortHandlers
     * @param eventGenerator           send exception messages and events while processing
     * @param eventSession             session to be used for sending exceptions and events
     */
    public AbstractInputPortHandler(Destination destination,
                                    InputPortInstanceAdapter inputPortInstanceAdapter,
                                    Collection outputPortHandlers,
                                    EventGenerator eventGenerator,
                                    Session eventSession) {
        this.destination = destination;
        this.inputPortInstanceAdapter = inputPortInstanceAdapter;
        this.outputPortHandlers = outputPortHandlers != null ? outputPortHandlers : Collections.EMPTY_LIST;
        this.eventGenerator = eventGenerator;
        this.eventSession = eventSession;
        this.logger = Logger.getLogger(this.getClass().getName());
        LoggerUtil.addFioranoConsoleHandler(this.logger);
    }

    /**
     * This method creates JMSHandler for every session on an input port
     *
     * @param connection           connection object
     * @param serviceConfiguration configuration object
     * @param exceptionHandler     used in handling exceptions.
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException if creation of jms handlers fails
     */
    public void createJMSHandlers(Connection connection, IServiceConfiguration serviceConfiguration,
                                  ServiceExceptionHandler exceptionHandler, ServiceDetails serviceDetails) throws ServiceExecutionException {

        jmsHandlers = new AbstractJMSHandler[getSessionCountToUse()];
        for (int sessionIndex = 0; sessionIndex < jmsHandlers.length; sessionIndex++) {
            jmsHandlers[sessionIndex] = createJMSHandler(sessionIndex, connection, serviceConfiguration, exceptionHandler);
            jmsHandlers[sessionIndex].setServiceDetails(serviceDetails);
        }
        if (!(destination instanceof Queue) && getSessionCountToUse() > 1) {
            Session[] sessions = new Session[getSessionCountToUse()];
            int i = 0;
            for (AbstractJMSHandler jmsHandler : jmsHandlers) {
                sessions[i++] = jmsHandler.getSession();
            }

            ServerSessionPool sessionPool = new ServerSessionPool(new StaticSessionProvider(sessions), getSessionCountToUse());

            if (inputPortInstanceAdapter.isDurableSubscription()) {
                if (!(destination instanceof Topic)) {
                    logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_DURABLE_SUBSCRIPTION, new Object[]{destination}));
                }
            }
            try {
                if (inputPortInstanceAdapter.isDurableSubscription()) {
                    String subscriptionName = inputPortInstanceAdapter.getSubscriptionName();
                    if (subscriptionName == null) {
                        subscriptionName = inputPortInstanceAdapter.getName();
                    }

                    connection.createDurableConnectionConsumer((Topic) destination, subscriptionName,
                            JMSTransportProvider.createSelectorFromFilters(inputPortInstanceAdapter.getMessageSelector(), inputPortInstanceAdapter.getMessageFilters()), sessionPool, 1);

                } else {
                    connection.createConnectionConsumer((Topic) destination,
                            JMSTransportProvider.createSelectorFromFilters(inputPortInstanceAdapter.getMessageSelector(), inputPortInstanceAdapter.getMessageFilters()), sessionPool, 1);
                }
            } catch (JMSException e) {
                if (getLogger().isLoggable(Level.SEVERE)) {
                    getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_OBJECT_CREATION_FAILED,
                            new Object[]{e.getMessage()}));
                }
                throw new ServiceExecutionException(e, ServiceErrorID.TRANSPORT_ERROR);
            }

            if (logger.isLoggable(Level.ALL)) {
                logger.log(Level.ALL, RBUtil.getMessage(Bundle.class, Bundle.CONSUMER_CREATED,
                        new Object[]{inputPortInstanceAdapter.getName(),
                                "1 - " + getSessionCountToUse()}));
            }
        }
    }

    /**
     * Number of sessions that have to be created for processing messages on input port {@code getInputPortInstanceAdapter().getName()}.
     * Override this method if session count other than the default value (number of sessions mentioned in input port) has to be used
     *
     * @return number of sessions configured on input port
     */
    public int getSessionCountToUse() {
        return getInputPortInstanceAdapter().getSessionConfiguration().getCount();
    }

    protected abstract AbstractJMSHandler createJMSHandler(int sessionIndex, Connection connection, IServiceConfiguration serviceConfiguration,
                                                           ServiceExceptionHandler exceptionHandler) throws ServiceExecutionException;

    /**
     * This method returns an array of JMSHandlers
     *
     * @return jmsHandlers
     */
    public AbstractJMSHandler[] getJmsHandlers() {
        return jmsHandlers;
    }

    /**
     * Gets the Logger.
     *
     * @return logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Sets the logger.
     *
     * @param logger java logger to be used
     */
    public void setLogger(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    /**
     * Gets the session
     *
     * @return session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Sets the session
     *
     * @param session JMS session for handling input port
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Returns the inputPort InstanceAdapter
     *
     * @return inputPortInstanceAdapter
     */
    public InputPortInstanceAdapter getInputPortInstanceAdapter() {
        return inputPortInstanceAdapter;
    }

    /**
     * Sets inputPort InstanceAdapter
     *
     * @param inputPortInstanceAdapter the actual port instance adapter
     */
    public void setInputPortInstanceAdapter(InputPortInstanceAdapter inputPortInstanceAdapter) {
        this.inputPortInstanceAdapter = inputPortInstanceAdapter;
    }

    /**
     * Gets the OutputPortHandlers.
     *
     * @return outputPortHandlers
     */
    public Collection getOutputPortHandlers() {
        return outputPortHandlers;
    }

    /**
     * Gets the destination object
     *
     * @return destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Sets the destination
     *
     * @param destination the jms destination of the input port
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * Gets the event generator object
     *
     * @return event generator
     */
    public EventGenerator getEventGenerator() {
        return eventGenerator;
    }

    /**
     * Gets the session used for sending exceptions and events
     *
     * @return jms session to be used for sending messages
     */
    public Session getEventSession() {
        return eventSession;
    }

    public IResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public AbstractJMSHandler createJMSMessageHandler(int sessionIndex, Connection connection, IServiceConfiguration serviceConfiguration,
                                                      ServiceExceptionHandler exceptionHandler) throws ServiceExecutionException {
        return createJMSHandler(sessionIndex, connection, serviceConfiguration, exceptionHandler);
    }

}
