/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.engine;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.edbc.framework.service.internal.connection.ConnectionManager;
import com.fiorano.edbc.framework.service.internal.engine.Engine;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.services.feeder.transport.jms.FeederTransportManager;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 22 Dec, 2010
 * Time: 11:17:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class FeederEngine<CM extends ConnectionManager> extends Engine<FeederPM, CM> {
    private FeederTransportManager transportManager = null;


    public FeederEngine(Service parent, FeederPM configuration, FeederTransportManager transportManager) {
        super(parent, configuration);
        this.transportManager = transportManager;
        parent.getChildren();
    }

    public FeederTransportManager getTransportManager() {
        return transportManager;
    }

    public IRequestProcessor createRequestProcessor(IModule parent, String type) {
        return null;
    }

}