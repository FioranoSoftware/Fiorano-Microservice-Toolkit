/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cache.transport.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.engine.XMLStringRequestValidator;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOMessageListener;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.Bundle;
import com.fiorano.services.cache.engine.CacheRequestProcessor;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 24-Nov-2010
 * Time: 16:56:42
 * To change this template use File | Settings | File Templates.
 */
public class CacheMessageListener extends AbstractSyncIOMessageListener<CacheRequestProcessor> {

    public CacheMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation, CacheRequestProcessor requestProcessor,
                                ErrorHandler<Message> errorHandler, Logger logger) {
        super(transportAssociation, requestProcessor, errorHandler, logger);
        XMLStringRequestValidator requestValidator = new XMLStringRequestValidator((ESBRecordDefinition) transportAssociation.getInputTransport().getConfiguration().getSchema(), logger);
        requestProcessor.setRequestValidator(requestValidator);
    }

    public void onRequest(Message request) throws ServiceExecutionException {
        String requestText;
        try {
            requestText = MessageUtil.getTextData(request);
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_EXTRACT_REQUEST), e,
                    ServiceErrorID.INVALID_REQUEST_ERROR);
        }
        String responseText = requestProcessor.process(requestText);
        try {
            MessageUtil.makeMessageReadWrite(request);
            MessageUtil.setTextData(request, responseText);
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_CREATE_RESPONSE_MESSAGE), e,
                    ServiceErrorID.RESPONSE_GENERATION_ERROR);
        }
        sendResponse(Constants.OUT_PORT_NAME, request);
    }
}
