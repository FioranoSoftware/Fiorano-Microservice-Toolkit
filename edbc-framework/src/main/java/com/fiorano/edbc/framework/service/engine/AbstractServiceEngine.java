/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.engine;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

/**
 * <code>AbstractServiceEngine</code> holds all data (objects) required for executing logic of service and drives service with the help of
 * classes in this package.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractServiceEngine {
    private IServiceConfiguration serviceConfiguration;
    private AbstractConnection connection;
    private IRequestProcessor requestProcessor;

    /**
     * Creates an engine class with the given configuration
     *
     * @param serviceConfiguration configuration details of service
     */
    protected AbstractServiceEngine(IServiceConfiguration serviceConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
    }

    /**
     * Starts this service, typically operations performed at startup like connection creation happens here.
     *
     * @throws ServiceExecutionException
     */
    public void start() throws ServiceExecutionException {
        if (!isConnectionless()) {
            connection = createConnection();
            connection.init();
        }
    }

    /**
     * Creates connection to EIS
     *
     * @return connection to EIS
     * @throws ServiceExecutionException
     */
    public abstract AbstractConnection createConnection() throws ServiceExecutionException;

    /**
     * Stops the service all cleanup is done here.
     *
     * @throws ServiceExecutionException
     */
    public void stop() throws ServiceExecutionException {
        if (connection != null) {
            connection.destroy();
        }
    }

    /**
     * Returns the configuration of service
     *
     * @return configuration
     */
    public IServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    /**
     * Sets the configuration of service
     *
     * @param serviceConfiguration configuration of service
     */
    public void setServiceConfiguration(IServiceConfiguration serviceConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
    }

    /**
     * Determines if the service does not require a connection to EIS
     *
     * @return <code>true</code> service does not require creating a connection to EIS,<code>false</code> otherwise
     */
    public boolean isConnectionless() {
        return serviceConfiguration instanceof ConnectionlessServiceConfiguration;
    }

    /**
     * Returns the connection to EIS
     *
     * @return connection to EIS
     */
    public AbstractConnection getConnection() {
        return connection;
    }

    /**
     * Sets the connection to EIS
     *
     * @param connection
     */
    public void setConnection(AbstractConnection connection) {
        this.connection = connection;
    }

    /**
     * Returns a request processor which can process the incoming request
     *
     * @return request processor for processing requests
     */
    public IRequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    public void setRequestProcessor(IRequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

}
