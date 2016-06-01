/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.xmlverification;

import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.InMemoryServiceLauncher;

public class XmlVerificationServiceLauncher extends InMemoryServiceLauncher {
    public static void main(String[] args) throws ServiceException {
        XmlVerificationServiceLauncher xmlVerificationServiceLauncher = new XmlVerificationServiceLauncher();
        xmlVerificationServiceLauncher.launch(args);
    }

    @Override
    protected IService createService() {
        return new XmlVerificationService(this);
    }
}
