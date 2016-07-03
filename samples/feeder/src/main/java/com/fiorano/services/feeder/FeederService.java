/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.services.feeder.engine.FeederEngine;
import com.fiorano.services.feeder.runtime.swing.FeederFrame;
import com.fiorano.services.feeder.transport.jms.FeederTransportManager;


/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 22 Dec, 2010
 * Time: 11:17:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class FeederService extends Service {

    private FeederFrame feederFrame;

    public FeederEngine getFeederEngine() {
        return feederEngine;
    }

    private FeederEngine feederEngine;

    public FeederService(IServiceLauncher launcher) {
           super(launcher);
       }

       //-----------------------------------[Container API]------------------------------------
       @Override
       protected void internalCreate() throws ServiceExecutionException {
           transportManager = new FeederTransportManager(this);
           transportManager.setTransportProvider(transportProvider);
           super.internalCreate();
           feederFrame = new FeederFrame(this);
       }

       protected void createEngine() {
            feederEngine = new FeederEngine(this, (FeederPM) configuration, (FeederTransportManager) transportManager);
       }




       @Override
       protected void createDefaultServiceConfiguration() {
           configuration = new FeederPM();
       }

       protected String[] getLoggerNames() {
           return new String[]{"com.FeederService",
                   "com.fiorano.services.feeder.engine","com.fiorano.services.feeder.transport.jms"};
       }

       protected boolean isConfigurationMandatory() {
           return false;
       }

    @Override
    protected IConfigurationSerializer getConfigurationSerializer() {
        return new FeederConfigurationSerializer();
    }

}
