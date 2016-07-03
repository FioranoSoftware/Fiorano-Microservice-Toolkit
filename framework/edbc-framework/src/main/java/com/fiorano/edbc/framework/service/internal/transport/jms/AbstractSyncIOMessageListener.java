/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.pool.ObjectPool;
import com.fiorano.edbc.framework.service.pool.PoolException;
import com.fiorano.edbc.framework.service.pool.RequestResponse;
import com.fiorano.services.common.configuration.XSLConfiguration;
import com.fiorano.services.common.transformation.EDBCXslTransformer;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.AcknowledgeMode;
import com.fiorano.util.StringUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 22-Nov-2010
 * Time: 14:41:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSyncIOMessageListener<RP extends IRequestProcessor> implements IJMSRequestListener {

    private static final Object lock = new Object();
    protected AbstractSyncIOTransportManager.TransportAssociation transportAssociation;
    protected ErrorHandler<Message> errorHandler;
    protected RP requestProcessor;
    protected Logger logger;
    protected long inTimeMillis;
    private EDBCXslTransformer inputTransformer;
    private EDBCXslTransformer outputTransformer;
    private IServiceConfiguration configuration;
    private long resequenceInterval;
    private ScheduledExecutorService scheduledExecutorService;
    private ExecutorService executor;
    private List<Future<RequestResponse>> messageFutures = new Vector<>();
    private ObjectPool<RP> processorPool;
    private boolean isBatchMode = false;

    public AbstractSyncIOMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation, RP requestProcessor,
                                         ErrorHandler<Message> errorHandler, Logger logger) {
        this.transportAssociation = transportAssociation;
        this.requestProcessor = requestProcessor;
        this.errorHandler = errorHandler;
        this.logger = logger;
    }

    public AbstractSyncIOMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation,
                                         ObjectPool<RP> processorPool, IServiceConfiguration configuration,
                                         final ErrorHandler<Message> errorHandler, final Logger logger) {
        this.transportAssociation = transportAssociation;
        this.processorPool = processorPool;
        this.errorHandler = errorHandler;
        this.logger = logger;
        this.configuration = configuration;
        intializePool();
    }

    private void intializePool() {

        resequenceInterval = configuration.getBatchEvictionInterval();
        int poolSize = configuration.getPoolSize();
        if (configuration.isEnableThreadPool()) {
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
                        handleException((ServiceExecutionException) e.getCause(), e.getRequest());
                    } catch (Throwable e) {
                        if (logger != null) {
                            logger.log(Level.SEVERE, RBUtil.getMessage(com.fiorano.edbc.framework.service.jms.Bundle.class, com.fiorano.edbc.framework.service.jms.Bundle.UNEXPECTED_ERROR), e);
                        }
                        if (e instanceof OutOfMemoryError && !errorHandler.isInMemory()) {
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
                sendResponses(requestResponse.getResponses());
                requestResponse.getRequest().acknowledge();
                iterator.remove();
            }
        } catch (ServiceExecutionException e) {
            handleException(e, requestResponse.getRequest());
        } catch (JMSException e) {
            handleException(new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR),
                    requestResponse.getRequest());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof PoolException) {
                throw (PoolException) e.getCause();
            } else {
                logger.log(Level.SEVERE, "Error Processing batch", e);
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error Processing batch", e);
        } catch (TimeoutException e) {
            //ignore
        }
    }

    private Callable<RequestResponse> getCallable(final Message requestMessage) {
        return new Callable<RequestResponse>() {
            @Override
            public RequestResponse call() throws Exception {
                Map<String, List<Message>> responses = null;
                try {
                    IRequestProcessor requestProcessor = processorPool.borrowObject();

                    if (isIgnoreRequest(requestMessage)) {
                        sendResponse(Constants.OUT_PORT_NAME, requestMessage);
                    } else {
                        transformRequest(requestMessage);
                        responses = handleRequest((RP) requestProcessor, requestMessage);
                    }
                    processorPool.returnObject((RP) requestProcessor);
                } catch (Exception e) {
                    throw new PoolException(requestMessage, e);
                } catch (Throwable e) {
                    if (logger != null) {
                        logger.log(Level.SEVERE, RBUtil.getMessage(com.fiorano.edbc.framework.service.jms.Bundle.class, com.fiorano.edbc.framework.service.jms.Bundle.UNEXPECTED_ERROR), e);
                    }
                    if (e instanceof OutOfMemoryError && !errorHandler.isInMemory()) {
                        System.exit(101);
                    }
                }
                return new RequestResponse(requestMessage, responses);
            }
        };
    }

    protected void sendResponses(Map<String, List<Message>> responses) throws ServiceExecutionException {

        for (String port : responses.keySet()) {
            List<Message> messages = responses.get(port);
            for (Message message : messages) {
                sendResponse(port, message);
            }
        }
    }

    //returns map of outport name and list of messages to be sent on that port
    protected Map<String, List<Message>> handleRequest(RP requestProcessor, Message request) throws ServiceExecutionException {
        return null;
    }

    protected ObjectPool<RP> getObjectPool() {
        return null;
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

    public boolean isIgnoreRequest(Message request) throws JMSException {
        if (configuration != null && configuration instanceof ConnectionlessServiceConfiguration) {
            ConnectionlessServiceConfiguration serviceConfiguration = (ConnectionlessServiceConfiguration) configuration;
            String configured = serviceConfiguration.getPropertyValue();
            String received = request.getStringProperty(serviceConfiguration.getPropertyName());
            return serviceConfiguration.isProcessMessageBasedOnProperty() && !StringUtil.isEmpty(configured) && !configured.equals(received);
        } else {
            return false;
        }
    }

    public void onMessage(Message requestMessage) {
        inTimeMillis = System.currentTimeMillis();
        try {
            MessageUtil.makeMessageReadWrite(requestMessage);
            requestMessage.setStringProperty(Constants.COMPONENT_IN_TIME, String.valueOf(inTimeMillis));
        } catch (JMSException e) {
            logger.log(Level.WARNING, "Unable to convert message into writable mode.", e);
        }
        try {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.RECEIVED_REQUEST, new Message[]{requestMessage}));
            }
            if (requestProcessor != null && configuration == null) {
                if (requestProcessor.getParent().getParent() instanceof IService) {
                    configuration = ((IService) requestProcessor.getParent().getParent()).getConfiguration();
                } else if (requestProcessor.getParent().getParent().getParent() instanceof IService) {
                    configuration = ((IService) requestProcessor.getParent().getParent().getParent()).getConfiguration();
                }
            }

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
                if (isIgnoreRequest(requestMessage)) {
                    sendResponse(Constants.OUT_PORT_NAME, requestMessage);
                } else {
                    transformRequest(requestMessage);
                    onRequest(requestMessage);
                }
            }
        } catch (ServiceExecutionException e) {
            handleException(e, requestMessage);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNEXPECTED_ERROR), e);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNEXPECTED_ERROR), ex);
            if (ex instanceof OutOfMemoryError && (errorHandler != null && !errorHandler.isInMemory()))
                System.exit(101);
        }
        try {
            if (transportAssociation.getInputTransport().getSession().getTransacted()) {
                transportAssociation.getInputTransport().commit();
                //todo log
            } else if (!isBatchMode && AcknowledgeMode.CLIENT_ACKNOWLEDGE.getJMSValue() == transportAssociation.getInputTransport().getSession().getAcknowledgeMode()) {
                acknowledgeMessage(requestMessage);
            }
        } catch (JMSException e) {
            handleException(new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.ACKNOWLEDGE_FAILED), e, ServiceErrorID.TRANSPORT_ERROR), requestMessage);
        } catch (ServiceExecutionException e) {
            handleException(e, requestMessage);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNEXPECTED_ERROR), ex);
            if (ex instanceof OutOfMemoryError && (errorHandler != null && !errorHandler.isInMemory()))
                System.exit(101);
        }
    }

    protected void acknowledgeMessage(Message requestMessage) throws JMSException {
        requestMessage.acknowledge();
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ACKNOWLEDGE_SUCCESS));
    }

    public void transformRequest(Message requestMessage) throws ServiceExecutionException {
        XSLConfiguration inpXslConfiguration = null;
        if (configuration != null && configuration instanceof ConnectionlessServiceConfiguration)
            inpXslConfiguration = ((ConnectionlessServiceConfiguration) configuration).getInputXSLConfiguration();
        if (inpXslConfiguration != null && !StringUtil.isEmpty(inpXslConfiguration.getXslValue())) {
            try {
                if (inputTransformer == null) {
                    inputTransformer = new EDBCXslTransformer(inpXslConfiguration, logger);
                }
                inputTransformer.invokeTransformer(requestMessage);
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
    }

    public void transformResponse(Message response) throws ServiceExecutionException {
        XSLConfiguration outputXslConfiguration = null;
        if (configuration != null && configuration instanceof ConnectionlessServiceConfiguration)
            outputXslConfiguration = ((ConnectionlessServiceConfiguration) configuration).getOutputXSLConfiguration();
        if (outputXslConfiguration != null && !StringUtil.isEmpty(outputXslConfiguration.getXslValue())) {
            try {
                if (outputTransformer == null) {
                    outputTransformer = new EDBCXslTransformer(outputXslConfiguration, logger);
                }
                outputTransformer.invokeTransformer(response);
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
    }

    protected void prepareResponse(Message request, Message response) throws ServiceExecutionException {
        try {
            MessageUtil.copyProperties(request, response);
            Enumeration propertyNames = request.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                if (!response.propertyExists(propertyName)) {
                    Object value = MessageUtil.getPropertyValue(propertyName, request);
                    MessageUtil.setProperty(propertyName, value, response);
                }
            }
            MessageUtil.copyJMSHeaders(request, response);
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_PREPARE_RESPONSE_MESSAGE), e, ServiceErrorID.RESPONSE_GENERATION_ERROR);
        }
    }

    protected Message createResponseMessage() throws ServiceExecutionException {
        try {
            return transportAssociation.getInputTransport().getSession().createTextMessage();
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_CREATE_RESPONSE_MESSAGE), e, ServiceErrorID.RESPONSE_GENERATION_ERROR);
        }
    }

    protected void sendResponse(String outputName, Message response) throws ServiceExecutionException {
        try {
            if (!isIgnoreRequest(response)) {
                transformResponse(response);
            }
            response.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTimeMillis));
        } catch (JMSException e) {
            logger.log(Level.WARNING, "Unable to set processing time", e);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.SENDING_RESPONSE, new Message[]{response}));
        }
        transportAssociation.getOutputTransport(outputName).send(response);
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RESPONSE_SEND_SUCCESSFUL));
    }

    protected void handleException(ServiceExecutionException e, Message requestMessage) {
        if (errorHandler != null) {
            errorHandler.handleException(e, requestMessage, this, null);
        }
    }
}
