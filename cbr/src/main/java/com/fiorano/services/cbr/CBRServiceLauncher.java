/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr;

import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.InMemoryServiceLauncher;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 27 Dec, 2010
 * Time: 2:58:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class CBRServiceLauncher extends InMemoryServiceLauncher {


    public static void main(String[] args) throws ServiceException {
        CBRServiceLauncher cbrServiceLauncher = new CBRServiceLauncher();
        cbrServiceLauncher.launch(args);
    }

    @Override
    protected IService createService() {
        return new CBRService(this);
    }
}
