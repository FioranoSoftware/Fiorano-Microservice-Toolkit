/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.peer.PeerCommunicationsManager;
import com.fiorano.edbc.framework.service.internal.transport.ITransportManager;
import com.fiorano.edbc.framework.service.internal.transport.ITransportProvider;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.microservice.common.port.JNDILookupHelper;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 9, 2010
 * Time: 10:39:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IService extends IModule {

    //-----------------------------------[Container API]------------------------------------

    void initialize(CommandLineParameters launchConfiguration, ITransportProvider transportProvider,
                    PeerCommunicationsManager peerCommunicationsManager)
            throws ServiceExecutionException;

    void configure() throws ServiceException;

    CommandLineParameters getLaunchConfiguration();

    ITransportProvider getTransportProvider();

    //-----------------------------------[Service related API]-------------------------------
    IServiceConfiguration getConfiguration();

    IEngine getEngine();

    ITransportManager getTransportManager();

    IServiceLauncher getLauncher();

    Logger getLogger(String loggerName);

    String getServiceLookupName();

    void updateConfiguration(Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration);

    void clearOutLogs();

    void clearErrLogs();

    MonitorFactory getMonitorFactory();

    public void destroyLogHandlers();


}
