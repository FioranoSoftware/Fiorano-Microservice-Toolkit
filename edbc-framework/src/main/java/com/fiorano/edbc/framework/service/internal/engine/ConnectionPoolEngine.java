/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.connection.pool.ConnectionPool;
import com.fiorano.edbc.framework.service.connection.pool.ConnectionPoolFactory;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.services.common.configuration.ConnectionPoolConfiguration;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public abstract class ConnectionPoolEngine extends Engine {

    protected ConnectionPool connectionPool;

    public ConnectionPoolEngine(IModule parent, IServiceConfiguration configuration) {
        super(parent, configuration);
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    protected GenericObjectPoolConfig getObjectPoolConfig() {

        if (configuration instanceof ConnectionlessServiceConfiguration) {
            ConnectionPoolConfiguration connectionPoolConfig = ((ConnectionlessServiceConfiguration) configuration)
                    .getConnectionPoolConfiguration();
            return convertToObjectPoolConfig(connectionPoolConfig);
        } else {
            return new GenericObjectPoolConfig();
        }
    }

    @Override
    protected void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        if (configuration instanceof ConnectionlessServiceConfiguration) {
            GenericObjectPoolConfig objectPoolConfig;
            if (((ConnectionlessServiceConfiguration) configuration).getConnectionPoolConfiguration().isEnabled()) {
                objectPoolConfig = getObjectPoolConfig();
            } else {
                objectPoolConfig = new GenericObjectPoolConfig();
                objectPoolConfig.setMaxTotal(1);
            }
            connectionPool = new ConnectionPool(new GenericObjectPool<>(getConnectionPoolFactory(), objectPoolConfig));
        }
    }

    @Override
    protected void internalStop() throws ServiceExecutionException {
        if (connectionPool != null) {
            connectionPool.close();
        }
        super.internalStop();
    }

    protected abstract ConnectionPoolFactory getConnectionPoolFactory();

    private GenericObjectPoolConfig convertToObjectPoolConfig(ConnectionPoolConfiguration connectionPoolConfig) {

        GenericObjectPoolConfig objectPoolConfig = new GenericObjectPoolConfig();
        objectPoolConfig.setMaxTotal(connectionPoolConfig.getMaxConnections());
        objectPoolConfig.setMaxIdle(connectionPoolConfig.getMaxIdleConnections());
        objectPoolConfig.setMaxWaitMillis(connectionPoolConfig.getMaxWaitTimeMillis());
        objectPoolConfig.setMinEvictableIdleTimeMillis(connectionPoolConfig.getIdleTimeout());
        objectPoolConfig.setTimeBetweenEvictionRunsMillis(connectionPoolConfig.getIdleTimeout());
        objectPoolConfig.setNumTestsPerEvictionRun(connectionPoolConfig.getMaxConnections());
        objectPoolConfig.setTestOnBorrow(true);
        objectPoolConfig.setTestOnCreate(true);
        return objectPoolConfig;
    }
}
