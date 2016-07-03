/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor;
import com.fiorano.edbc.framework.service.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subclasses of <code>AbstractMessageListener</code> listen for messages on ports they are set to
 * and passes on the message to RequestProcessor for procesing the request
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractMessageListener implements MessageListener, IMessageHandler {
    protected long inTimeMillis;

    public void onMessage(Message requestMessage) {
        inTimeMillis = System.currentTimeMillis();
        try {
            MessageUtil.makeMessageReadWrite(requestMessage);
            requestMessage.setStringProperty(Constants.COMPONENT_IN_TIME, String.valueOf(inTimeMillis));
            handleMessage(requestMessage);
        } catch (ServiceExecutionException e) {
            handleException(e, requestMessage);
        } catch (JMSException e) {
            if (getLogger() != null) {
                getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CONVERTING_MESSAGE), e);
            }
        } catch (Throwable e) {
            if (getLogger() != null) {
                getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNEXPECTED_ERROR), e);
            }
            if (e instanceof OutOfMemoryError && !getExceptionHandler().isInMemory())
                System.exit(101);
        } finally {
            try {
                if (getSession() != null && !getSession().getTransacted() && getSession().getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
                    requestMessage.acknowledge();
                    getLogger().log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.ACKNOWLEDGE_SUCCESS));
                }
            } catch (JMSException e) {
                getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ACKNOWLEDGE_FAILED), e);
            } catch (Throwable e) {
                if (getLogger() != null) {
                    getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNEXPECTED_ERROR), e);
                }
                if (e instanceof OutOfMemoryError && !getExceptionHandler().isInMemory())
                    System.exit(101);
            }
        }
    }

    public void handleMessage(Message requestMessage) throws ServiceExecutionException {
        IRequestProcessor requestProcessor = getRequestProcessor();
        String response;
        String request;
        try {
            request = MessageUtil.getTextData(requestMessage);
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_FETCH_REQUEST), e, ServiceErrorID.TRANSPORT_ERROR);
        }

        if (requestProcessor != null) {
            try {
                if (requestProcessor instanceof AbstractRequestProcessor && !((AbstractRequestProcessor) requestProcessor).IsIgnoreRequest(requestMessage)) {
                    ((AbstractRequestProcessor) requestProcessor).transformRequest(requestMessage);
                    try {
                        requestProcessor.validate(request);
                    } catch (ServiceExecutionException e) {
                        handleException(e, requestMessage);
                    }
                    response = requestProcessor.process(request);
                } else {
                    sendResponse(requestMessage);
                    return;
                }
            } catch (JMSException e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        } else {
            response = request;
        }
        try {
            requestMessage.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTimeMillis));
        } catch (JMSException e) {
            getLogger().log(Level.WARNING, "Unable to set processing time", e);
        }
        sendResponse(prepareResponse(requestMessage, response));
    }

    public boolean handleException(ServiceExecutionException e, Message requestMessage) {
        ServiceExceptionHandler exceptionHandler = getExceptionHandler();
        return exceptionHandler != null && exceptionHandler.handleException(e, requestMessage, this);
    }

    protected Message prepareResponse(Message requestMessage, String response) throws ServiceExecutionException {
        Message responseMessage;
        try {
            responseMessage = getMessageForResponse(requestMessage);
            MessageUtil.setTextData(responseMessage, response);
            MessageUtil.setAllProperties(responseMessage, getPropertiesForResponse());
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_CREATE_RESPONSE_MESSAGE), e, ServiceErrorID.TRANSPORT_ERROR);
        }
        IRequestProcessor requestProcessor = getRequestProcessor();
        if (requestProcessor instanceof AbstractRequestProcessor) {
            ((AbstractRequestProcessor) requestProcessor).transformResponse(responseMessage);
        }
        return responseMessage;
    }

    protected Message getMessageForResponse(Message requestMessage) throws JMSException {
        if (useInputMessageForOutput()) {
            MessageUtil.makeMessageReadWrite(requestMessage);
            return requestMessage;
        } else {
            Message responseMessage = MessageUtil.createMessage(getSession(), requestMessage.getClass());
            MessageUtil.cloneMessage(requestMessage, responseMessage);
            return responseMessage;
        }
    }

    protected Map getPropertiesForResponse() {
        return Collections.EMPTY_MAP;
    }

    protected boolean useInputMessageForOutput() {
        return true;
    }

    protected abstract IRequestProcessor getRequestProcessor();

    protected abstract Session getSession();

    protected abstract Logger getLogger();

    protected abstract void sendResponse(Message message) throws ServiceExecutionException;

    protected abstract ServiceExceptionHandler getExceptionHandler();
}
