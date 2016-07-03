/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.engine;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.internal.transport.ITransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.pool.ObjectPool;

/**
 * Created by Deepthi on 7/16/2015.
 */
public class ProcessorPool extends ObjectPool<CBRRequestProcessor> {

    private ITransportManager transportManager;
    private IServiceConfiguration configuration;
    private AbstractSyncIOTransportManager.TransportAssociation transportAssociation;

    public ProcessorPool(ITransportManager transportManager, IServiceConfiguration configuration,
                         AbstractSyncIOTransportManager.TransportAssociation transportAssociation) {
        super(configuration.getPoolSize(), configuration.getPoolSize(), 1000);
        this.transportManager = transportManager;
        this.configuration = configuration;
        this.transportAssociation = transportAssociation;
        initialize(configuration.getPoolSize());
    }

    @Override
    protected CBRRequestProcessor createObject() {
        return new CBRRequestProcessor(transportManager, configuration, transportAssociation);
    }
}
