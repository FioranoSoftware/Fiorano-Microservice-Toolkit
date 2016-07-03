/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.transport.jms;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.JMSErrorTransport;
import com.fiorano.edbc.framework.service.internal.transport.jms.JMSOutputTransport;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.SessionConfiguration;
import com.fiorano.services.libraries.jms.helper.MQHelperException;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 22 Dec, 2010
 * Time: 11:17:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class FeederTransportManager extends AbstractTransportManager{

    private JMSOutputTransport feederTransport;
    private JMSErrorTransport errorTransport;
    private Session session;

    public FeederTransportManager(IService parent) {
        super(parent);
    }

    protected void createTransports() throws ServiceExecutionException {
        try {
            session = transportProvider.getMqHelper().createSession(transportProvider.getConnection(),
                    new SessionConfiguration());
            feederTransport = transportProvider.createOutputTransport(this, getOutputTransportConfiguration("OUT_PORT"));
            feederTransport.setSession(session);
            errorTransport = transportProvider.createErrorTransport(this, getErrorTransportConfiguration());
            errorTransport.setSession(session);
        } catch (MQHelperException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(com.fiorano.edbc.framework.service.internal.transport.Bundle.class, com.fiorano.edbc.framework.service.internal.transport.Bundle.FAILED_TO_INITIALIZE_TRANSPORT), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    @Override
    public void internalDestroy() throws ServiceExecutionException {
        super.internalDestroy();
        try {
            session.close();
        } catch (JMSException e) {
            //logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.),e);
        } finally {
            session = null;
        }
    }

    public JMSOutputTransport getFeederTransport() {
        return feederTransport;
    }

    public JMSErrorTransport getErrorTransport() {
        return errorTransport;
    }
}
