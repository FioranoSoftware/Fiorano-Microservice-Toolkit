/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.configuration.BeanXMLConfigurationSerializer;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.peer.PeerCommunicationsManager;
import com.fiorano.edbc.framework.service.internal.transport.ITransportManager;
import com.fiorano.edbc.framework.service.internal.transport.ITransportProvider;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.microservice.common.log.LogManager;
import com.fiorano.microservice.common.log.LoggerUtil;
import com.fiorano.microservice.common.service.ManageablePropertyUtil;
import com.fiorano.openesb.application.application.PortInstance;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.*;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.configuration.NamedConfigConstants;
import com.fiorano.services.common.configuration.NamedConfiguration;
import com.fiorano.services.common.jaxb.JAXBUtil;
import com.fiorano.services.common.security.MessageEncryptionConfiguration;
import com.fiorano.services.common.service.CollectingErrorListener;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.common.xmlsecurity.encryption.XMLEncrypter;
import com.fiorano.util.StringUtil;
import com.fiorano.util.crypto.CommonConstants;
import com.fiorano.util.crypto.StringEncrypter;
import fiorano.esb.utils.BeanUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 10, 2010
 * Time: 10:37:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Service extends Module implements IService {

    //-----------------------------------[Container API]-------------------------------
    protected ITransportProvider transportProvider;
    protected CommandLineParameters launchConfiguration;
    protected PeerCommunicationsManager peerCommunicationsManager;
    //-----------------------------------[Service related API]-------------------------------
    protected IServiceConfiguration configuration;
    protected IEngine engine;
    protected IServiceLauncher launcher;
    protected ITransportManager transportManager;

    private Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration = new HashMap<>();
    protected Map<String, List<PortInstance>> portInstances;
    protected HashMap<String, String> namedConfigurations = new HashMap<>();

    protected LogManager logManager;
    private static String serviceLookupName;

    public Service(IServiceLauncher launcher) {
        super(null);
        System.setProperty("ProductName", "OpenESB");
        this.launcher = launcher;
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    public void create() throws ServiceExecutionException {
        if (launchConfiguration == null) {
            return;
        }
        super.create();
    }

    protected void internalCreate() throws ServiceExecutionException {
        transportProvider.addService(this);
        createEngine();
        createTransportManager();
        transportManager.setPortInstances(portInstances);
        super.internalCreate();
    }

    protected void internalDestroy() throws ServiceExecutionException {
        try {
            super.internalDestroy();
        } finally {
            transportProvider.removeService(this);
            engine = null;
            transportManager = null;
            BeanUtils.removeClassDelegate(ServiceErrorID.class);
            java.beans.Introspector.flushCaches();
        }
    }

    public String getName() {
        return launchConfiguration.getConnectionFactory();
    }

    @Override
    public void initialize(CommandLineParameters launchConfiguration, ITransportProvider transportProvider,
                           PeerCommunicationsManager peerCommunicationsManager)
            throws ServiceExecutionException {

        if (launchConfiguration == null) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.LAUNCH_CONFIG_NOT_PROVIDED), ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
        if (transportProvider == null) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.TRANSPORT_MANAGER_NOT_PROVIDED), ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
        if (peerCommunicationsManager == null) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.PEER_COMMUNICATIONS_MANAGER_NOT_PROVIDED), ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }
        if (this.launchConfiguration != null) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.SERVICE_ALREADY_CONFIGURED), ServiceErrorID.SERVICE_LAUNCH_ERROR);
            return;
        }
        this.launchConfiguration = launchConfiguration;
        this.transportProvider = transportProvider;
        this.peerCommunicationsManager = peerCommunicationsManager;

        try {
            initializeLoggers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configure() throws ServiceExecutionException {
        fetchConfiguration();
    }


    public CommandLineParameters getLaunchConfiguration() {
        return launchConfiguration;
    }

    public ITransportProvider getTransportProvider() {
        return transportProvider;
    }

    public IServiceConfiguration getConfiguration() {
        return configuration;
    }

    public IEngine getEngine() {
        return engine;
    }

    public IServiceLauncher getLauncher() {
        return launcher;
    }

    public ITransportManager getTransportManager() {
        return transportManager;
    }

    public Logger getLogger(String loggerName) {
        return LoggerUtil.getServiceLogger(loggerName.toUpperCase(), launchConfiguration.getApplicationName(), String.valueOf(launchConfiguration.getApplicationVersion()), launchConfiguration.getServiceGUID(), launchConfiguration.getServiceInstanceName());
    }

    //-----------------------------------[Other API]-------------------------------
    protected void initializeLoggers() throws Exception {
        logManager = LoggerUtil.createLogHandlers(launchConfiguration);
        this.logger = LoggerUtil.getServiceLogger(getClass().getName().toUpperCase(), launchConfiguration.getApplicationName(), String.valueOf(launchConfiguration.getApplicationVersion()) , launchConfiguration.getServiceGUID(), launchConfiguration.getServiceInstanceName());
    }

    public void destroyLogHandlers(){
        if(logManager != null && launchConfiguration.isInmemoryLaunchable()){
            logManager.destroyHandlers(launchConfiguration.getApplicationName() + "__" + String.valueOf(launchConfiguration.getApplicationVersion()).replace(".","_") + "." + launchConfiguration.getServiceGUID()+ "." + launchConfiguration.getServiceInstanceName());
        }
    }

    protected void fetchConfiguration() throws ServiceExecutionException {

        DataRequestEvent dataRequestEvent = new DataRequestEvent();

        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);
        dataRequestEvent.setReplyNeeded(true);
        peerCommunicationsManager.getCcpEventManager().getCCPEventGenerator().sendEvent(dataRequestEvent);

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
            if (StringUtil.isEmpty(data.getConfiguration())) {
                createDefaultServiceConfiguration();
            } else {
                configuration = getConfigurationSerializer().deserializeFromString(data.getConfiguration());
            }
        } catch (ServiceConfigurationException e) {
            if (isConfigurationMandatory()) {
                throw new ServiceExecutionException(e.getMessage(), e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            } else {
                logger.warning(e.getMessage() + RBUtil.getMessage(Bundle.class, Bundle.CREATING_NEW_CONFIGURATION));
                createDefaultServiceConfiguration();
            }
        }

        dataRequestEvent = new DataRequestEvent();
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.PORT_CONFIGURATION);
        dataRequestEvent.setReplyNeeded(true);
        peerCommunicationsManager.getCcpEventManager().getCCPEventGenerator().sendEvent(dataRequestEvent);

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        PortConfiguration portConfig = (PortConfiguration) ccpConfiguration.get(DataRequestEvent.DataIdentifier.PORT_CONFIGURATION);
        portInstances = portConfig.getPortInstances();

        dataRequestEvent = new DataRequestEvent();
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.NAMED_CONFIGURATION);
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.MANAGEABLE_PROPERTIES);
        dataRequestEvent.setReplyNeeded(true);
        peerCommunicationsManager.getCcpEventManager().getCCPEventGenerator().sendEvent(dataRequestEvent);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        com.fiorano.openesb.microservice.ccp.event.common.data.NamedConfiguration namedConfigData
                = (com.fiorano.openesb.microservice.ccp.event.common.data.NamedConfiguration)
                ccpConfiguration.get(DataRequestEvent.DataIdentifier.NAMED_CONFIGURATION);

        if(namedConfigData != null){
            namedConfigurations = namedConfigData.getNamedConfigurations();
        }

        String encryptionKey = null;
        try {
            if (namedConfigurations != null && namedConfigurations.size() != 0) {
                encryptionKey = namedConfigurations.get(CommonConstants.KEY_STORE_RESOURCE_TYPE + "__" + CommonConstants.KEY_STORE_CONFIG);
                if (encryptionKey != null) {
                    StringEncrypter.setEncryptionKey(encryptionKey);
                }
            }

            ManageableProperties manageableProperties = (ManageableProperties) ccpConfiguration.get(DataRequestEvent.DataIdentifier.MANAGEABLE_PROPERTIES);

            if(manageableProperties != null){
                Map<String, ConfigurationProperty> properties = manageableProperties.getManageableProperties();
                ManageablePropertyUtil.setManageableProperties(configuration, properties);
            }

            try {
                if (namedConfigurations != null && namedConfigurations.size() != 0){
                    updateNamedConfigurations();
                }
            } catch (Exception e) {
                throw new ServiceExecutionException(
                        RBUtil.getMessage(Bundle.class, Bundle.NAMED_CONFIG_LOOKUP_FAILED, new Object[]{e.getMessage()}),
                        e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            }

            try {
                if (namedConfigurations != null && namedConfigurations.size() != 0) {
                    MessageEncryptionConfiguration mec = new MessageEncryptionConfiguration();
                    mec.setConfigName(CommonConstants.MSG_ENCRYPTION_CONFIG);
                    updateNamedConfiguration(mec, NamedConfigConstants.RESOURCE_CONFIG_TYPE);
                    XMLEncrypter.encryptionConfiguration = mec;
                }
            } catch (Exception e) {
                if (getLogger() != null) {
                    getLogger().log(Level.FINE, e.getMessage());
                }
            }


            configuration.applyPasswordEncLogger(launchConfiguration.getConnectionFactory(), launchConfiguration.getServiceGUID());
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
        } finally {
            //reset
            if (encryptionKey != null) {
                StringEncrypter.resetEncryptionKey();
            }
        }
    }

    @Override
    public void updateConfiguration(Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration) {
        this.ccpConfiguration.putAll(ccpConfiguration);
    }

    protected void updateNamedConfigurations() throws Exception {
    }

    protected void updateNamedConfiguration(NamedConfiguration targetConfig, String configType) throws Exception {

        if (targetConfig != null) {
            String configName = targetConfig.getConfigName();
            if (configName != null) {
                String xml = namedConfigurations.get(configType + "__" + configName);
                NamedConfiguration srcConfig = (NamedConfiguration) JAXBUtil.unmarshal(xml);
                JAXBUtil.copyProperties(srcConfig, targetConfig);
            }
        }
    }

    protected IConfigurationSerializer getConfigurationSerializer() {
        return new BeanXMLConfigurationSerializer();
    }

    protected boolean isConfigurationMandatory() {
        return true;
    }

    protected void createDefaultServiceConfiguration() {
    }

    protected void createEngine() throws ServiceExecutionException {
    }

    protected void createTransportManager() throws ServiceExecutionException {
    }

    @Override
    public String getServiceLookupName() {

        if (serviceLookupName != null) {
            return serviceLookupName;
        }

        return serviceLookupName = launchConfiguration.getApplicationName() + "__"
                + String.valueOf(launchConfiguration.getApplicationVersion()).replace(".", "_") + "__"
                + launchConfiguration.getServiceInstanceName();
    }

    @Override
    public void clearOutLogs() {
        logManager.clearOutLogs(launchConfiguration.getApplicationName() + "__"
                + String.valueOf(launchConfiguration.getApplicationVersion()) + "__"
                + launchConfiguration.getServiceInstanceName(), getLogger());
    }

    @Override
    public void clearErrLogs() {
        logManager.clearErrLogs(launchConfiguration.getApplicationName() + "__"
                + String.valueOf(launchConfiguration.getApplicationVersion()) + "__"
                + launchConfiguration.getServiceInstanceName(), getLogger());
    }

    protected String[] getLoggerNames() {
        return new String[]{};
    }

    public MonitorFactory getMonitorFactory() {
        return null;
    }
}
