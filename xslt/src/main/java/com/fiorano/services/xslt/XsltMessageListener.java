/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.jms.AbstractThreadPoolMsgListener;
import com.fiorano.edbc.framework.service.jms.ServiceExceptionHandler;
import com.fiorano.edbc.framework.service.pool.PoolException;
import com.fiorano.edbc.framework.service.pool.RequestResponse;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.record.ESBRecordDefinition;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * In order to receive message asynchronously as they are delivered to the message consumer, the client program needs to
 * create a message listener that implements the MessageListener interface.
 * MessageListener Listens to the JMSMessages, processes them and sends the result JMSMessage.
 * When a message listener is set on the consumer object, the session passes the incoming JMS messages to the
 * onMessage(..) method.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @created Thu, 22 Mar 2007
 */
public class XsltMessageListener extends AbstractThreadPoolMsgListener {
    /**
     * Request processor object.
     */
    private RequestProcessor requestProcessor;

    /**
     * Creates a Message Listener object.
     *
     * @param jmsHandler       jmshanlder object
     * @param exceptionHandler used in handling exceptions.
     */
    public XsltMessageListener(JMSHandler jmsHandler, final ServiceExceptionHandler exceptionHandler) {
        super(jmsHandler, exceptionHandler);
        if (jmsHandler.getServiceConfiguration().isEnableThreadPool()) {
            initializePool(new ProcessorPool(jmsHandler.getServiceConfiguration().getPoolSize(),
                    jmsHandler.getServiceConfiguration().getPoolSize(), jmsHandler));
        }
        ESBRecordDefinition recordDefinition = jmsHandler.getInputPortHandler().getInputPortInstanceAdapter().getSchema();
        requestProcessor = new RequestProcessor(recordDefinition, jmsHandler.getLogger(), jmsHandler.getServiceConfiguration());
        this.exceptionHandler = exceptionHandler;
    }

    public void handleMessage(final Message requestMessage) throws ServiceExecutionException {

        try {
            requestMessage.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTimeMillis));
        } catch (JMSException e) {
            getLogger().log(Level.WARNING, "Unable to set processing time", e);
        }

        if (executor != null) {
            super.handleMessage(requestMessage);
        } else {
            try {
                if (requestProcessor.IsIgnoreRequest(requestMessage)) {
                    sendResponse(requestMessage);
                } else {
                    requestProcessor.transformRequest(requestMessage);
                    Message response = getRequestProcessor().process(requestMessage);
                    requestProcessor.transformResponse(response);
                    sendResponse(response);
                }
            } catch (JMSException e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }

        }
    }

    protected Callable<RequestResponse> getCallable(final Message requestMessage) {
        return new Callable<RequestResponse>() {
            @Override
            public RequestResponse call() throws Exception {
                Message response = null;
                try {
                    RequestProcessor requestProcessor = (RequestProcessor) processorPool.borrowObject();

                    if (requestProcessor.IsIgnoreRequest(requestMessage)) {
                        response = requestMessage;
                    } else {
                        requestProcessor.transformRequest(requestMessage);
                        response = requestProcessor.process(requestMessage);
                        requestProcessor.transformResponse(response);
                    }
                    processorPool.returnObject(requestProcessor);
                } catch (Exception e) {
                    throw new PoolException(requestMessage, e);
                } catch (Throwable e) {
                    if (getLogger() != null) {
                        getLogger().log(Level.SEVERE, RBUtil.getMessage(com.fiorano.edbc.framework.service.jms.Bundle.class, com.fiorano.edbc.framework.service.jms.Bundle.UNEXPECTED_ERROR), e);
                    }
                    if (e instanceof OutOfMemoryError && !getExceptionHandler().isInMemory()) {
                        System.exit(101);
                    }
                }
                return new RequestResponse(requestMessage, response);
            }
        };
    }

    /**
     * Returns the request processor.
     *
     * @return requestProcessor object.
     */
    protected IRequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

}
