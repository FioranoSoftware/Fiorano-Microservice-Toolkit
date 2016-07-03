/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport;

import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessFactory;
import com.fiorano.openesb.application.application.PortInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface ITransportManager<TP extends ITransportProvider, RPF extends IRequestProcessFactory> extends IModule {
    void setTransportProvider(TP transportProvider);

    void setRequestProcessorFactory(RPF requestProcessorFactory);

    String getErrorChannelName();

    Collection<String> getInputChannelNames();

    Collection<String> getOutputChannelNames();

    IInputTransportConfiguration getInputTransportConfiguration(String name);

    IOutputTransportConfiguration getOutputTransportConfiguration(String name);

    IOutputTransportConfiguration getErrorTransportConfiguration();

    void setPortInstances(Map<String, List<PortInstance>> portInstances);
}
