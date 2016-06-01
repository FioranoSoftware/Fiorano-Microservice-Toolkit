/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface IErrorTransport<C extends ITransportConfiguration, R> extends ITransport<C, R> {
    void sendError(String errorCode, String errorMessage, Throwable th, R request) throws ServiceExecutionException;

    void sendError(ServiceExecutionException th, R request) throws ServiceExecutionException;
}
