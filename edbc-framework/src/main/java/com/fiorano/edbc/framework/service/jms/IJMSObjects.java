/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import fiorano.esb.util.EventGenerator;

import javax.jms.Connection;
import javax.jms.Session;

/**
 * <code>IJMSObjects</code> is an interface that should be implemented by class which creates
 * all JMS related objects.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface IJMSObjects {

    /**
     * This method does the connection factory lookup and creates the connection.
     * It also does the outputports lookup and creates the JMSHandler.
     *
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException if there's any exception in the jmsObjects creation.
     */
    void create() throws ServiceExecutionException;

    /**
     * Starts the connection. After this method is called the service should ready to process any incoming requests.
     *
     * @throws ServiceExecutionException
     */
    void start() throws ServiceExecutionException;

    /**
     * Stops the connection. After this method the transport is completely stopped and the service cannot send
     * or receive any more messages.
     *
     * @throws ServiceExecutionException
     */
    void stop() throws ServiceExecutionException;

    /**
     * Closes the connection and destroys the jmsObjects.
     */
    void destroy() throws ServiceExecutionException;

    /**
     * Gets the event generator.
     *
     * @return eventGenerator
     */
    EventGenerator getEventGenerator();

    /**
     * Gets the event session. Session which can be used to create JMS objects required for
     * sending errors on exception port or raising events
     *
     * @return session JMS session for sned errors
     */
    Session getEventSession();

    /**
     * Returns exception handler which can be used to handle eceptions that may be arised
     * during service launch or execution.
     *
     * @return exception handler for taking configured actions when exception occurs
     */
    ServiceExceptionHandler getExceptionHandler();

    /**
     * Returns the JMS connection.
     *
     * @return
     */
    Connection getConnection();

    /**
     * All the message consumers created for the service are stopped. No new requests are accepted after
     * returning from this method. However, already consumed request will be processed completely when this
     * method returns
     */
    void stopProcessing();

}
