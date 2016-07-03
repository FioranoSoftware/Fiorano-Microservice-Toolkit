/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.connection.IConnectionManager;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 9, 2010
 * Time: 11:29:27 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IEngine<C extends IServiceConfiguration, CM extends IConnectionManager> extends IModule, IRequestProcessFactory {
    CM getConnectionManager();

    C getConfiguration();
}
