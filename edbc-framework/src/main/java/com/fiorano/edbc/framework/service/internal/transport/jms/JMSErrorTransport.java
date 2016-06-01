/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.transport.Bundle;
import com.fiorano.edbc.framework.service.internal.transport.IErrorTransport;
import com.fiorano.fw.error.FrameWorkException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.helper.MQHelper;
import com.fiorano.services.libraries.jms.helper.MQHelperException;
import com.fiorano.util.StringUtil;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.util.EventGenerator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.logging.Level;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSErrorTransport extends Module implements IErrorTransport<JMSOutputTransportConfiguration, Message> {
    private MQHelper mqHelper;
    private Session session;
    private JMSOutputTransportConfiguration configuration;
    private EventGenerator eventGenerator;

    public JMSErrorTransport(IModule parent, MQHelper mqHelper, JMSOutputTransportConfiguration configuration) {
        super(parent);
        this.mqHelper = mqHelper;
        this.configuration = configuration;
        this.eventGenerator = new EventGenerator();
        this.logger = parent.getLogger();
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    public void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        Destination errorDestination = null;
        try {
            errorDestination = mqHelper.fetchDestination(session, configuration.getDestinationConfiguration());
            eventGenerator.setErrorDestination(errorDestination);
            eventGenerator.setProducer(mqHelper.createProducer(session, null));
        } catch (MQHelperException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(com.fiorano.edbc.framework.service.internal.transport.Bundle.class, Bundle.FAILED_TO_INITIALIZE_ERROR_TRANSPORT, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public void inernalDestroy() throws ServiceExecutionException {
        try {
            eventGenerator.getProducer().close();
        } catch (JMSException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_DESTROY_ERROR_TRANSPORT));
        }
        eventGenerator = null;
        super.internalDestroy();
    }

    public String getName() {
        return parent.getName() + ESBConstants.JNDI_CONSTANT + configuration.getDestinationConfiguration().getName();
    }

    //---------------------------------------------[ITransport API]----------------------------------------------------
    public JMSOutputTransportConfiguration getConfiguration() {
        return configuration;
    }

    //---------------------------------------------[IErrorTransport API]----------------------------------------------------
    public void sendError(String errorCode, String errorMessage, Throwable th, Message request) throws ServiceExecutionException {
        try {
            if (errorMessage == null) {
                errorMessage = th.getMessage();
            }
            if (request != null && (!StringUtil.isEmpty(request.getStringProperty(Constants.COMPONENT_IN_TIME)))) {
                long inTime = Long.parseLong(request.getStringProperty(Constants.COMPONENT_IN_TIME));
                request.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTime));
            }
            eventGenerator.sendError(errorCode, errorMessage, th, request, session.createTextMessage());
        } catch (FrameWorkException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SEND_ERR_RESPONSE, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SEND_ERR_RESPONSE, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public void sendError(ServiceExecutionException th, Message request) throws ServiceExecutionException {
        try {
            if (request != null && (!StringUtil.isEmpty(request.getStringProperty(Constants.COMPONENT_IN_TIME)))) {
                long inTime = Long.parseLong(request.getStringProperty(Constants.COMPONENT_IN_TIME));
                request.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTime));
            }
            eventGenerator.sendError(th.getErrorID() != null ? th.getErrorID().getName() : null, th.getMessage(), th, request, session.createTextMessage());
        } catch (FrameWorkException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SEND_ERR_RESPONSE, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SEND_ERR_RESPONSE, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public void commit() throws ServiceExecutionException {
        try {
            if (session.getTransacted()) {
                session.commit();
            }
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_COMMIT_TRANSPORT, new String[]{configuration.getDestinationConfiguration().getName(), e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
