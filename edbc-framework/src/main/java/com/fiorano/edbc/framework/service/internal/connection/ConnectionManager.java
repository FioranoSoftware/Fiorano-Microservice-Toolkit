/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.connection;

import com.fiorano.edbc.framework.service.configuration.AbstractConnectionConfiguration;
import com.fiorano.edbc.framework.service.connection.IConnection;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class ConnectionManager<C extends IConnection, CC extends AbstractConnectionConfiguration>
        extends Module implements IConnectionManager<C, CC> {

    protected ConnectionManager(IModule parent) {
        super(parent);
    }

    //---------------------------------------------[IConnectionManager API]----------------------------------------------------
    public abstract C createConnection(CC connectionConfiguration) throws ServiceExecutionException;

    public void destroyConnection(C connection) throws ServiceExecutionException {
        connection.destroy();
    }

    public boolean isValid(C connection) {
        return connection.isValid();
    }

    public void errorOccured(C connection) throws ServiceExecutionException {
        connection.fireConnectionError();
    }
}
