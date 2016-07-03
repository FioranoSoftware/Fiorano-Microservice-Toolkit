/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.peer;

import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.microservice.common.ccp.AbstractCCPEventManager;
import com.fiorano.microservice.common.ccp.ICCPEventGenerator;
import com.fiorano.microservice.common.ccp.ICCPEventHandler;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.logging.Logger;

/**
 * The CCPEventManager manages the life cycle of the objects created for handling the communication
 * with the peer server.
 * <p/>
 * Date: Feb 28, 2010
 * Time: 7:23:47 PM
 */
public class CCPEventManager extends AbstractCCPEventManager {

    private IService service;

    public CCPEventManager(IService service, Session session, Logger logger) {
        this.service = service;
        this.session = session;
        this.logger = logger;
    }

    @Override
    protected String getComponentID() {
        return service.getServiceLookupName();
    }

    @Override
    protected ICCPEventGenerator createCCPEventGenerator() {
        return new CCPEventGenerator(producer, session, service, logger);
    }

    @Override
    protected ICCPEventHandler createCCPEventHandler() throws JMSException {
        return new CCPEventHandler(service, ccpEventGenerator, logger);
    }

    @Override
    protected Session createSession() throws JMSException {
        return session;
    }
}