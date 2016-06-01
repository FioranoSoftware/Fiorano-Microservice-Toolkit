/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessFactory;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.SessionConfiguration;
import com.fiorano.services.libraries.jms.helper.MQHelperException;
import com.fiorano.services.libraries.jms.pubsub.ServerSessionPool;
import com.fiorano.services.libraries.jms.pubsub.StaticSessionProvider;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractSyncIOTransportManager<RPF extends IRequestProcessFactory> extends AbstractTransportManager<RPF> {
    protected Map<String, Collection<TransportAssociation>> transports = new LinkedHashMap<>();
    protected Map<String, ConnectionConsumer> connectionConsumers = new LinkedHashMap<>();

    public AbstractSyncIOTransportManager(IService parent) {
        super(parent);
    }

    public abstract IJMSRequestListener createRequestListener(String name, TransportAssociation transportAssociation);

    @Override
    protected void internalStop() throws ServiceExecutionException {

        for (Collection<TransportAssociation> transportAssociationsList : transports.values()) {
            for (TransportAssociation transportAssociation : transportAssociationsList) {
                JMSInputTransport inputTransport = transportAssociation.getInputTransport();
                if (inputTransport.getListener() instanceof AbstractSyncIOMessageListener) {
                    ((AbstractSyncIOMessageListener) inputTransport.getListener()).stop();
                }
            }
        }

        super.internalStop();
    }

    @Override
    public void internalDestroy() throws ServiceExecutionException {
        super.internalDestroy();
        for (Collection<TransportAssociation> transportAssociationsList : transports.values()) {
            for (TransportAssociation transportAssociation : transportAssociationsList) {
                JMSErrorTransport errorTransport = transportAssociation.getErrorTransport();
                try {
                    errorTransport.getSession().close();
                } catch (JMSException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        for (ConnectionConsumer connectionConsumer : connectionConsumers.values()) {
            try {
                connectionConsumer.close();
            } catch (JMSException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        transports.clear();
        transports = null;
    }

    protected void createTransports() throws ServiceExecutionException {
        Collection<String> inputChannelNames = getInputChannelNames();
        for (String inputChannelName : inputChannelNames) {
            int sessionCount = getInputPortConfiguration(inputChannelName).getSessionConfiguration().getCount();
            Collection<TransportAssociation> transportAssociations = new ArrayList<>(sessionCount);
            Session[] sessions = new Session[sessionCount];
            for (int i = 0; i < sessionCount; i++) {
                TransportAssociation transportAssociation = createTransportAssociation(inputChannelName, getOutputChannelNames(inputChannelName), true);
                transportAssociations.add(transportAssociation);
                transportAssociation.getInputTransport().setRequestListener(createRequestListener(inputChannelName, transportAssociation));
                sessions[i] = transportAssociation.getInputTransport().getSession();
            }
            if (shouldUseSessionPool(getInputPortConfiguration(inputChannelName))) {
                ServerSessionPool sessionPool = new ServerSessionPool(new StaticSessionProvider(sessions), sessionCount);
                try {
                    connectionConsumers.put(inputChannelName, transportProvider.createConnectionConsumer(getInputTransportConfiguration(inputChannelName), sessionPool));
                } catch (MQHelperException e) {
                    e.printStackTrace();
                }
            }
            transports.put(inputChannelName, transportAssociations);
        }
    }

    protected TransportAssociation createTransportAssociation(String inputChannelName, Collection<String> outputChannelNames,
                                                              boolean associateErrorTransport) throws ServiceExecutionException {
        try {
            Session session;
            JMSInputTransport inputTransport = null;
            Map<String, JMSOutputTransport> outputTransports = null;
            JMSErrorTransport errorTransport = null;

            if (inputChannelName != null) {
                session = transportProvider.createSession(getInputPortConfiguration(inputChannelName));
                JMSInputTransportConfiguration inputTransportConfiguration = getInputTransportConfiguration(inputChannelName);
                inputTransport = transportProvider.createInputTransport(this, inputTransportConfiguration);
                inputTransport.setSession(session);
                boolean useSessionListener = shouldUseSessionPool(getInputPortConfiguration(inputChannelName));
                inputTransport.setUseSessionListener(useSessionListener);
            } else {
                session = transportProvider.getMqHelper().createSession(transportProvider.getConnection(),
                        new SessionConfiguration());
            }
            if (!outputChannelNames.isEmpty()) {
                outputTransports = new HashMap<>(outputChannelNames.size());
                for (String outputChannelName : outputChannelNames) {
                    JMSOutputTransport outputTransport = transportProvider.createOutputTransport(this, getOutputTransportConfiguration(outputChannelName));
                    outputTransport.setSession(session);
                    outputTransports.put(outputChannelName, outputTransport);
                }
            }
            if (associateErrorTransport) {
                errorTransport = transportProvider.createErrorTransport(this, getErrorTransportConfiguration());
                errorTransport.setSession(session);
            }
            return new TransportAssociation(inputTransport, outputTransports, errorTransport);
        } catch (MQHelperException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_INITIALIZE_TRANSPORT), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    protected Collection<String> getOutputChannelNames(String inputChannelName) {
        return getOutputChannelNames();
    }


    /**
     * Created by IntelliJ IDEA.
     * User: Venkat
     * Date: Nov 20, 2010
     * Time: 1:42:10 AM
     * To change this template use File | Settings | File Templates.
     */
    public static class TransportAssociation {
        private JMSInputTransport inputTransport;
        private Map<String, JMSOutputTransport> outputTransports;
        private JMSErrorTransport errorTransport;

        public TransportAssociation(JMSInputTransport inputTransport, Map<String, JMSOutputTransport> outputTransports, JMSErrorTransport errorTransport) {
            this.inputTransport = inputTransport;
            this.outputTransports = outputTransports;
            this.errorTransport = errorTransport;
        }

        public JMSInputTransport getInputTransport() {
            return inputTransport;
        }

        public Map<String, JMSOutputTransport> getOutputTransports() {
            return outputTransports;
        }

        public JMSErrorTransport getErrorTransport() {
            return errorTransport;
        }

        public JMSOutputTransport getOutputTransport(String type) {
            return outputTransports == null ? null : outputTransports.get(type);
        }
    }
}
