/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.exception.ServiceException;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 13, 2010
 * Time: 1:07:56 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IServiceLauncher {
    //-----------------------------------[Launcher API]-------------------------------
    void launch(String[] args) throws ServiceException;

    void terminate();
}
