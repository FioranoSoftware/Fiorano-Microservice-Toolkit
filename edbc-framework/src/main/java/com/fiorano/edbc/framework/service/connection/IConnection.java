/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.connection;

import com.fiorano.edbc.framework.service.configuration.AbstractConnectionConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 9, 2010
 * Time: 12:08:53 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IConnection<CC extends AbstractConnectionConfiguration, CO> {
    boolean isValid();

    void init() throws ServiceExecutionException;

    void destroy() throws ServiceExecutionException;

    CO getConnection();

    CC getConnectionConfiguration();

    void addListener(IConnectionEventListener listener);

    void removeListener(IConnectionEventListener listener);

    void fireConnectionError();
}
