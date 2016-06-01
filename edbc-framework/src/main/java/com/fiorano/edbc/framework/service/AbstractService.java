/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service;

import com.fiorano.edbc.framework.service.ccp.jms.CCPEventManager;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.configuration.ServiceConfigurationSerializer;
import com.fiorano.edbc.framework.service.engine.AbstractServiceEngine;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.jms.AbstractJMSObjects;
import com.fiorano.edbc.framework.service.jms.IJMSObjects;
import com.fiorano.edbc.framework.service.jms.ServiceExceptionHandler;
import com.fiorano.esb.common.service.ServiceState;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.esb.wrapper.IJNDILookupHelper;
import com.fiorano.esb.wrapper.JNDILookupHelper;
import com.fiorano.microservice.common.ccp.ICCPEventManager;
import com.fiorano.microservice.common.log.LogManager;
import com.fiorano.microservice.common.log.LoggerUtil;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;
import com.fiorano.openesb.microservice.ccp.event.common.data.ManageableProperties;
import com.fiorano.openesb.application.application.PortInstance;
import com.fiorano.openesb.microservice.ccp.event.common.data.MicroserviceConfiguration;
import com.fiorano.openesb.microservice.ccp.event.common.data.PortConfiguration;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.configuration.NamedConfigConstants;
import com.fiorano.services.common.configuration.NamedConfiguration;
import com.fiorano.microservice.common.service.ManageablePropertyUtil;
import com.fiorano.services.common.jaxb.JAXBUtil;
import com.fiorano.services.common.security.MessageEncryptionConfiguration;
import com.fiorano.services.common.service.CollectingErrorListener;
import com.fiorano.services.common.service.ServiceLifeCycle;
import com.fiorano.services.common.transaction.ResourceManager;
import com.fiorano.services.common.transaction.TransactionException;
import com.fiorano.services.common.transaction.api.IResourceFactory;
import com.fiorano.services.common.transaction.api.IResourceManager;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.common.xmlsecurity.encryption.XMLEncrypter;
import com.fiorano.util.JavaUtil;
import com.fiorano.util.crypto.CommonConstants;
import com.fiorano.util.crypto.StringEncrypter;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.utils.BeanUtils;

