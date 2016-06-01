/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.AbstractService;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.jms.ports.AbstractInputPortHandler;
import com.fiorano.edbc.framework.service.jms.ports.OutputPortHandler;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.esb.wrapper.IJNDILookupHelper;
import com.fiorano.esb.wrapper.ILookupConfiguration;
import com.fiorano.esb.wrapper.InputPortInstanceAdapter;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.microservice.common.port.PortInstanceAdapter;
import com.fiorano.openesb.application.application.PortInstance;
import com.fiorano.services.common.service.ServerAvailabilityListener;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.libraries.jms.configuration.*;
import com.fiorano.services.libraries.jms.helper.DefaultMQHelper;
import com.fiorano.services.libraries.jms.helper.MQHelper;
import com.fiorano.util.ExceptionUtil;
import com.fiorano.util.StringUtil;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.util.EventGenerator;
import fiorano.tifosi.dmi.application.OutputPortInstance;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JMSObjects handles the creation of connection parameters, does the lookup and creates the JMSHandler.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractJMSObjects implements IJMSObjects {

    /**
     * Handles exceptions occured while processing requests
     */
    protected ServiceExceptionHandler exceptionHandler;
    /**
     * Connection object to make an active connection to its JMS provider.
     */
    private Connection connection;
    /**
     * Service class object.
     */
    private AbstractService service;
    /**
     * Used to raise events
     */
    private EventGenerator eventGenerator;
    /**
     * session used to create messages for raising events
     */
    private Session eventSession;
    /**
     * Input port handlers collection.
     */
    private Collection inputPortHandlers = new ArrayList();
    /**
     * Hashtable storing the output ports name against the corresponding destinations.
     */
    private Hashtable outputPortHandlers = new Hashtable();
    /**
     * Logger for logging
     */
    private Logger logger;

    protected MQHelper mqHelper;
    private ConnectionConfiguration connectionConfiguration;
    private String destinationLookupPrefix = "";
    /**
     * Creates an instance of JMSObjects.
     *
     * @param service service class object.
     */
    public AbstractJMSObjects(AbstractService service) {
        this.service = service;
        logger = service.getLogger();
        mqHelper = new DefaultMQHelper(logger);
    }

    public Connection getConnection() {
        return connection;
    }

    protected AbstractService getService() {
        return service;
    }

    protected Collection getInputPortHandlers() {
        return inputPortHandlers;
    }

    protected Hashtable getOutputPortHandlers() {
        return outputPortHandlers;
    }

    protected Logger getLogger() {
        return logger;
    }

    /**
     * This method does the connection factory lookup, creates and starts the connection.
     * It also does the outputports lookup and creates the JMSHandler.
     *
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException if there's any exception in the jmsObjects creation.
     */
    public void create() throws ServiceExecutionException {
        try {
            CommandLineParameters commandLineParams = service.getCommandLineParams();
            destinationLookupPrefix = commandLineParams.getApplicationName() + ESBConstants.JNDI_CONSTANT
                    + String.valueOf(commandLineParams.getApplicationVersion()).replace(".", "_") + ESBConstants.JNDI_CONSTANT
                    + commandLineParams.getServiceInstanceName() + ESBConstants.JNDI_CONSTANT;
            IJNDILookupHelper lookupHelper = service.getLookupHelper();
            JNDIConfiguration jndiConfiguration = new JNDIConfiguration();
            if(commandLineParams.getParameter("-icf") == null){
                jndiConfiguration.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            }else{
                jndiConfiguration.setInitialContextFactory((String) commandLineParams.getParameter("-icf"));
            }
            jndiConfiguration.setUsername(commandLineParams.getUsername());
            jndiConfiguration.setPassword(commandLineParams.getPassword());
            HashMap<String, Object> additionalProperties = new HashMap<>();
            if (!StringUtil.isEmpty(commandLineParams.getSecurityProtocol())) {
                additionalProperties.put(Context.SECURITY_PROTOCOL, commandLineParams.getSecurityProtocol());
            }
//            if (!StringUtil.isEmpty(lookupConfiguration.getSecurityManager())) {
//                additionalProperties.put(FioranoJNDIContext.SSL_SECURITY_MANAGER, lookupConfiguration.getSecurityManager());
//            }
//            if (!StringUtil.isEmpty(lookupConfiguration.getTransportProtocol())) {
//                additionalProperties.put(FioranoJNDIContext.TRANSPORT_PROTOCOL, lookupConfiguration.getTransportProtocol());
//            }
            if (commandLineParams.getAdditionalEnvProperties() != null) {
                additionalProperties.putAll(commandLineParams.getAdditionalEnvProperties());
            }
            jndiConfiguration.setAdditionalProperties(additionalProperties);

            ConnectionFactoryConfiguration cfConfiguration = new ConnectionFactoryConfiguration();
            cfConfiguration.setUrl(commandLineParams.getURL());
            cfConfiguration.setBackupURLs(commandLineParams.getBackupURL());
            cfConfiguration.setLookupCF(true);
            cfConfiguration.setCfLookupName(commandLineParams.getConnectionFactory());
            cfConfiguration.setJndiConfiguration(jndiConfiguration);

            connectionConfiguration = new ConnectionConfiguration();
            connectionConfiguration.setClientID(service.getCommandLineParams().getApplicationName() + "__" + String.valueOf(service.getCommandLineParams().getApplicationVersion()).replace(".","_") + "__" + service.getCommandLineParams().getServiceInstanceName());
            connectionConfiguration.setUsername(commandLineParams.getUsername());
            connectionConfiguration.setPassword(commandLineParams.getPassword());
            connectionConfiguration.setUnifiedDomainSupported(true);
            connectionConfiguration.setCfConfiguration(cfConfiguration);
            connectionConfiguration.setClientIDDefined(false);

            mqHelper.initialize(connectionConfiguration.getCfConfiguration());
            ConnectionFactory connectionFactory = lookupHelper.lookupConnectionFactory();
            connection = mqHelper.createConnection(connectionFactory, connectionConfiguration);

            connection.setClientID(service.getCommandLineParams().getApplicationName() + "__" + String.valueOf(service.getCommandLineParams().getApplicationVersion()).replace(".","_") + "__" + service.getCommandLineParams().getServiceInstanceName());
//            new ServerAvailabilityListener(service, connection, logger);
            eventGenerator = new EventGenerator();

            try {
                eventSession = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
                eventGenerator.setProducer(eventSession.createProducer(null));
            } catch (JMSException e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_OBJECT_CREATION_FAILED,
                        new Object[]{ExceptionUtil.getStackTrace(e)}));
                throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            }

//            _createAdditionalObjects();
        } catch (NamingException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.NAMING_EXCEPTION,
                    new Object[]{ExceptionUtil.getStackTrace(e)}));
            throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);

        } catch (JMSException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_OBJECT_CREATION_FAILED,
                    new Object[]{ExceptionUtil.getStackTrace(e)}));
            throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_OBJECT_CREATION_FAILED,
                    new Object[]{ExceptionUtil.getStackTrace(e)}));
            throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
    }

    public void createDestinations() throws NamingException, ServiceExecutionException {

        try {

            Session session = mqHelper.createSession(connection, new SessionConfiguration());
            Map<String, List<PortInstance>> ports = ((AbstractService)service).getPortInstances();
            List<PortInstance> outPorts = ports.get("OUT_PORTS");
            for (PortInstance o : outPorts) {
                if(o.isEnabled()){
                    if (!"ON_EXCEPTION".equalsIgnoreCase(o.getName())) {
                        OutputPortInstanceAdapter outputPortInstanceAdapter = new OutputPortInstanceAdapter();
                        outputPortInstanceAdapter.setName(o.getName());
                        outputPortInstanceAdapter.setEnabled(true);
                        outputPortInstanceAdapter.setDestinationType((o.getDestinationType() == PortInstanceAdapter.DESTINATION_TYPE_TOPIC) ? InputPortInstanceAdapter.DESTINATION_TYPE_TOPIC : InputPortInstanceAdapter.DESTINATION_TYPE_QUEUE);
                        DestinationConfiguration destinationConfiguration = new DestinationConfiguration();
                        if(o.isSpecifiedDestinationUsed() && o.getDestination() != null){
                            destinationConfiguration.setName(o.getDestination());
                        }else{
                            destinationConfiguration.setName(destinationLookupPrefix + o.getName());
                        }
                        destinationConfiguration.setLookupSupported(false);
                        destinationConfiguration.setAutoCreateSupported(true);
                        destinationConfiguration.setType(convertDestinationType(o.getDestinationType()));
                        Destination sendDest = mqHelper.fetchDestination(session, destinationConfiguration);
                        OutputPortHandler outputPortHandler = createOutputPortHandler(sendDest, outputPortInstanceAdapter);
                        outputPortHandlers.put(o.getName(), outputPortHandler);
                    }
                }

            }
               DestinationConfiguration destinationConfiguration = new DestinationConfiguration();
                destinationConfiguration.setName(destinationLookupPrefix + "ON_EXCEPTION");
                destinationConfiguration.setLookupSupported(false);
                destinationConfiguration.setAutoCreateSupported(true);
                destinationConfiguration.setType(DestinationConfiguration.DestinationType.TOPIC);
                Destination exceptionDestination = mqHelper.fetchDestination(session, destinationConfiguration);
                eventGenerator.setErrorDestination(exceptionDestination);

            createExceptionHandler();
            connection.setExceptionListener(exceptionHandler);

            List<PortInstance> inputPorts = ports.get("IN_PORTS");
            for (PortInstance inputPort : inputPorts) {
                InputPortInstanceAdapter inputPortInstanceAdapter = new InputPortInstanceAdapter();
                if (inputPort.isEnabled()) {
                    inputPortInstanceAdapter.setEnabled(true);
                    inputPortInstanceAdapter.setName(inputPort.getName());
                    inputPortInstanceAdapter.setDestinationType((inputPort.getDestinationType() == PortInstanceAdapter.DESTINATION_TYPE_TOPIC) ? InputPortInstanceAdapter.DESTINATION_TYPE_TOPIC : InputPortInstanceAdapter.DESTINATION_TYPE_QUEUE);

                    DestinationConfiguration destinationConfiguration1 = new DestinationConfiguration();
                    if(inputPort.isSpecifiedDestinationUsed() && inputPort.getDestination() != null){
                        destinationConfiguration1.setName(inputPort.getDestination());
                    }else{
                        destinationConfiguration1.setName(destinationLookupPrefix + inputPort.getName());
                    }
                    destinationConfiguration1.setLookupSupported(false);
                    destinationConfiguration1.setAutoCreateSupported(true);
                    destinationConfiguration1.setType(convertDestinationType(inputPort.getDestinationType()));
                    Destination destination = mqHelper.fetchDestination(session, destinationConfiguration1);
                    AbstractInputPortHandler inputPortHandler = createInputPortHandler(destination, inputPortInstanceAdapter, outputPortHandlers.values(), eventGenerator, eventSession);
                    if(inputPortHandler == null){
                        continue;
                    }
                    inputPortHandler.setResourceManager(getService().getResourceManager());
                    inputPortHandler.setLogger(logger);
                    CommandLineParameters commandLineParameters = service.getCommandLineParams();
                    ServiceDetails serviceDetails = new ServiceDetails(commandLineParameters.getNodeName(), commandLineParameters.getApplicationName(),
                            commandLineParameters.getServiceGUID(), commandLineParameters.getServiceInstanceName(), String.valueOf(commandLineParameters.getApplicationVersion()));
                    inputPortHandler.createJMSHandlers(connection, service.getConfiguration(), service.getExceptionHandler(), serviceDetails);
                    inputPortHandlers.add(inputPortHandler);
                }
            }

        }catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_OBJECT_CREATION_FAILED,
                    new Object[]{ExceptionUtil.getStackTrace(e)}));
            throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
        _createAdditionalObjects();
    }


    public void createExceptionHandler() {
        exceptionHandler = new ServiceExceptionHandler(service, eventGenerator, eventSession);
    }

    public void start() throws ServiceExecutionException {
        try {
            connection.start();
        } catch (JMSException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_START_FAILED,
                    new Object[]{ExceptionUtil.getStackTrace(e)}));
            throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
    }

    protected void _createAdditionalObjects() {
    }

    protected abstract AbstractInputPortHandler createInputPortHandler(Destination destination, InputPortInstanceAdapter inputPortInstanceAdapter,
                                                                       Collection outputPortHandlers, EventGenerator eventGenerator, Session eventSession);

    protected OutputPortHandler createOutputPortHandler(Destination sendDest, OutputPortInstanceAdapter outputPortInstanceAdapter) {
        return new OutputPortHandler(sendDest, outputPortInstanceAdapter);
    }

    public void stop() throws ServiceExecutionException {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.JMS_STOP_FAILED,
                    new Object[]{e.getMessage()}), e);
            throw new ServiceExecutionException(e, ServiceErrorID.TRANSPORT_ERROR);
        }
    }

    /**
     * Closes the connection and destroys the jmsObjects.
     */
    public void destroy() {
        try {
            stop();
        } catch (ServiceExecutionException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_IN_CLOSE_CONNECTION,
                    new Object[]{e.getMessage(), ExceptionUtil.getStackTrace(e)}));
        }
        connection = null;
        outputPortHandlers.clear();
        eventGenerator = null;
        exceptionHandler = null;
        inputPortHandlers.clear();
    }

    /**
     * Gets the event generator.
     *
     * @return eventGenerator
     */
    public EventGenerator getEventGenerator() {
        return eventGenerator;
    }

    public Session getEventSession() {
        return eventSession;
    }

    public ServiceExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void stopProcessing() {
        for (Object inputPortHandlerObj : inputPortHandlers) {
            AbstractInputPortHandler inputPortHandler = (AbstractInputPortHandler) inputPortHandlerObj;
            AbstractJMSHandler[] jmsHandlers = inputPortHandler.getJmsHandlers();
            if (jmsHandlers != null) {
                for (AbstractJMSHandler jmsHandler : jmsHandlers) {
                    try {
                        jmsHandler.messageConsumer.close();
                        jmsHandler.messageConsumer.setMessageListener(null);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, RBUtil.getMessage(Bundle.class,
                                Bundle.ERROR_CLOSING_CONSUMER, new String[]{e.getMessage()})));
                    }
                }
            }
        }
    }

    private DestinationConfiguration.DestinationType convertDestinationType(int type) {
        if (type == PortInstanceAdapter.DESTINATION_TYPE_TOPIC) {
            return DestinationConfiguration.DestinationType.TOPIC;
        } else {
            return DestinationConfiguration.DestinationType.QUEUE;
        }
    }
}