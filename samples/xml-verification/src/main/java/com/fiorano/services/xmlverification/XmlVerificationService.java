/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.services.xmlverification.engine.XmlVerificationEngine;
import com.fiorano.services.xmlverification.transport.jms.XmlVerificationTransportManager;
import com.fiorano.xmlverification.model.XmlVerificationPM;


public class XmlVerificationService extends Service {

    public XmlVerificationService(IServiceLauncher launcher) {
        super(launcher);
    }

    @Override
    protected boolean isConfigurationMandatory() {
        return true;
    }

    @Override
    protected void createEngine() throws ServiceExecutionException {
        engine = new XmlVerificationEngine(this, (XmlVerificationPM) configuration);
    }

    @Override
    protected void createTransportManager() throws ServiceExecutionException {
        transportManager = new XmlVerificationTransportManager(this);
        transportManager.setTransportProvider(transportProvider);
        transportManager.setRequestProcessorFactory(engine);
    }

    @Override
    protected void createDefaultServiceConfiguration() {
        configuration = new XmlVerificationPM();
    }

    @Override
    protected String[] getLoggerNames() {
        return new String[]{"com.fiorano.services.xmlverification.XmlVerificationService",
                "com.fiorano.services.xmlverification.transport.jms",
                "com.fiorano.services.xmlverification.engine"};
    }
}