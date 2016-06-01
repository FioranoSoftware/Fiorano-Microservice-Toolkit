/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.error;

import com.fiorano.edbc.framework.service.connection.IConnection;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.transport.IRequestListener;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 8, 2010
 * Time: 8:33:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IErrorHandler<R> extends IModule {
    void handleException(ServiceExecutionException exception, R requestMessage, IRequestListener<R> requestListener, IConnection connection);
}
