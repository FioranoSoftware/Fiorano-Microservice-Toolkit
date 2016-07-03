/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener;

import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.InMemoryServiceLauncher;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 17 Dec, 2010
 * Time: 11:16:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExceptionListenerServiceLauncher extends InMemoryServiceLauncher {

    @Override
    protected IService createService() {
        return new ExceptionListenerService(this);
    }

    public static void main(String[] args) throws ServiceException {
        ExceptionListenerServiceLauncher launcher = new ExceptionListenerServiceLauncher();
        launcher.launch(args);
    }
}
