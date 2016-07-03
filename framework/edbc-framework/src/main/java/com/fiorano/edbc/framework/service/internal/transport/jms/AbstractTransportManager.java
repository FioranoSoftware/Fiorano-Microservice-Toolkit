/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessFactory;
import com.fiorano.edbc.framework.service.internal.transport.ITransportManager;
import com.fiorano.microservice.common.port.InputPortInstanceAdapter;
import com.fiorano.microservice.common.port.OutputPortInstanceAdapter;
import com.fiorano.openesb.application.application.InputPortInstance;
import com.fiorano.openesb.application.application.OutputPortInstance;
import com.fiorano.openesb.application.application.PortInstance;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 23-Nov-2010
 * Time: 15:43:50
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTransportManager<RPF extends IRequestProcessFactory> extends Module implements ITransportManager<JMSTransportProvider, RPF> {
    private static final String ERROR_PORT_NAME = "ON_EXCEPTION";
    protected JMSTransportProvider transportProvider;
    protected RPF requestProcessFactory;
    private Map<String, InputPortInstanceAdapter> inputConfigurationsMap = new HashMap<>();
    private Map<String, OutputPortInstanceAdapter> outputConfigurationsMap = new HashMap<>();
    private OutputPortInstanceAdapter errorConfiguration;

    public AbstractTransportManager(IService parent) {
        super(parent);
        setLogger(parent.getLogger(this.getClass().getPackage().getName().toUpperCase()));
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    @Override
    public void internalCreate() throws ServiceExecutionException {

        List<PortInstance> inputPorts = portInstances.get("IN_PORTS");
        for (PortInstance portInstance : inputPorts) {
            InputPortInstanceAdapter inputPortInstanceAdapter = new InputPortInstanceAdapter((InputPortInstance) portInstance);
            if (inputPortInstanceAdapter.isEnabled()) {
                inputConfigurationsMap.put(inputPortInstanceAdapter.getName(), inputPortInstanceAdapter);
            }
        }
        List<PortInstance> outputPorts = portInstances.get("OUT_PORTS");
        for (PortInstance portInstance : outputPorts) {
            OutputPortInstanceAdapter outputPortInstanceAdapter = new OutputPortInstanceAdapter((OutputPortInstance) portInstance);
            if (outputPortInstanceAdapter.isEnabled()) {
                if (ERROR_PORT_NAME.equals(outputPortInstanceAdapter.getName())) {
                    errorConfiguration = outputPortInstanceAdapter;
                } else {
                    outputConfigurationsMap.put(outputPortInstanceAdapter.getName(), outputPortInstanceAdapter);
                }
            }
        }
        createTransports();
        for (IModule child : getChildren()) {
            child.create();
        }
    }

    @Override
    protected void internalDestroy() throws ServiceExecutionException {
        super.internalDestroy();
        errorConfiguration = null;
        inputConfigurationsMap.clear();
        inputConfigurationsMap = null;
        outputConfigurationsMap.clear();
        outputConfigurationsMap = null;
    }

    protected boolean shouldUseSessionPool(InputPortInstanceAdapter inputPortInstanceAdapter) {
        return InputPortInstanceAdapter.DESTINATION_TYPE_TOPIC == inputPortInstanceAdapter.getDestinationType()
                && inputPortInstanceAdapter.getSessionConfiguration().getCount() > 1;
    }

    public void setTransportProvider(JMSTransportProvider transportProvider) {
        this.transportProvider = transportProvider;
    }

    public void setRequestProcessorFactory(RPF requestProcessorFactory) {
        this.requestProcessFactory = requestProcessorFactory;
    }

    @Override
    public IService getParent() {
        return (IService) super.getParent();
    }

    protected abstract void createTransports() throws ServiceExecutionException;

    public String getErrorChannelName() {
        return ERROR_PORT_NAME;
    }

    public Collection<String> getInputChannelNames() {
        return inputConfigurationsMap.keySet();
    }

    public Collection<String> getOutputChannelNames() {
        return outputConfigurationsMap.keySet();
    }

    public InputPortInstanceAdapter getInputPortConfiguration(String name) {
        return inputConfigurationsMap.get(name);
    }

    public OutputPortInstanceAdapter getOutputPortConfiguration(String name) {
        return outputConfigurationsMap.get(name);
    }

    public OutputPortInstanceAdapter getErrorPortConfiguration() {
        return errorConfiguration;
    }

    public JMSInputTransportConfiguration getInputTransportConfiguration(String name) {
        return transportProvider.createInputTransportConfiguration(inputConfigurationsMap.get(name));
    }

    public JMSOutputTransportConfiguration getOutputTransportConfiguration(String name) {
        return transportProvider.createOutputTransportConfiguration(outputConfigurationsMap.get(name));
    }

    public JMSOutputTransportConfiguration getErrorTransportConfiguration() {
        return transportProvider.createOutputTransportConfiguration(errorConfiguration);
    }

    protected Map<String, List<PortInstance>> portInstances;

    @Override
    public void setPortInstances(Map<String, List<PortInstance>> portInstances) {
        this.portInstances = portInstances;
    }
}
