/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor;
import com.fiorano.edbc.framework.service.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.pool.ObjectPool;
import com.fiorano.edbc.framework.service.pool.PoolException;
import com.fiorano.edbc.framework.service.pool.RequestResponse;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractThreadPoolMsgListener extends AbstractMessageListener {

    private static final Object lock = new Object();
    protected ExecutorService executor;
    protected ObjectPool processorPool;
    protected AbstractJMSHandler jmsHandler;
    protected ServiceExceptionHandler exceptionHandler;
    private long resequenceInterval;
    private ScheduledExecutorService scheduledExecutorService;
    private List<Future<RequestResponse>> messageFutures = new Vector<>();
    private boolean isBatchMode = false;

    public AbstractThreadPoolMsgListener(AbstractJMSHandler jmsHandler, ServiceExceptionHandler exceptionHandler) {
        this.jmsHandler = jmsHandler;
        this.exceptionHandler = exceptionHandler;
    }

    protected void initializePool(ObjectPool processorPool) {

        IServiceConfiguration configuration = jmsHandler.getServiceConfiguration();

        if (configuration.isEnableThreadPool()) {
            this.processorPool = processorPool;
            resequenceInterval = configuration.getBatchEvictionInterval();
            int poolSize = configuration.getPoolSize();
            isBatchMode = true;
            executor = Executors.newFixedThreadPool(poolSize == -1 ? 5 : poolSize);

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isSingleThreadedMode()) {
                            sendProcessedMessages();
                        } else {
                            synchronized (lock) {
                                sendProcessedMessages();
                            }
                        }
                    } catch (PoolException e) {
                        getExceptionHandler().handleException((ServiceExecutionException) e.getCause(), e.getRequest(),
                                AbstractThreadPoolMsgListener.this);
                    } catch (Throwable e) {
                        if (getLogger() != null) {
                            getLogger().log(Level.SEVERE, RBUtil.getMessage(com.fiorano.edbc.framework.service.jms.Bundle.class, com.fiorano.edbc.framework.service.jms.Bundle.UNEXPECTED_ERROR), e);
                        }
                        if (e instanceof OutOfMemoryError && !getExceptionHandler().isInMemory()) {
                            System.exit(101);
                        }
                    }
                }
            };
            if (!isSingleThreadedMode()) {
                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleWithFixedDelay(task, resequenceInterval,
                        resequenceInterval, TimeUnit.MILLISECONDS);
            }
        }
    }

    protected Callable<RequestResponse> getCallable(final Message requestMessage) {
        return new Callable<RequestResponse>() {
            @Override
            public RequestResponse call() throws Exception {
                Message responseMsg = null;
                try {
                    AbstractRequestProcessor requestProcessor = (AbstractRequestProcessor) processorPool.borrowObject();

                    if (requestProcessor.IsIgnoreRequest(requestMessage)) {
                        responseMsg = requestMessage;
                    } else {
                        requestProcessor.transformRequest(requestMessage);
                        String request;
                        try {
                            request = MessageUtil.getTextData(requestMessage);
                        } catch (JMSException e) {
                            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_FETCH_REQUEST),
                                    e, ServiceErrorID.TRANSPORT_ERROR);
                        }
                        String response = requestProcessor.process(request);
                        responseMsg = getSession().createTextMessage();
                        MessageUtil.cloneMessage(requestMessage, responseMsg);
                        MessageUtil.setTextData(responseMsg, response);
                        requestProcessor.transformResponse(responseMsg);
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
                return new RequestResponse(requestMessage, responseMsg);
            }
        };
    }

    private boolean isSingleThreadedMode() {
        return resequenceInterval == -1;
    }

    private void sendProcessedMessages() throws PoolException {
        RequestResponse requestResponse = null;
        try {
            Iterator<Future<RequestResponse>> iterator = messageFutures.iterator();
            while (iterator.hasNext()) {
                Future<RequestResponse> messageFuture = iterator.next();
                requestResponse = messageFuture.get(10, TimeUnit.MILLISECONDS);
                sendResponse(requestResponse.getResponse());
                requestResponse.getRequest().acknowledge();
                iterator.remove();
            }
        } catch (ServiceExecutionException e) {
            getExceptionHandler().handleException(e, requestResponse.getRequest(), this);
        } catch (JMSException e) {
            getExceptionHandler().handleException(new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR),
                    requestResponse.getRequest(), this);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof PoolException) {
                throw (PoolException) e.getCause();
            } else {
                getLogger().log(Level.SEVERE, "Error Processing batch", e);
            }
        } catch (InterruptedException e) {
            getLogger().log(Level.SEVERE, "Error Processing batch", e);
        } catch (TimeoutException e) {
            //ignore
        }
    }

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
                if (!isBatchMode && getSession() != null && !getSession().getTransacted()
                        && getSession().getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
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

        if (executor != null) {
            Future<RequestResponse> submit = executor.submit(getCallable(requestMessage));

            if (isSingleThreadedMode()) {
                messageFutures.add(submit);
            } else {
                synchronized (lock) {
                    messageFutures.add(submit);
                }
            }
        } else {
            String response;
            String request;
            try {
                request = MessageUtil.getTextData(requestMessage);
            } catch (JMSException e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
            IRequestProcessor requestProcessor = getRequestProcessor();
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
    }

    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
        if (processorPool != null) {
            processorPool.shutdown();
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    /**
     * Gets the session object
     *
     * @return session object
     */
    protected Session getSession() {
        return jmsHandler.getSession();
    }

    /**
     * Gets the logger
     *
     * @return logger used for logging
     */
    protected Logger getLogger() {
        return jmsHandler.getLogger();
    }

    /**
     * Gets the exception handler.
     *
     * @return exceptionHandler object.
     */
    protected ServiceExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Sends the response message after processing the input message.
     * Currently the message is handled in the AbstractMessageListener class.
     * handleMessage(..) can be overidden to implement our own handler.
     *
     * @param message output message
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException if there is any exception in sending the message.
     */
    protected void sendResponse(Message message) throws ServiceExecutionException {
        try {
            jmsHandler.sendMessage(message);
        } catch (JMSException e) {
            throw new ServiceExecutionException(e, ServiceErrorID.RESPONSE_GENERATION_ERROR);
        }
        getLogger().log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.MESSAGE_SENT_SUCCESSFULLY, new Object[]{message}));
    }
}
