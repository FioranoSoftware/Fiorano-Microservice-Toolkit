/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xslt;

import com.fiorano.edbc.framework.service.pool.ObjectPool;

public class ProcessorPool extends ObjectPool<RequestProcessor> {

    private JMSHandler jmsHandler;

    public ProcessorPool(int minIdle, int maxIdle, JMSHandler jmsHandler) {
        super(minIdle, maxIdle, 1000);
        this.jmsHandler = jmsHandler;
        initialize(minIdle);
    }

    @Override
    protected RequestProcessor createObject() {
        return new RequestProcessor(jmsHandler.getInputPortHandler().getInputPortInstanceAdapter().getSchema(),
                jmsHandler.getLogger(), jmsHandler.getServiceConfiguration());
    }
}