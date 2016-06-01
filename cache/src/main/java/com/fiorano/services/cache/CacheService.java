/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cache;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.services.cache.engine.CacheEngine;
import com.fiorano.services.cache.transport.jms.CacheTransportManager;

/**
 * User: Venkat
 */
public class CacheService extends Service {

    public CacheService(IServiceLauncher launcher) {
        super(launcher);
    }

    protected String[] getLoggerNames() {
        return new String[]{"com.fiorano.services.cache.CacheService",
                "com.fiorano.services.cache.transport.jms",
                "com.fiorano.services.cache.engine"};
    }

    @Override
    protected void createEngine() throws ServiceExecutionException {
        engine = new CacheEngine(this, (CachePM) configuration);
    }

    @Override
    protected void createTransportManager() throws ServiceExecutionException {
        transportManager = new CacheTransportManager(this);
        transportManager.setTransportProvider(transportProvider);
        transportManager.setRequestProcessorFactory(engine);
    }

    @Override
    protected void createDefaultServiceConfiguration() {
        configuration = new CachePM();
    }

}
