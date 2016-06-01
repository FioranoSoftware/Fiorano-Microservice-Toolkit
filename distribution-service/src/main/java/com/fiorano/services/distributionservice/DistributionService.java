/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.services.distributionservice.configuration.DistributionConfigurationSerializer;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;
import com.fiorano.services.distributionservice.transport.jms.DistributionServiceTransportManager;

/**
 * Distribution service
 *
 * @author FSIPL
 * @version 1.0
 * @created July 08, 2005
 */
public class DistributionService extends Service {
    public DistributionService(IServiceLauncher launcher) {
        super(launcher);
    }

    @Override
    protected String[] getLoggerNames() {
        return new String[]{"com.fiorano.services.distributionservice.DistributionService", "com.fiorano.services.distributionservice.engine", "com.fiorano.services.distributionservice.transport.jms", "com.fiorano.services.distributionservice.engine"};
    }

    @Override
    protected void createDefaultServiceConfiguration() {
        configuration = new DistributionServicePM();
    }

    @Override
    protected void createTransportManager() throws ServiceExecutionException {
        transportManager = new DistributionServiceTransportManager(this);
        transportManager.setTransportProvider(transportProvider);
    }

    @Override
    protected IConfigurationSerializer getConfigurationSerializer() {
        return new DistributionConfigurationSerializer();
    }
}