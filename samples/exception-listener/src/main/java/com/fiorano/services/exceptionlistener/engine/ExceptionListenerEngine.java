/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener.engine;


import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.edbc.framework.service.internal.connection.ConnectionManager;
import com.fiorano.edbc.framework.service.internal.engine.Engine;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.services.exceptionlistener.ExceptionListenerService;
import com.fiorano.services.exceptionlistener.configuration.ExceptionListenerConfiguration;
import com.fiorano.services.exceptionlistener.transport.jms.ExceptionListenerTransportManager;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 17 Dec, 2010
 * Time: 11:13:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExceptionListenerEngine<CM extends ConnectionManager> extends Engine<ExceptionListenerConfiguration, CM> {
    private ExceptionListenerTransportManager transportManager = null;
    private ExceptionListenerJob exceptionListenerJob = null;

    public ExceptionListenerEngine(Service parent, ExceptionListenerConfiguration configuration, ExceptionListenerTransportManager transportManager) {
        super(parent, configuration);
        this.transportManager = transportManager;
    }

    public void internalCreate() throws ServiceExecutionException {
        exceptionListenerJob = new ExceptionListenerJob(this);
        super.internalCreate();
    }

    public void internalStart() throws ServiceExecutionException {
        exceptionListenerJob.init();
    }


    public ExceptionListenerTransportManager getTransportManager() {
        return transportManager;
    }


    public IRequestProcessor createRequestProcessor(IModule parent, String type) {
        return null;
    }

    @Override
    public void internalDestroy() {
        exceptionListenerJob.closeConnections();
    }

    public boolean isInMemory() {
        return ((ExceptionListenerService)getParent()).getLaunchConfiguration().isInmemoryLaunchable();
    }

}
