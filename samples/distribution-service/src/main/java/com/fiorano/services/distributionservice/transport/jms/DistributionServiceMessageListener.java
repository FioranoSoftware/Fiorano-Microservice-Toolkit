/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.transport.jms;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOMessageListener;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.services.distributionservice.DistributionServiceConstants;
import com.fiorano.services.distributionservice.engine.DistributionServiceRequestProcessor;

import javax.jms.Message;
import java.util.logging.Logger;


/**
 * listens to messages on the input port of the service
 *
 * @author FSIPL
 * @version 1.0
 * @created July 08, 2005
 */

public class DistributionServiceMessageListener extends AbstractSyncIOMessageListener<DistributionServiceRequestProcessor> {

    public DistributionServiceMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation, DistributionServiceRequestProcessor requestProcessor, ErrorHandler<Message> errorHandler, Logger logger) {
        super(transportAssociation, requestProcessor, errorHandler, logger);
    }

    public void onRequest(Message request) throws ServiceExecutionException {
        int portToSend = requestProcessor.process(request);
        sendResponse(DistributionServiceConstants.OUT_PORT_PREFIX + String.valueOf(portToSend), request);
    }
}
