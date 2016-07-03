/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.ServiceUtil;
import com.fiorano.edbc.framework.service.internal.connection.IConnectionManager;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 11, 2010
 * Time: 11:38:28 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Engine<C extends IServiceConfiguration, CM extends IConnectionManager> extends Module implements IEngine<C, CM> {

    //---------------------------------------------[IEngine API]----------------------------------------------------
    protected C configuration;
    protected CM connectionManager;

    public Engine(IModule parent, C configuration) {
        super(parent);
        this.configuration = configuration;
        setLogger(ServiceUtil.getLogger(this, this.getClass().getPackage().getName().toUpperCase()));
    }

    public C getConfiguration() {
        return configuration;
    }

    public CM getConnectionManager() {
        return connectionManager;
    }
}
