/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.jms.AbstractJMSHandler;
import com.fiorano.edbc.framework.service.jms.ServiceExceptionHandler;
import com.fiorano.services.xslt.ports.InputPortHandler;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.NamingException;

/**
 * JMSHandler class holds the MessageProducer, MessageConsumer, InputPortHandler and session for every session
 * on the InputPort.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @created Thu, 22 Mar 2007
 */

public class JMSHandler extends AbstractJMSHandler {

    private XsltMessageListener messageListener;

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
    public JMSHandler(InputPortHandler inportHandler, int index, Connection connection,
                      IServiceConfiguration serviceConfiguration, ServiceExceptionHandler exceptionHandler) throws JMSException, NamingException {
        super(inportHandler, index, connection, serviceConfiguration, exceptionHandler);
    }


    protected MessageListener createMessageListener(ServiceExceptionHandler serviceExceptionHandler) {
        return messageListener = new XsltMessageListener(this, serviceExceptionHandler);
    }

    public void stop() {
        messageListener.stop();
    }

    @Override
    public void createSession(Connection connection) throws JMSException {
        if (serviceConfiguration.isBatchMode()) {
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        } else {
            super.createSession(connection);
        }
    }
}

