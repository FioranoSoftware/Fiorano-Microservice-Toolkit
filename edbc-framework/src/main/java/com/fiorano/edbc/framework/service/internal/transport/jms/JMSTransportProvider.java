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
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.transport.IInputTransportConfiguration;
import com.fiorano.edbc.framework.service.internal.transport.IOutputTransportConfiguration;
import com.fiorano.edbc.framework.service.internal.transport.ITransportProvider;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.esb.wrapper.ILookupConfiguration;
import com.fiorano.microservice.common.port.InputPortInstanceAdapter;
import com.fiorano.microservice.common.port.JNDILookupHelper;
import com.fiorano.microservice.common.port.OutputPortInstanceAdapter;
import com.fiorano.microservice.common.port.PortInstanceAdapter;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.*;
import com.fiorano.services.libraries.jms.configuration.DeliveryMode;
import com.fiorano.services.libraries.jms.helper.MQHelper;
import com.fiorano.services.libraries.jms.helper.MQHelperException;
import com.fiorano.services.libraries.jms.helper.MQHelperFactory;
import com.fiorano.util.StringUtil;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.util.ESBConstants;
import fiorano.jms.runtime.naming.FioranoJNDIContext;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSTransportProvider extends Module implements ITransportProvider {

    private JNDILookupHelper lookupHelper;
    private String destinationLookupPrefix;
    private ConnectionConfiguration connectionConfiguration;
    private Connection connection;
    private MQHelper mqHelper;
    private List<IService> services = new LinkedList<>();
    private CommandLineParameters commandLineParameters;

    public JMSTransportProvider(CommandLineParameters params, JNDILookupHelper lookupHelper) {
        super(null);
        this.lookupHelper = lookupHelper;
        this.commandLineParameters = params;
        ILookupConfiguration lookupConfiguration = params;
        JNDIConfiguration jndiConfiguration = new JNDIConfiguration();
        if(params.getParameter("-icf") == null){
            jndiConfiguration.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        }else{
            jndiConfiguration.setInitialContextFactory((String) params.getParameter("-icf"));
        }
        jndiConfiguration.setUsername(lookupConfiguration.getUsername());
        jndiConfiguration.setPassword(lookupConfiguration.getPassword());
        HashMap<String, Object> additionalProperties = new HashMap<>();
        if (!StringUtil.isEmpty(lookupConfiguration.getSecurityProtocol())) {
            additionalProperties.put(Context.SECURITY_PROTOCOL, lookupConfiguration.getSecurityProtocol());
        }
        if (!StringUtil.isEmpty(lookupConfiguration.getSecurityManager())) {
            additionalProperties.put(FioranoJNDIContext.SSL_SECURITY_MANAGER, lookupConfiguration.getSecurityManager());
        }
        if (!StringUtil.isEmpty(lookupConfiguration.getTransportProtocol())) {
            additionalProperties.put(FioranoJNDIContext.TRANSPORT_PROTOCOL, lookupConfiguration.getTransportProtocol());
        }
        if (lookupConfiguration.getAdditionalEnvProperties() != null) {
            additionalProperties.putAll(lookupConfiguration.getAdditionalEnvProperties());
        }
        jndiConfiguration.setAdditionalProperties(additionalProperties);

        ConnectionFactoryConfiguration cfConfiguration = new ConnectionFactoryConfiguration();
        cfConfiguration.setUrl(lookupConfiguration.getURL());
        cfConfiguration.setBackupURLs(lookupConfiguration.getBackupURL());
        cfConfiguration.setLookupCF(true);
        cfConfiguration.setCfLookupName(lookupConfiguration.getConnectionFactory());
        cfConfiguration.setJndiConfiguration(jndiConfiguration);

        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration();
        connectionConfiguration.setClientID(params.getApplicationName() + "__" + String.valueOf(params.getApplicationVersion()).replace(".", "_") + "__" + params.getServiceInstanceName());
        connectionConfiguration.setUsername(params.getUsername());
        connectionConfiguration.setPassword(params.getPassword());
        connectionConfiguration.setUnifiedDomainSupported(true);
        connectionConfiguration.setCfConfiguration(cfConfiguration);
        connectionConfiguration.setClientIDDefined(false);

        this.connectionConfiguration = connectionConfiguration;
        this.destinationLookupPrefix = commandLineParameters.getApplicationName() + ESBConstants.JNDI_CONSTANT
                + String.valueOf(commandLineParameters.getApplicationVersion()).replace(".","_") + ESBConstants.JNDI_CONSTANT
                + commandLineParameters.getServiceInstanceName() + ESBConstants.JNDI_CONSTANT;
    }

    public static String createSelectorFromFilters(String messageSelector, HashMap messageFilters) {

        if (StringUtil.isEmpty(messageSelector) && (messageFilters == null || messageFilters.isEmpty())) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        if (!StringUtil.isEmpty(messageSelector)) {
            builder.append("(").append(messageSelector).append(")");
        }
        if (messageFilters != null && messageFilters.size() > 0) {
            if (builder.length() > 0) { //selector present
                builder.append(Constants.AND_OPERATOR);
            }
            builder.append("(");
            for (Object key : messageFilters.keySet()) {
                builder.append(key).append("='").append(messageFilters.get(key)).append("'");
                builder.append(Constants.AND_OPERATOR);
            }
            builder.delete(builder.length() - 5, builder.length()); //remove last AND_OP
            builder.append(")");
        }

        return builder.toString();
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    @SuppressWarnings({"CastToConcreteClass"})
    public void internalCreate() throws ServiceExecutionException {
        this.mqHelper = MQHelperFactory.createMQHelper(JMSImplementation.JBOSS, logger);
        try {
            mqHelper.initialize(connectionConfiguration.getCfConfiguration());
            ConnectionFactory connectionFactory = lookupHelper.lookupConnectionFactory();
            connection = mqHelper.createConnection(connectionFactory, connectionConfiguration);
        } catch (MQHelperException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_INITIALIZE_TRANSPORT), e, ServiceErrorID.TRANSPORT_ERROR);
        } catch (NamingException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_INITIALIZE_TRANSPORT), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public void internalStart() throws ServiceExecutionException {
        try {
            connection.start();
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_START_TRANSPORT), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public void internalStop() throws ServiceExecutionException {
        super.internalStop();
        try {
            if (connection != null)
                connection.stop();
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_STOP_TRANSPORT), e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    public void internalDestroy() throws ServiceExecutionException {
        if (mqHelper != null) {
            mqHelper.close();
            mqHelper = null;
        }
        if (connection != null) {
            try {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TRANSPORT_STOPPED));
                connection.close();
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TRANSPORT_DESTROYED));
            } catch (JMSException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_DESTROY_TRANSPORT));
            } finally {
                connection = null;
            }
        }
    }

    @Override
    public String getName() {
        return connectionConfiguration.getClientID() + ESBConstants.JNDI_CONSTANT + ClassUtil.getShortClassName(this.getClass());
    }

    public JNDILookupHelper getLookupHelper() {
        return lookupHelper;
    }

    public Connection getConnection() {
        return connection;
    }

    public MQHelper getMqHelper() {
        return mqHelper;
    }

    public Session createSession(InputPortInstanceAdapter inputPort) throws MQHelperException {
        return mqHelper.createSession(connection, convertSessionConfiguration(inputPort.getSessionConfiguration()));
    }

    public ConnectionConsumer createConnectionConsumer(JMSInputTransportConfiguration inputTransportConfiguration, ServerSessionPool sessionPool) throws MQHelperException {
        return mqHelper.createConnectionConsumer(connection, mqHelper.fetchDestination(null, inputTransportConfiguration.getDestinationConfiguration()), inputTransportConfiguration.getConsumerConfiguration(), sessionPool, 1);
    }

    protected JMSInputTransportConfiguration createInputTransportConfiguration(InputPortInstanceAdapter inputPort) {
        DestinationConfiguration destinationConfiguration = new DestinationConfiguration();
        if(inputPort.getPortInstance().isSpecifiedDestinationUsed() && inputPort.getPortInstance().getDestination() != null){
            destinationConfiguration.setName(inputPort.getPortInstance().getDestination());
        }else{
            destinationConfiguration.setName(destinationLookupPrefix + inputPort.getName());
        }
        destinationConfiguration.setLookupSupported(false);
        destinationConfiguration.setAutoCreateSupported(true);
        destinationConfiguration.setType(convertDestinationType(inputPort.getDestinationType()));

        ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration();
        consumerConfiguration.setListenerMode(ListenerMode.ASYNCHRONOUS);
        consumerConfiguration.setDurable(inputPort.isDurableSubscription());
        consumerConfiguration.setSubscriptionName(inputPort.getSubscriptionName());
        consumerConfiguration.setSelector(createSelectorFromFilters(inputPort.getMessageSelector(), inputPort.getMessageFilters()));

        JMSInputTransportConfiguration inputTransportConfiguration = new JMSInputTransportConfiguration(inputPort.getName(), inputPort.getSchema());
        inputTransportConfiguration.setConsumerConfiguration(consumerConfiguration);
        inputTransportConfiguration.setDestinationConfiguration(destinationConfiguration);

        return inputTransportConfiguration;
    }

    protected JMSOutputTransportConfiguration createOutputTransportConfiguration(OutputPortInstanceAdapter outputPort) {
        DestinationConfiguration destinationConfiguration = new DestinationConfiguration();
        if(outputPort.getPortInstance().isSpecifiedDestinationUsed() && outputPort.getPortInstance().getDestination() != null){
            destinationConfiguration.setName(outputPort.getPortInstance().getDestination());
        }else{
            destinationConfiguration.setName(destinationLookupPrefix + outputPort.getName());
        }
        destinationConfiguration.setLookupSupported(false);
        destinationConfiguration.setAutoCreateSupported(true);
        destinationConfiguration.setType(convertDestinationType(outputPort.getDestinationType()));

        ProducerConfiguration producerConfiguration = new ProducerConfiguration();
        producerConfiguration.setDeliveryMode(convertDeliveryMode(outputPort.getDeliveryMode()));
        producerConfiguration.setPriority(outputPort.getPriority());
        producerConfiguration.setTtl(outputPort.getTimeToLive());

        ServiceDetails serviceDetails = new ServiceDetails(commandLineParameters.getNodeName(), commandLineParameters.getApplicationName(),
                commandLineParameters.getServiceGUID(), commandLineParameters.getServiceInstanceName(), String.valueOf(commandLineParameters.getApplicationVersion()));

        JMSOutputTransportConfiguration outputTransportConfiguration = new JMSOutputTransportConfiguration(outputPort.getName(), outputPort.getSchema());
        outputTransportConfiguration.setDestinationConfiguration(destinationConfiguration);
        outputTransportConfiguration.setProducerConfiguration(producerConfiguration);
        outputTransportConfiguration.setServiceDetails(serviceDetails);
        outputTransportConfiguration.setMessageFilters(outputPort.getMessageFilters());
        return outputTransportConfiguration;
    }

    private DeliveryMode convertDeliveryMode(int deliveryMode) {
        return deliveryMode == javax.jms.DeliveryMode.NON_PERSISTENT ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT;
    }

    private DestinationConfiguration.DestinationType convertDestinationType(int type) {
        if (type == PortInstanceAdapter.DESTINATION_TYPE_TOPIC) {
            return DestinationConfiguration.DestinationType.TOPIC;
        } else {
            return DestinationConfiguration.DestinationType.QUEUE;
        }
    }

    SessionConfiguration convertSessionConfiguration(com.fiorano.microservice.common.port.SessionConfiguration inputPortSessionConfiguration) {
        SessionConfiguration sessionConfiguration = new SessionConfiguration();
        AcknowledgeMode ackMode;
        switch (inputPortSessionConfiguration.getAcknowledgementMode()) {
            case com.fiorano.esb.wrapper.SessionConfiguration.AUTO_ACKNOWLEDGE:
                ackMode = AcknowledgeMode.AUTO_ACKNOWLEDE;
                break;
            case com.fiorano.esb.wrapper.SessionConfiguration.DUPS_OK_ACKNOWLEDGE:
            default:
                ackMode = AcknowledgeMode.DUPS_OKACKNOWLEDE;
                break;
            case com.fiorano.esb.wrapper.SessionConfiguration.CLIENT_ACKNOWLEDGE:
                ackMode = AcknowledgeMode.CLIENT_ACKNOWLEDGE;
                break;
        }
        sessionConfiguration.setAcknowledgementMode(ackMode);
        sessionConfiguration.setTransacted(inputPortSessionConfiguration.isTransacted());
        sessionConfiguration.setTransactionSize(inputPortSessionConfiguration.getTransactionSize());
        return sessionConfiguration;
    }

    public JMSInputTransport createInputTransport(IModule parent, IInputTransportConfiguration transportConfiguration) {
        JMSInputTransportConfiguration inputTransportConfiguration = (JMSInputTransportConfiguration) transportConfiguration;
        return new JMSInputTransport(parent, mqHelper, inputTransportConfiguration);
    }

    public JMSOutputTransport createOutputTransport(IModule parent, IOutputTransportConfiguration transportConfiguration) {
        JMSOutputTransportConfiguration outputTransportConfiguration = (JMSOutputTransportConfiguration) transportConfiguration;
        return new JMSOutputTransport(parent, mqHelper, outputTransportConfiguration);
    }

    public JMSErrorTransport createErrorTransport(IModule parent, IOutputTransportConfiguration transportConfiguration) {
        JMSOutputTransportConfiguration errorTransportConfiguration = (JMSOutputTransportConfiguration) transportConfiguration;
        return new JMSErrorTransport(parent, mqHelper, errorTransportConfiguration);
    }

    public void addService(IService service) {
        services.add(service);
    }

    public void removeService(IService service) {
        services.remove(service);
    }
}
