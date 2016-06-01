/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cache.transport.jms;

import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.IJMSRequestListener;
import com.fiorano.services.cache.engine.CacheEngine;
import com.fiorano.services.cache.engine.CacheRequestProcessor;

import javax.jms.Message;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 16, 2010
 * Time: 11:39:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class CacheTransportManager extends AbstractSyncIOTransportManager<CacheEngine> {

    public CacheTransportManager(IService parent) {
        super(parent);
    }

    public IJMSRequestListener createRequestListener(String name, TransportAssociation transportAssociation) {
        CacheRequestProcessor requestProcessor = (CacheRequestProcessor) requestProcessFactory.createRequestProcessor(this, transportAssociation.getInputTransport().getConfiguration().getName());
        ErrorHandler<Message> errorHandler = new ErrorHandler<>(this, getParent(), transportAssociation.getErrorTransport());
        return new CacheMessageListener(transportAssociation, requestProcessor, errorHandler, logger);
    }
}
