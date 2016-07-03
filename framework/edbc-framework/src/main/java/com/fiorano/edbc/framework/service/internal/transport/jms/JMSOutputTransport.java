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
import com.fiorano.edbc.framework.service.internal.transport.IOutputTransport;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.ProducerConfiguration;
import com.fiorano.services.libraries.jms.helper.MQHelper;
import com.fiorano.services.libraries.jms.helper.MQHelperException;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.util.MessageUtil;
import fiorano.jms.services.msg.def.FioranoMessage;

import javax.jms.*;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSOutputTransport extends Module implements IOutputTransport<JMSOutputTransportConfiguration, Message> {
    private MQHelper mqHelper;
    private Session session;
    private JMSOutputTransportConfiguration configuration;
    private MessageProducer producer;

    public JMSOutputTransport(IModule parent, MQHelper mqHelper, JMSOutputTransportConfiguration configuration) {
        super(parent);
        this.mqHelper = mqHelper;
        this.configuration = configuration;
        this.logger = parent.getLogger();
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    public void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        Destination destination = null;
        try {
            destination = mqHelper.fetchDestination(session, configuration.getDestinationConfiguration());
            producer = mqHelper.createProducer(session, destination);
        } catch (MQHelperException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_INITIALIZE_OUTPUT_TRANSPORT, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
        }
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.OUTPUT_TRANSPORT_INITIALIZED));
    }

    public void internalDestroy() throws ServiceExecutionException {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (JMSException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_DESTROY_OUTPUT_TRANSPORT));
        }

        producer = null;
        super.internalDestroy();
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.OUTPUT_TRANSPORT_DESTROYED));
    }

    public String getName() {
        return parent.getName() + ESBConstants.JNDI_CONSTANT + configuration.getDestinationConfiguration().getName();
    }

    //---------------------------------------------[ITransport API]----------------------------------------------------
    public JMSOutputTransportConfiguration getConfiguration() {
        return configuration;
    }

    //---------------------------------------------[IOutputTransport API]----------------------------------------------------
    public void send(Message message) throws ServiceExecutionException {
        ProducerConfiguration producerConfiguration = configuration.getProducerConfiguration();
        ServiceDetails serviceDetails = configuration.getServiceDetails();
        try {
            //set service instance properties
            MessageUtil.setCompInstName(message, serviceDetails.getServiceName());
            MessageUtil.setEventProcessName(message, serviceDetails.getApplicationName());
            MessageUtil.setEventProcessVersion(message, String.valueOf(serviceDetails.getApplicationVersion()));
            //set message filters as properties
            HashMap filters = configuration.getMessageFilters();
            for (Object key : filters.keySet()) {
                message.setStringProperty((String) key, (String) filters.get(key));
            }
            if (message instanceof FioranoMessage)
                ((FioranoMessage) message).disableEncryption();

            producer.send(message, producerConfiguration.getDeliveryMode().getJMSValue(), producerConfiguration.getPriority(), producerConfiguration.getTtl());
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_SEND_RESPONSE, new String[]{e.getMessage()}), e, ServiceErrorID.TRANSPORT_ERROR);
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
