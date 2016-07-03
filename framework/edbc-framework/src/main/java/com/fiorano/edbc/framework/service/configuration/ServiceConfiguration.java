/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.util.ErrorListener;

/**
 * <code>ServiceConfiguration</code> should be extended by services connect to an EIS.
 * The configuration details required for creating connection are also captured here.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @fiorano.xmbean
 */
public abstract class ServiceConfiguration extends ConnectionlessServiceConfiguration {
    protected AbstractConnectionConfiguration connectionConfiguration;

    protected ServiceConfiguration() {
        errorHandlingConfiguration = new ErrorHandlingConfiguration();
    }

    /**
     * Returns configuration details for creating connection
     *
     * @jmx.managed-attribute access="read-write" description="Configuration details for creating connection"
     * @jmx.descriptor name="displayName" value="Connection configuration"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     */
    public AbstractConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * sets configuration details for creating connection
     *
     * @jmx.managed-attribute
     */
    public void setConnectionConfiguration(AbstractConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    public void validate(ErrorListener listener) throws ServiceConfigurationException {
        if (connectionConfiguration != null) {
            connectionConfiguration.validate(listener);
        }
        super.validate(listener);
    }
}
