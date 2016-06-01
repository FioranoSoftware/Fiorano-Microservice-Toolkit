/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;


/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface IMessageSender {
    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage output message to be sent.
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    void send(Message outputMessage) throws JMSException;

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage   output message to be sent.
     * @param sendDestination destination on which the message should be sent
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    void send(Message outputMessage, Destination sendDestination) throws JMSException;

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage   output message to be sent.
     * @param sendDestination destination on which the message should be sent
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    void send(Message outputMessage, Destination sendDestination, int deliveryMode, int priority, long ttl) throws JMSException;

    /**
     * Sends specified message on sendDestination.
     *
     * @param outputMessage  output message to be sent.
     * @param outputPortName name of the output port to send the message to
     * @throws javax.jms.JMSException if there's any exception in sending the message or committing the transaction
     */
    void send(Message outputMessage, String outputPortName) throws JMSException;
}
