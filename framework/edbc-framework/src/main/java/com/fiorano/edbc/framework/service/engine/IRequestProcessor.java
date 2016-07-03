/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.engine;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

import javax.jms.Message;

/**
 * <code>IRequestProcessor</code> is an interface that must be extended by classes that should process a request.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface IRequestProcessor {

    /**
     * Process the request and returns the response
     *
     * @param request that has to be processed
     * @return response after processing
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException any exception that might occur while
     */
    String process(String request) throws ServiceExecutionException;

    /**
     * Validates request.
     *
     * @param request request to be processed
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException any exception that might occur while validating the request.
     */
    void validate(String request) throws ServiceExecutionException;

    /**
     * Process the request and returns the response
     *
     * @param request that has to be processed
     * @return response after processing
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException any exception that might occur while processing
     */
    Object process(Object request) throws ServiceExecutionException;

    /**
     * Process the request and returns the response
     *
     * @param request that has to be processed
     * @return response after processing
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException any exception that might occur while processing
     */
    Message process(Message request) throws ServiceExecutionException;
}
