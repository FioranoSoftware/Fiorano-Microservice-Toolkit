/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.peer;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.transport.jms.JMSTransportProvider;
import com.fiorano.microservice.common.ccp.Bundle;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.util.ESBConstants;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 04-Feb-2011
 * Time: 00:27:29
 * To change this template use File | Settings | File Templates.
 */
public class PeerCommunicationsManager extends Module {
    private JMSTransportProvider transportProvider;
    private CCPEventManager ccpEventManager;
    private IService service;
    private Session session;

    public PeerCommunicationsManager(JMSTransportProvider transportProvider, IService service) {
        super(null);
        this.transportProvider = transportProvider;
        this.service = service;
    }

    @Override
    protected void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        try {
            session = transportProvider.getConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
            ccpEventManager = new CCPEventManager(service, session, logger);
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.COMMUNICATION_WITH_PEER_ESTABLISHED));
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_START_COMMUNICATION_WITH_PEER), e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
    }

    @Override
    protected void internalStart() throws ServiceExecutionException {
        super.internalStart();
        if (ccpEventManager != null) {
            try {
                ccpEventManager.start();
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            }
        }
    }

    @Override
    protected void internalStop() throws ServiceExecutionException {
        if (ccpEventManager != null) {
            try {
                ccpEventManager.stop();
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            }
        }
        super.internalStop();
    }

    @Override
    protected void internalDestroy() throws ServiceExecutionException {
        try {
            if (session != null) {
                try {
                    session.close();
                } finally {
                    session = null;
                }
            }
            if (ccpEventManager != null) {
                ccpEventManager = null;
            }
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.COMMUNICATION_WITH_PEER_ESTABLISHED));
        } catch (JMSException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_CLOSE_COMMUNICATION_WITH_PEER), e);
        }
        super.internalDestroy();
    }


    @Override
    public String getName() {
        return service.getLaunchConfiguration().getConnectionFactory() + ESBConstants.JNDI_CONSTANT + ClassUtil.getShortClassName(this.getClass());
    }

    public CCPEventManager getCcpEventManager() {
        return ccpEventManager;
    }
}
