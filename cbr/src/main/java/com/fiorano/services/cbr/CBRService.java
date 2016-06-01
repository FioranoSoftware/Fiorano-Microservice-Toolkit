/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IServiceLauncher;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.services.cbr.transport.jms.CBRTransportManager;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 27 Dec, 2010
 * Time: 2:58:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CBRService extends Service {


    public final static String USE_FIORANO_CBR = "usefioranocbr";

    public CBRService(IServiceLauncher launcher) {
        super(launcher);
    }

    @Override
    protected String[] getLoggerNames() {
        return new String[]{"com.fiorano.services.cbr.CBRService", "com.fiorano.services.cbr.transport.jms"};
    }

    protected boolean isConfigurationMandatory() {
        return true;
    }

    @Override
    protected void createDefaultServiceConfiguration() {
        configuration = new CBRPropertyModel();
        boolean usefioranoCBR = (Boolean.valueOf(String.valueOf(this.getLaunchConfiguration().getParameter(USE_FIORANO_CBR))));
        ((CBRPropertyModel) configuration).setFioranoCBR(usefioranoCBR);
    }

    @Override
    protected void createTransportManager() throws ServiceExecutionException {
        transportManager = new CBRTransportManager(this);
        transportManager.setTransportProvider(transportProvider);
    }
}
