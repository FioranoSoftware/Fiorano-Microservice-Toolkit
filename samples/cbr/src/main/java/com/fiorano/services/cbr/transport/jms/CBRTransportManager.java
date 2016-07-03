/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.transport.jms;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.IJMSRequestListener;
import com.fiorano.services.cbr.engine.CBRRequestProcessor;
import com.fiorano.services.cbr.engine.ProcessorPool;

import javax.jms.Message;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 27 Dec, 2010
 * Time: 3:09:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CBRTransportManager extends AbstractSyncIOTransportManager<IEngine> {

    private CBRPropertyModel configuration;

    public CBRTransportManager(IService parent) {
        super(parent);
        this.configuration = (CBRPropertyModel) parent.getConfiguration();
    }

    @Override
    public IJMSRequestListener createRequestListener(String name, TransportAssociation transportAssociation) {
        ErrorHandler<Message> errorHandler = new ErrorHandler<>(this, getParent(), transportAssociation.getErrorTransport());
        IJMSRequestListener messageListener;
        if (configuration.isEnableThreadPool()) {
            messageListener = new CBRMessageListener(transportAssociation, new ProcessorPool(this, configuration, transportAssociation),
                    configuration, errorHandler, logger);
        } else {
            messageListener = new CBRMessageListener(transportAssociation, new CBRRequestProcessor(this, configuration, transportAssociation),
                    errorHandler, logger, configuration);
        }
        return messageListener;
    }
}
