/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display;


import com.fiorano.bc.display.model.ConfigurationPM;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.edbc.framework.service.internal.transport.ITransportProvider;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.esb.wrapper.IJNDILookupHelper;
import com.fiorano.services.display.engine.DisplayEngine;

import com.fiorano.services.display.runtime.swing.DisplayFrame;
import com.fiorano.services.display.transport.jms.DisplayTransportManager;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 9 Nov, 2010
 * Time: 6:36:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayService extends Service {

    private DisplayFrame displayFrame_model = null;

    public DisplayService(IServiceLauncher launcher) {
        super(launcher);
    }

    //-----------------------------------[Container API]------------------------------------

    @Override
    protected void createTransportManager() throws ServiceExecutionException {
        transportManager = new DisplayTransportManager(this);
        transportManager.setTransportProvider(transportProvider);
        transportManager.setRequestProcessorFactory(engine);
    }

    @SuppressWarnings({"CastToConcreteClass"})
    @Override
    protected void createEngine() throws ServiceExecutionException {
        displayFrame_model = new DisplayFrame(this);
        engine = new DisplayEngine(this, (ConfigurationPM) configuration, displayFrame_model);
    }

    @Override
    protected void createDefaultServiceConfiguration() {
        configuration = new ConfigurationPM();
    }

    protected String[] getLoggerNames() {
        return new String[]{"com.fiorano.services.display.DisplayService",
                "com.fiorano.services.display.engine",
                "com.fiorano.services.display.transport.jms"};
    }

    protected boolean isConfigurationMandatory() {
        return false;
    }

    @Override
    protected IConfigurationSerializer getConfigurationSerializer() {
        return new DisplayConfigurationSerializer();
    }

    @Override
    protected void internalStop() throws ServiceExecutionException {
        displayFrame_model.close();
        super.internalStop();
    }
}




















