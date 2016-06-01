/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.engine;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.ServiceUtil;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.internal.engine.IRequestValidator;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.distributionservice.Bundle;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.logging.Level;


public class DistributionServiceRequestProcessor extends Module implements IRequestProcessor<Message, Integer> {
    private DistributionServicePM distributionServicePM;
    private int currentPortToSendIndex = 0;
    private int currentMessageCount = 0;

    public DistributionServiceRequestProcessor(IModule parent, DistributionServicePM distributionServicePM) {
        super(parent);
        this.logger = ServiceUtil.getLogger(this, this.getClass().getPackage().getName());
        this.distributionServicePM = distributionServicePM;
    }

    public Integer process(Message request) throws ServiceExecutionException {
        if (logger.isLoggable(Level.FINER)) {
            String text;
            try {
                text = request instanceof TextMessage ? ((TextMessage) request).getText() : "";
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.RECIEVED_MESSAGE, new Object[]{text}));
            } catch (JMSException e) {
                // do nothing as this can be completely ignored.
            }
        }
        return findPortToSend();
    }

    private int findPortToSend() {
        int currentPortWeight = distributionServicePM.getPortWeights()[currentPortToSendIndex];
        if (currentPortWeight <= currentMessageCount) {
            //if the weight of current output port is satisfied then send to the next output port available
            //make the message count as 1 the current message will be sent on the updated port
            currentPortToSendIndex = (currentPortToSendIndex + 1) % distributionServicePM.getPortWeights().length;
            currentMessageCount = 1;
        } else {
            ++currentMessageCount;
        }
        return currentPortToSendIndex;
    }


    public IRequestValidator<Message> getRequestValidator() {
        return null;
    }

}
