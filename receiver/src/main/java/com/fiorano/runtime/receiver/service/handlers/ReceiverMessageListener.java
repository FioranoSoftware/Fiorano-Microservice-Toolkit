/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.runtime.receiver.service.handlers;

import com.fiorano.runtime.receiver.service.Bundle;
import com.fiorano.runtime.receiver.service.ReceiverService;

import fiorano.esb.utils.RBUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Message Listener class
 *
 * @author FSIPL
 * @version 1.0
 * @created May 20, 2005
 */
public class ReceiverMessageListener
        implements MessageListener {
    
    private ReceiverService receiverService;
    private Session session;
    // Current message count for individual message listener.
    private long mesCount = 0;
    private long startTime = -1;
    private Logger logger;

    /**
     * @param owner service object
     * @param session session
     */
    public ReceiverMessageListener(ReceiverService owner, Session session) {
        receiverService = owner;

        this.session = session;
        logger = receiverService.getLogger();
    }

    /**
     * Returns rate for object
     */
    public float getRate() {
        float messageRate = 0;
        if (startTime == -1) {
            return -1;
        }

        long totalTime = System.currentTimeMillis() - startTime;
        messageRate = (float) (mesCount * 1000) / totalTime;
        return messageRate;
    }

    /**
     * Returns the message count for individual message listeners
     */
    public long getMessageCount() {
        return mesCount;
    }

    /**
     * Called when a message comes into the components input port
     *
     * @param message message object
     */
    public void onMessage(Message message) {
        if (startTime == -1) {
            startTime = System.currentTimeMillis();
        }

        try {
            receiverService.incrementMessageCount();
            mesCount++;
            if (session.getTransacted()) {
                if ((mesCount % receiverService.getTransactionSize()) == 0) {
                    session.commit();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.COMMITTED_SESSION) + session);
                    }
                }
            }
        }
        catch (JMSException ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.COMMIT_ERROR,
                                                       new Object[]{ex.getLocalizedMessage(), ex}));
        }
    }
}