import javax.naming.NamingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fiorano.esb.common.service.ServiceState.State.*;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractService implements ServiceLifeCycle {
    private final ServiceState serviceState = new ServiceState();
    protected CommandLineParameters commandLineParams = null;
    protected Logger logger;
    protected IJNDILookupHelper lookupHelper = null;
    protected IServiceConfiguration configuration = null;
    protected AbstractServiceEngine engine;
    protected IJMSObjects jmsObjects;
    @Deprecated
    protected volatile boolean started = false;
    protected LogManager logManager;
    private static String serviceLookupName;
    private ICCPEventManager ccpEventManager;
    private IResourceManager resourceManager;
    private Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration = new HashMap<>();
    private Map<String, List<PortInstance>> portInstances = new HashMap<>();
    private HashMap<String, String> namedConfigurations = new HashMap<>();

    /**
     * Starts the service:
     * processes the command line arguments,
     * creates the logger,
     * calls lookups (while the lookup helper is active),
     * calls initialize,
     *
     * @param args command line arguments passed from main.
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException
     */
    protected void start(String[] args) throws ServiceExecutionException {
        System.setProperty("COMPONENT_CPS", "true");
        commandLineParams = parseArguments(args);
        System.setProperty("ProductName", "OpenESB");
        try {
            initializeLoggers();
        } catch (Exception e) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_WHILE_CREATING_LOGGERS), e);
        }
        createLookupHelper(commandLineParams);
        createJMSObjects();
        jmsObjects.start();
        try {
            if (!commandLineParams.isInmemoryLaunchable()) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PROCESS_ID, new Object[]{JavaUtil.getPID()}));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOGGING_PROCESS_ID, new Object[]{e.getMessage()}));
        }

        for (int i = 0; i < args.length; i++) {
            if (ESBConstants.PASSWORD.equals(args[i])) {
                i++;
            } else {
                logger.info(RBUtil.getMessage(Bundle.class, Bundle.RT_ARGS, new Object[]{i, args[i]}));
            }
        }
        try {
            createCCPObjects();
            ccpEventManager.start();
            getServiceState().setState(STARTING);
            lookups();
            ((AbstractJMSObjects)jmsObjects).createDestinations();
            initialize();
            started = true;
            getServiceState().setState(STARTED);
        } catch (ServiceExecutionException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SERVICE_STARTUP_FAILED, new String[]{e.getMessage()}), e);
            stop();
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SERVICE_STARTUP_FAILED, new String[]{e.getMessage()}), e);
            stop();
            throw new ServiceExecutionException(e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        } finally {
            System.getProperties().remove("COMPONENT_CPS");
            closeLookupHelper();
        }
    }

    protected void initializeLoggers() throws Exception {
        logManager = LoggerUtil.createLogHandlers(commandLineParams);
        createLogger(commandLineParams);
    }

    /**
     * Performs lookup of configuration, input and output ports, create all necessary transport and
     * business objects used by this service.
     *
     * @throws ServiceExecutionException
     */
    protected void lookups() throws ServiceExecutionException {
        fetchConfiguration();
        //set encryption key
        try {
            String key = lookupKeyStoreNamedConfig();
            if (key != null) {
                StringEncrypter.setEncryptionKey(key);
            }
            XMLEncrypter.encryptionConfiguration = lookupMessageEncryptionKeyStore();
        } finally {
            //reset
            StringEncrypter.resetEncryptionKey();
        }
        getServiceState().setTransportLayerState(STARTING);
        if (configuration.getTransactionConfiguration().isEnabled()) {
            if (!commandLineParams.isCCPEnabled()) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.CCP_FOR_TRANSACTION), ServiceErrorID.SERVICE_LAUNCH_ERROR);
            }
            resourceManager = new ResourceManager(configuration.getTransactionConfiguration(), commandLineParams.getApplicationName()
                    + "__" + commandLineParams.getServiceInstanceName(), getLogger());
        }


