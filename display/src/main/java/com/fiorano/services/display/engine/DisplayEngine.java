/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.engine;


import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.edbc.framework.service.internal.connection.ConnectionManager;
import com.fiorano.edbc.framework.service.internal.engine.Engine;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.services.display.runtime.swing.DisplayFrame;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 8 Nov, 2010
 * Time: 7:12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayEngine<CM extends ConnectionManager> extends Engine<ConfigurationPM, CM> {
    private DisplayFrame displayFrame;

    public DisplayEngine(Service parent, ConfigurationPM configuration, DisplayFrame displayFrame) {
        super(parent, configuration);
        this.displayFrame = displayFrame;
    }


    public IRequestProcessor createRequestProcessor(IModule parent, String type) {
        return new DisplayRequestProcessor(parent, displayFrame);
    }


}
