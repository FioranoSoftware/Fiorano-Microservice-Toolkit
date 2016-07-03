/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.internal.transport.IOutputTransportConfiguration;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.libraries.jms.configuration.DestinationConfiguration;
import com.fiorano.services.libraries.jms.configuration.ProducerConfiguration;

import java.util.HashMap;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSOutputTransportConfiguration implements IOutputTransportConfiguration {
    private DestinationConfiguration destinationConfiguration;
    private ProducerConfiguration producerConfiguration;
    private ServiceDetails serviceDetails;
    private HashMap messageFilters;
    private ISchema schema;
    private String name;

    public JMSOutputTransportConfiguration(String name, ISchema schema) {
        this.name = name;
        this.schema = schema;
    }

    public HashMap getMessageFilters() {
        return messageFilters;
    }

    public void setMessageFilters(HashMap messageFilters) {
        this.messageFilters = messageFilters;
    }

    public ServiceDetails getServiceDetails() {
        return serviceDetails;
    }

    public void setServiceDetails(ServiceDetails serviceDetails) {
        this.serviceDetails = serviceDetails;
    }

    public DestinationConfiguration getDestinationConfiguration() {
        return destinationConfiguration;
    }

    public void setDestinationConfiguration(DestinationConfiguration destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
    }

    public ProducerConfiguration getProducerConfiguration() {
        return producerConfiguration;
    }

    public void setProducerConfiguration(ProducerConfiguration producerConfiguration) {
        this.producerConfiguration = producerConfiguration;
    }

    public ISchema getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }
}
