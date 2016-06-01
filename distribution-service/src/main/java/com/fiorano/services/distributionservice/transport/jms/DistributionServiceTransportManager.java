/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.transport.jms;

import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.IJMSRequestListener;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;
import com.fiorano.services.distributionservice.engine.DistributionServiceRequestProcessor;

import javax.jms.Message;


public class DistributionServiceTransportManager extends AbstractSyncIOTransportManager<IEngine> {
    public DistributionServiceTransportManager(IService parent) {
        super(parent);
    }

    @Override
    public IJMSRequestListener createRequestListener(String name, TransportAssociation transportAssociation) {
        DistributionServiceRequestProcessor requestProcessor = new DistributionServiceRequestProcessor(this, (DistributionServicePM) getParent().getConfiguration());
        ErrorHandler<Message> errorHandler = new ErrorHandler<>(this, getParent(), transportAssociation.getErrorTransport());
        return new DistributionServiceMessageListener(transportAssociation, requestProcessor, errorHandler, logger);
    }
}
