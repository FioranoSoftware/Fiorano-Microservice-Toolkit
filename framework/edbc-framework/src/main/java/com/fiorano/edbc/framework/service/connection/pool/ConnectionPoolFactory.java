/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.connection.pool;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.connection.IConnection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.logging.Logger;

public abstract class ConnectionPoolFactory extends BasePooledObjectFactory<IConnection> {

    protected IServiceConfiguration configuration;
    protected Logger logger;

    public ConnectionPoolFactory(IServiceConfiguration configuration, Logger logger) {
        this.configuration = configuration;
        this.logger = logger;
    }

    @Override
    public PooledObject<IConnection> wrap(IConnection iConnection) {
        return new DefaultPooledObject<>(iConnection);
    }
}