//        createJMSObjects();

        getServiceState().setTransportLayerState(STARTED);
        getServiceState().setBusinessLayerState(STARTING);
        createServiceObjects();
        getServiceState().setBusinessLayerState(STARTED);
    }

    private String lookupKeyStoreNamedConfig() {
        try {
            String connectionFactory = getCommandLineParams().getConnectionFactory();
            return ((String) namedConfigurations.get(connectionFactory + "__" + NamedConfigConstants.RESOURCE_CONFIG_TYPE + "__" + CommonConstants.KEY_STORE_CONFIG));
        } catch (Exception e) {
            return null;
        }
    }

    private MessageEncryptionConfiguration lookupMessageEncryptionKeyStore() {
        try {
            String connectionFactory = getCommandLineParams().getConnectionFactory();
            String config = ((String) namedConfigurations.get(connectionFactory + "__" + NamedConfigConstants.RESOURCE_CONFIG_TYPE
                    + "__" + CommonConstants.MSG_ENCRYPTION_CONFIG));
            MessageEncryptionConfiguration mec = new MessageEncryptionConfiguration();
            Object unmarshal = JAXBUtil.unmarshal(config);
            JAXBUtil.copyProperties(unmarshal, mec);
            return mec;
        } catch (Exception e) {
            if (logger != null) {
                logger.log(Level.FINE, e.getMessage());
            }
            return null;
        }
    }

    /**
     * All start up actions for business layer are performed here.
     *
     * @throws ServiceExecutionException
     */
    protected void initialize() throws ServiceExecutionException {
        if (engine != null) {
            engine.start();
        }
    }

    /**
     * Call stop to stop the service. It calls finish and then clears the reference to the logger.
     */
    public void stop() {
        if (!STOPPING.equals(getServiceState().getState())) {
            try {
                getServiceState().setState(STOPPING);
                finish();
                if (logger != null)
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.COMPONENT_STOPPED));
                logger = null;
            } finally {
                BeanUtils.removeClassDelegate(ServiceErrorID.class);
                java.beans.Introspector.flushCaches();
                started = true;
                getServiceState().setState(STOPPED);
                exit(-2);
            }
        }
    }

    /**
     * Stops the service process with the provided exit value, by calling <code>System.exit(exitValue)</code>.
     */
    protected void exit(int exitValue) {
        System.exit(exitValue);
    }


    /**
     * Cleanup of the service.
     * <ul>
     * <li>stops listening for new requests</li>
     * <li>processes current request</li>
     * <li>closes lookup resource</li>
     * <li>invoke stop call on business layer</li>
     * <li>stops CCP and transport layer</li>
     * <li>clears configuration</li>
     * </ul>
     */
    protected void finish() {
        new Thread(new Runnable() {
            public void run() {
                if (jmsObjects != null) {
                    jmsObjects.stopProcessing();
                }
                if (lookupHelper != null && !lookupHelper.isContextClosed()) {
                    try {
                        lookupHelper.close();
                    } catch (NamingException e) {
                        logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    }
                }
                getServiceState().setBusinessLayerState(STOPPING);
                if (engine != null) {
                    try {
                        engine.stop();
                    } catch (ServiceExecutionException e) {
                        getServiceState().setState(UNDEFINED);
                        getServiceState().setBusinessLayerState(UNDEFINED);
                        logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    } finally {
                        engine = null;
                    }
                }
                if (resourceManager != null) {
                    try {
                        resourceManager.stop();
                    } catch (TransactionException e) {
                        getServiceState().setState(UNDEFINED);
                        getServiceState().setBusinessLayerState(UNDEFINED);
                        logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    } finally {
                        resourceManager = null;
                    }
                }
                getServiceState().setBusinessLayerState(STOPPED);
                if (ccpEventManager != null) {
                    try {
                        ccpEventManager.stop();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    } finally {
                        ccpEventManager = null;
                    }
                }
                if (jmsObjects != null) {
                    try {
                        getServiceState().setTransportLayerState(STOPPING);
                        getServiceState().setTransportLayerState(STOPPED);
                        getServiceState().setState(STOPPED);
                        jmsObjects.destroy();
                    } catch (ServiceExecutionException e) {
                        getServiceState().setState(UNDEFINED);
                        getServiceState().setTransportLayerState(UNDEFINED);
                        logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    } finally {
                        jmsObjects = null;
                    }
                }
                configuration = null;
                if(isInMemory()){
                    logManager.destroyHandlers(commandLineParams.getApplicationName() + "__" + String.valueOf(commandLineParams.getApplicationVersion()).replace(".","_") + "." + commandLineParams.getServiceGUID()+ "." + commandLineParams.getServiceInstanceName());
                }
            }
        }).start();
    }

    /**
     * Indicates the state of the service.
     *
     * @return true if start has been called but not stop
     */
    public synchronized boolean isStarted() {
        return started;
    }

    protected synchronized void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Gets the command line parameters object after start has been called.
     *
     * @return the command line parameters object or null
     */
    public CommandLineParameters getCommandLineParams() {
        return commandLineParams;
    }

    /**
     * Returns the current state of the service.
     *
     * @return ServiceState - state of the service
     * @see ServiceState
     */
    public ServiceState getServiceState() {
        return serviceState;
    }

    /**
     * Called by start to process the command line arguments.
     *
     * @param args command line parameters from main
     * @return the command line parameters object
     * @throws ServiceExecutionException
     */
    protected CommandLineParameters parseArguments(String[] args) throws ServiceExecutionException {
        return new CommandLineParameters(args);
    }

    /**
     * Gets the logger object, or null if the service has not been started or has stopped.
     *
     * @return the logger object or null
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Called by start to create the logger object.
     *
     * @param commandLineParams the processed command line arguments
     */
    protected void createLogger(CommandLineParameters commandLineParams) {
        logger = LoggerUtil.getServiceLogger(this.getClass().getName().toUpperCase(), commandLineParams);
    }

    /**
     * Called by start to create a lookup helper.
     *
     * @param commandLineParams the processed command line arguments
     */
    protected void createLookupHelper(CommandLineParameters commandLineParams) {
        if(commandLineParams.getParameter("-icf") == null){
            commandLineParams.setInitialContextFcatory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        }else{
            commandLineParams.setInitialContextFcatory((String) commandLineParams.getParameter("-icf"));
        }
        lookupHelper = new JNDILookupHelper(commandLineParams);
    }

    /**
     * Gets the JNDI lookup helper or null if the service has not been started or has been stopped.
     *
     * @return the JNDI lookup helper object or null
     */
    public IJNDILookupHelper getLookupHelper() {
        return lookupHelper;
    }

    /**
     * Called by start to close the JNDI lookup helper after calling lookups.
     */
    private void closeLookupHelper() {
        if (lookupHelper != null && !lookupHelper.isContextClosed()) {
            try {
                lookupHelper.close();
            } catch (NamingException e) {
                String explanation = e.getExplanation();
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CONTEXT_CLOSE_FAILED, new Object[]{explanation == null ? "" : explanation}), e);
            }
        }
    }

    public void updateConfiguration(Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration) {
        this.ccpConfiguration.putAll(ccpConfiguration);
    }

    protected void fetchConfiguration() throws ServiceExecutionException {

        DataRequestEvent dataRequestEvent = new DataRequestEvent();

        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.PORT_CONFIGURATION);
        dataRequestEvent.setReplyNeeded(true);
        ccpEventManager.getCCPEventGenerator().sendEvent(dataRequestEvent);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        dataRequestEvent = new DataRequestEvent();
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);
        dataRequestEvent.setReplyNeeded(true);
        ccpEventManager.getCCPEventGenerator().sendEvent(dataRequestEvent);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        dataRequestEvent = new DataRequestEvent();
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.NAMED_CONFIGURATION);
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.MANAGEABLE_PROPERTIES);
        dataRequestEvent.setReplyNeeded(true);
        ccpEventManager.getCCPEventGenerator().sendEvent(dataRequestEvent);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MicroserviceConfiguration data = (MicroserviceConfiguration) ccpConfiguration
                .get(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);


        try {
            configuration = ServiceConfigurationSerializer.deserialize(data.getConfiguration());
        } catch (Exception e) {
            if (isConfigurationMandatory()) {
                throw new ServiceExecutionException(e.getMessage(), e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            } else {
                logger.warning(e.getMessage() + RBUtil.getMessage(Bundle.class, Bundle.CREATING_NEW_CONFIGURATION));
                createDefaultServiceConfiguration();
            }
        }

        ManageableProperties manageablePropertiesData = (ManageableProperties) ccpConfiguration.get(DataRequestEvent.DataIdentifier.MANAGEABLE_PROPERTIES);
        if(manageablePropertiesData != null && manageablePropertiesData.getManageableProperties() != null){
            ManageablePropertyUtil.setManageableProperties(configuration, manageablePropertiesData.getManageableProperties());
        }

        com.fiorano.openesb.microservice.ccp.event.common.data.NamedConfiguration namedConfigData
                = (com.fiorano.openesb.microservice.ccp.event.common.data.NamedConfiguration)
                ccpConfiguration.get(DataRequestEvent.DataIdentifier.NAMED_CONFIGURATION);
        namedConfigurations = namedConfigData.getNamedConfigurations();


        try {
            updateNamedConfigurations();
        } catch (Exception e) {
            throw new ServiceExecutionException(
                    RBUtil.getMessage(Bundle.class, Bundle.NAMED_CONFIG_LOOKUP_FAILED, new Object[]{e.getMessage()}),
                    e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }

        PortConfiguration portConfig = (PortConfiguration) ccpConfiguration.get(DataRequestEvent.DataIdentifier.PORT_CONFIGURATION);
        portInstances = portConfig.getPortInstances();

        String serviceInstId = commandLineParams.getApplicationName() + "__" + String.valueOf(commandLineParams.getApplicationVersion()).replace(".","_") + "__" + commandLineParams.getServiceInstanceName();
        configuration.applyPasswordEncLogger(serviceInstId, commandLineParams.getServiceGUID());
        configuration.decryptPasswords();
        try {
            CollectingErrorListener errorListener = new CollectingErrorListener();
            configuration.validate(errorListener);
            Exceptions exceptions = errorListener.getCollectedExceptions();
            if (!exceptions.isEmpty()) {
                throw new ServiceExecutionException(exceptions.getMessage(), exceptions, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            } else {
                logger.info(RBUtil.getMessage(Bundle.class, Bundle.CONFIGURATION_VALID));
            }
        } catch (ServiceConfigurationException e) {
            throw new ServiceExecutionException(e.getMessage(), e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
    }

    public Map<String, List<PortInstance>> getPortInstances() {
        return portInstances;
    }

    protected void updateNamedConfigurations() throws Exception {
    }

    protected void updateNamedConfiguration(NamedConfiguration targetConfig, String configType) throws Exception {

        if (targetConfig != null) {

            String configName = targetConfig.getConfigName();
            if (configName != null) {

                String connectionFactory = getCommandLineParams().getConnectionFactory();

                String xml = (String) namedConfigurations.get(configType + "__" + configName);

                if (xml != null) {
                    NamedConfiguration srcConfig = (NamedConfiguration) JAXBUtil.unmarshal(xml);

                    JAXBUtil.copyProperties(srcConfig, targetConfig);
                }
            }
        }
    }

    protected abstract boolean isConfigurationMandatory();

    protected void createDefaultServiceConfiguration() {
    }

    public IServiceConfiguration getConfiguration() {
        return configuration;
    }

    public IResourceFactory getResourceFactory() {
        return null;
    }

    public AbstractServiceEngine getEngine() {
        return engine;
    }

    protected abstract IJMSObjects _createJMSObjects();

    protected void createJMSObjects() throws ServiceExecutionException {
        jmsObjects = _createJMSObjects();
        jmsObjects.create();
    }

    protected void createServiceObjects() throws ServiceExecutionException {
    }

    protected void createCCPObjects() throws ServiceExecutionException {
        if (!commandLineParams.isCCPEnabled()) {
            return;
        }
        ccpEventManager = new CCPEventManager(this);
    }

    public ServiceExceptionHandler getExceptionHandler() {
        return jmsObjects != null ? jmsObjects.getExceptionHandler() : null;
    }

    public IJMSObjects getJMSObjects() {
        return jmsObjects;
    }

    public IResourceManager getResourceManager() {
        return resourceManager;
    }

    public boolean isInMemory() {
        return commandLineParams.isInmemoryLaunchable();
    }

    public Logger getLogger(String loggerName) {
        return LoggerUtil.getServiceLogger(loggerName.toUpperCase(), commandLineParams.getApplicationName(), String.valueOf(commandLineParams.getApplicationVersion()) , commandLineParams.getServiceGUID(), commandLineParams.getServiceInstanceName());
    }

    public String getServiceLookupName() {

        if (serviceLookupName != null) {
            return serviceLookupName;
        }

        return serviceLookupName = commandLineParams.getApplicationName() + "__"
                + String.valueOf(commandLineParams.getApplicationVersion()).replace(".", "_") + "__"
                + commandLineParams.getServiceInstanceName();
    }

    public void clearOutLogs() {
        logManager.clearOutLogs(commandLineParams.getApplicationName() + "__"
                + String.valueOf(commandLineParams.getApplicationVersion()) + "__"
                + commandLineParams.getServiceInstanceName(), getLogger());
    }

    public void clearErrLogs() {
        logManager.clearErrLogs(commandLineParams.getApplicationName() + "__"
                + String.valueOf(commandLineParams.getApplicationVersion()) + "__"
                + commandLineParams.getServiceInstanceName(), getLogger());
    }
}
