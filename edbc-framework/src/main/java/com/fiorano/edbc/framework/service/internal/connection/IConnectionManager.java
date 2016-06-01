/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.connection;

import com.fiorano.edbc.framework.service.connection.IConnection;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface IConnectionManager<C extends IConnection, CC> extends IModule {
    C createConnection(CC connectionConfiguration) throws ServiceExecutionException;

    void destroyConnection(C connection) throws ServiceExecutionException;

    boolean isValid(C connection);

    void errorOccured(C connection) throws ServiceExecutionException;
}
