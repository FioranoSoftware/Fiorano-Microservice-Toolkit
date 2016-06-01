/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.display.model;

import com.fiorano.edbc.framework.service.configuration.AbstractErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.util.ErrorListener;

import java.net.URL;

/**
 * @author FSIPL
 * @created April 15, 2005
 * @version 1.0
 * @jboss.xmbean
 * @fiorano.xmbean
 * @jmx.mbean description="Stores the configuration for display properties."
 */
public class ConfigurationPM extends ConnectionlessServiceConfiguration {
    private int     maxBufferedMessages = 10;

    /**
     * @return
     * @jmx.managed-attribute access="read-write"
     *    description="Max Messages that can be buffered"
     * @jmx.descriptor name="defaultValue" value="10"
     * @jmx.descriptor name="dynamic" value="true"
     * @jmx.descriptor name="minValue" value="1"
     */
    public int getMaxBufferedMessages()
    {
        return maxBufferedMessages;
    }

    /**
     * @param maxBufferedMessages
     * @jmx.managed-attribute
     */
    public void setMaxBufferedMessages(int maxBufferedMessages)
    {
        this.maxBufferedMessages = maxBufferedMessages;
    }

    @Override
    public void test() throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAsFormattedString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
