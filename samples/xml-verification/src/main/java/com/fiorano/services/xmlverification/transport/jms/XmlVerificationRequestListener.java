/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification.transport.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOMessageListener;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.services.xmlverification.engine.XmlVerificationRequestProcessor;
import com.fiorano.util.ExceptionUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.logging.Logger;


public class XmlVerificationRequestListener extends AbstractSyncIOMessageListener<XmlVerificationRequestProcessor> {
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    private static final String STACKTRACE = "STACKTRACE";

    public XmlVerificationRequestListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation, XmlVerificationRequestProcessor requestProcessor, ErrorHandler<Message> errorHandler, Logger logger) {
        super(transportAssociation, requestProcessor, errorHandler, logger);
    }

    public void onRequest(Message request) throws ServiceExecutionException {
        TextMessage newMessage;
        try {
            Message response = requestProcessor.process(request);
            sendResponse(Constants.OUT_PORT_NAME, response);
        } catch (ServiceExecutionException e) {
            if (e.getErrorID().equals(ServiceErrorID.INVALID_REQUEST_ERROR)) {
                try {
                    newMessage = transportAssociation.getInputTransport().getSession().createTextMessage();
                    MessageUtil.cloneMessage(request, newMessage);
                    newMessage.setStringProperty(ERROR_MESSAGE, e.getMessage());
                    newMessage.setStringProperty(STACKTRACE, ExceptionUtil.getStackTrace(e));
                } catch (JMSException e1) {
                    throw new ServiceExecutionException(e1, ServiceErrorID.RESPONSE_GENERATION_ERROR);
                }
                sendResponse("FAILED_PORT", newMessage);
            } else {
                throw e;
            }
        }
    }
}