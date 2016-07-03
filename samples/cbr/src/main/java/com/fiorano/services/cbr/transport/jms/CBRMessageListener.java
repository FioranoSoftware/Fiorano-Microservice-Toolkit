/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.transport.jms;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOMessageListener;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.pool.ObjectPool;
import com.fiorano.services.cbr.engine.CBRRequestProcessor;

import javax.jms.Message;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 27 Dec, 2010
 * Time: 3:20:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CBRMessageListener extends AbstractSyncIOMessageListener<CBRRequestProcessor> {

    public CBRMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation, CBRRequestProcessor requestProcessor, ErrorHandler<Message> errorHandler, Logger logger, CBRPropertyModel configuration) {
        super(transportAssociation, requestProcessor, errorHandler, logger);
    }

    public CBRMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation,
                              ObjectPool<CBRRequestProcessor> processorPool, IServiceConfiguration configuration,
                              ErrorHandler<Message> errorHandler, Logger logger) {
        super(transportAssociation, processorPool, configuration, errorHandler, logger);
    }


    public void onRequest(Message message) throws ServiceExecutionException {
        Map<String, List<Message>> responses = handleRequest(requestProcessor, message);
        sendResponses(responses);
    }

    @Override
    protected Map<String, List<Message>> handleRequest(CBRRequestProcessor requestProcessor, Message message)
            throws ServiceExecutionException {

        return requestProcessor.handleRequest(message);
    }
}
