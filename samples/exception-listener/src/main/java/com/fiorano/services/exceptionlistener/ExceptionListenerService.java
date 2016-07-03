/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener;


import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.services.exceptionlistener.configuration.ExceptionListenerConfiguration;
import com.fiorano.services.exceptionlistener.engine.ExceptionListenerEngine;
import com.fiorano.services.exceptionlistener.transport.jms.ExceptionListenerTransportManager;


/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 17 Dec, 2010
 * Time: 11:16:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExceptionListenerService extends Service {

    public ExceptionListenerService(IServiceLauncher launcher) {
        super(launcher);
    }

    //-----------------------------------[Container API]------------------------------------
    @Override
    protected void internalCreate() throws ServiceExecutionException {
        transportManager = new ExceptionListenerTransportManager(this);
        transportManager.setTransportProvider(transportProvider);
        super.internalCreate();
    }


    protected void createEngine() throws ServiceExecutionException{
        engine = new ExceptionListenerEngine(this, (ExceptionListenerConfiguration) configuration, (ExceptionListenerTransportManager) transportManager);
    }



    @Override
    protected void createDefaultServiceConfiguration() {
        configuration = new ExceptionListenerConfiguration();
    }

    protected String[] getLoggerNames() {
        return new String[]{"com.fiorano.services.exception.ExceptionListenerService",
                "com.fiorano.services.exception.engine","com.fiorano.services.exception.transport.jms"};
    }

    protected boolean isConfigurationMandatory() {
        return true;
    }

}
