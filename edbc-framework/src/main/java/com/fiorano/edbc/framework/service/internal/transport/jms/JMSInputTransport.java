/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.transport.IInputTransport;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.ConsumerConfiguration;
import com.fiorano.services.libraries.jms.helper.MQHelper;
import com.fiorano.services.libraries.jms.helper.MQHelperException;
import fiorano.esb.util.ESBConstants;

import javax.jms.*;
import java.util.logging.Level;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSInputTransport extends Module implements IInputTransport<JMSInputTransportConfiguration, IJMSRequestListener, Message> {
    private MQHelper mqHelper;
    private Session session;
    private JMSInputTransportConfiguration configuration;
    private MessageConsumer consumer;
    private IJMSRequestListener listener;
    private boolean useSessionListener;

    public JMSInputTransport(IModule parent, MQHelper mqHelper, JMSInputTransportConfiguration configuration) {
        super(parent);
        this.mqHelper = mqHelper;
        this.configuration = configuration;
        this.logger = parent.getLogger();
    }

    public void setUseSessionListener(boolean useSessionListener) {
        this.useSessionListener = useSessionListener;
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    public void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        Destination destination = null;
        try {
            destination = mqHelper.fetchDestination(session, configuration.getDestinationConfiguration());
            ConsumerConfiguration consumerConfiguration = configuration.getConsumerConfiguration();
            if (!useSessionListener) {
                consumer = mqHelper.createConsumer(session, destination, consumerConfiguration);
            }
        } catch (MQHelperException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_INITIALIZE_INPUT_TRANSPORT, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
        try {
            if (useSessionListener) {
                session.setMessageListener(listener);
            } else {
                consumer.setMessageListener(listener);
            }
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SET_REQUEST_PROCESSOR, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INPUT_TRANSPORT_INITIALIZED));
    }

    public void internalStop() throws ServiceExecutionException {
        try {
            if (useSessionListener) {
                session.setMessageListener(null);
            } else {
                if (consumer != null) {
                    try {
                        consumer.setMessageListener(null);
                        consumer.close();
                    } finally {
                        consumer = null;
                    }
                }
            }
        } catch (JMSException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_DESTROY_INPUT_TRANSPORT), e);
        }
        super.internalStop();
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INPUT_TRANSPORT_DESTROYED));
    }

    public String getName() {
        return parent.getName() + ESBConstants.JNDI_CONSTANT + configuration.getDestinationConfiguration().getName();
    }

    //---------------------------------------------[ITransport API]----------------------------------------------------
    public JMSInputTransportConfiguration getConfiguration() {
        return configuration;
    }

    //---------------------------------------------[IInputTransport API]----------------------------------------------------
    public void setRequestListener(IJMSRequestListener listener) {
        try {
            setRequestListener(listener, false);
        } catch (ServiceExecutionException e) {
            // nothing to do here
        }
    }

    public void setRequestListener(IJMSRequestListener listener, boolean reload) throws ServiceExecutionException {
        this.listener = listener;
        if (reload) {
            try {
                if (useSessionListener) {
                    session.setMessageListener(listener);
                } else {
                    if (consumer != null) {
                        consumer.setMessageListener(listener);
                    }
                }
            } catch (JMSException e) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SET_REQUEST_PROCESSOR, new String[]{e.getMessage()}), ServiceErrorID.TRANSPORT_ERROR);
            }
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

    public MessageConsumer getConsumer() {
        return consumer;
    }

    public IJMSRequestListener getListener() {
        return listener;
    }
}
