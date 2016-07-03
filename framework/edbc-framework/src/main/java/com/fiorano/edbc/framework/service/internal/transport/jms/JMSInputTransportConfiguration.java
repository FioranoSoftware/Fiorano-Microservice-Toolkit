/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.internal.transport.IInputTransportConfiguration;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.libraries.jms.configuration.ConsumerConfiguration;
import com.fiorano.services.libraries.jms.configuration.DestinationConfiguration;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSInputTransportConfiguration implements IInputTransportConfiguration, Cloneable {
    private DestinationConfiguration destinationConfiguration;
    private ConsumerConfiguration consumerConfiguration;
    private ISchema schema;
    private String name;

    public JMSInputTransportConfiguration(String name, ISchema schema) {
        this.name = name;
        this.schema = schema;
    }

    public DestinationConfiguration getDestinationConfiguration() {
        return destinationConfiguration;
    }

    public void setDestinationConfiguration(DestinationConfiguration destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
    }

    public ConsumerConfiguration getConsumerConfiguration() {
        return consumerConfiguration;
    }

    public void setConsumerConfiguration(ConsumerConfiguration consumerConfiguration) {
        this.consumerConfiguration = consumerConfiguration;
    }

    public ISchema getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public Object clone() throws CloneNotSupportedException {
        JMSInputTransportConfiguration jMSInputTransportConfiguration = (JMSInputTransportConfiguration) super.clone();
        jMSInputTransportConfiguration.setConsumerConfiguration(getConsumerConfiguration());
        jMSInputTransportConfiguration.setDestinationConfiguration(getDestinationConfiguration());
        return jMSInputTransportConfiguration;
    }

}
