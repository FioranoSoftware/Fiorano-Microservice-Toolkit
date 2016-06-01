/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.IService;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 18, 2010
 * Time: 4:40:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ITransportProvider extends IModule {
    IInputTransport createInputTransport(IModule parent, IInputTransportConfiguration transportConfiguration) throws ServiceExecutionException;

    IOutputTransport createOutputTransport(IModule parent, IOutputTransportConfiguration transportConfiguration) throws ServiceExecutionException;

    IErrorTransport createErrorTransport(IModule parent, IOutputTransportConfiguration transportConfiguration) throws ServiceExecutionException;

    void addService(IService service);

    void removeService(IService service);
}
