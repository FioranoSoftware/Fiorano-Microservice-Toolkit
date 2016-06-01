/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.bc.bcgateway;

import com.fiorano.bc.bcgateway.ccp.CCPEventManager;
//import com.fiorano.bc.trgateway.ExCommandLineParams;
import com.fiorano.bc.trgateway.TrGateway;
import com.fiorano.bc.trgateway.model.Configuration;
import com.fiorano.bc.trgateway.model.dmi.bcdk.BCDKConfigurationInfo;
import com.fiorano.bc.trgateway.model.dmi.tr.JMSTransportProperties;
import com.fiorano.bc.trgateway.model.dmi.tr.TrConfigurationInfo;
import com.fiorano.bc.trgateway.model.dmi.tr.TransportProperties;
import com.fiorano.esb.common.service.ServiceState;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.microservice.common.ccp.ICCPEventManager;
import com.fiorano.microservice.common.log.LogManager;
import com.fiorano.microservice.common.log.LoggerUtil;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;
import com.fiorano.openesb.microservice.ccp.event.common.data.ManageableProperties;
import com.fiorano.openesb.microservice.ccp.event.common.data.MicroserviceConfiguration;
import com.fiorano.openesb.microservice.ccp.event.common.data.PortConfiguration;
import com.fiorano.services.common.configuration.NamedConfigConstants;
import com.fiorano.services.common.jaxb.JAXBUtil;
import com.fiorano.services.common.security.MessageEncryptionConfiguration;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.common.xmlsecurity.encryption.XMLEncrypter;
import com.fiorano.util.JavaUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.util.crypto.CommonConstants;
import com.fiorano.util.crypto.StringEncrypter;
import fiorano.esb.adapter.cfg.ConfigurationObject;
import fiorano.esb.adapter.jca.cfg.FESBRAHandler;
import fiorano.esb.adapter.jca.cfg.JCAAdapterConfigurations;
import fiorano.esb.adapter.jmx.JMXUtil;
import fiorano.esb.util.ConfigurationProperty;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.util.InMemoryLaunchable;
import fiorano.jms.common.IMQConstants;
import fiorano.jms.runtime.naming.FioranoJNDIContext;
import fiorano.tifosi.dmi.application.InputPortInstance;
import fiorano.tifosi.dmi.application.OutputPortInstance;
import fiorano.tifosi.dmi.application.PortInstance;
import fiorano.tifosi.dmi.aps.PortInstConstants;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class launches a BC as an EDBC
 *
 * @author FSIPL
 * @version 1.0
 * @created June 2, 2005
 */
public class ESBComponent implements InMemoryLaunchable {

    private static String INITIAL_CONTEXT_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private final ServiceState serviceState = new ServiceState();
    private final Object lock = new Object();
    private final Object shutDownLock = new Object();
    protected volatile boolean started = false;
    private TrGateway trGateway = null;
    private Logger logger;
    private boolean inMemory = false;
    private CommandLineParameters cmdLineArgs = null;
    private ICCPEventManager ccpEventManager;
    private boolean shutDownHandled = false;
    private Configuration configuration;
    private int exitValue = 0;
    private Connection connection;
    private InitialContext initialContext;
    private LogManager logManager;
    private Map<DataRequestEvent.DataIdentifier, Data> data = new HashMap<>();

    /**
     * Create ESBComponent
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final ESBComponent component = new ESBComponent();
        component.startup(args);
    }

    /**
     * Starts the component
     *
     * @param args command line arguments
     */
    public void startup(String[] args) {
        System.setProperty("ProductName", "OpenESB");
        try {
            cmdLineArgs = new CommandLineParameters(args);
        } catch (Exception ex) {
            shutdown(ex);
            return;
        }
        try {
            createInitialContext();
        } catch (NamingException e) {
            e.printStackTrace();
            shutdown(e);
        }
        try {
            createConnection();
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
            shutdown(e);
        }
        try {
            initializeLoggers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger = LoggerUtil.getServiceLogger(TrGateway.class.getPackage().getName().toUpperCase(), cmdLineArgs);

        if (!cmdLineArgs.isInmemoryLaunchable()) {
            try {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PROCESS_ID, new Object[]{JavaUtil.getPID()}));
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOGGING_PROCESS_ID, new Object[]{e.getMessage()}));
            }
        }
        for (int i = 0; i < args.length; i++) {
            if (ESBConstants.PASSWORD.equals(args[i])) {
                i++;
            } else {
                logger.log(Level.INFO, "Arg[ " + i + "] ->" + args[i]);
            }
        }

        try {
            createCCPObjects();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Not able to start CCP Event Manager.", e);
            shutdown(e);
        }
        getServiceState().setState(ServiceState.State.STARTING);

        //load the configurations
        configuration = new Configuration();
        try {
            fetchConfiguration(cmdLineArgs);
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOOKUP), e);
        }

        setInMemory(cmdLineArgs.isInmemoryLaunchable());

        //invoke the Tr Gateway
        trGateway = getTrGateway();

        try {
            trGateway.setId(cmdLineArgs.getApplicationName() + "__" + String.valueOf(cmdLineArgs.getApplicationVersion()).replace(".", "_") + "__" + cmdLineArgs.getServiceInstanceName());
            trGateway.setLogger(logger);
            trGateway.createServiceDetails("FPS", cmdLineArgs.getApplicationName(), cmdLineArgs.getServiceGUID(), cmdLineArgs.getServiceInstanceName(), cmdLineArgs.getApplicationVersion()+"");
            //set the configurations
            trGateway.setConfiguration(configuration);
            trGateway.setNamedConfigutation(namedConfigurations);
            trGateway.setInMemory(cmdLineArgs.isInmemoryLaunchable());
            trGateway.setServiceState(serviceState);
            if (managedProperties != null) {
                trGateway.setManageableProperties(managedProperties);
            }
            trGateway.setEventsTopic(cmdLineArgs.getEventsTopic());

            try {
                //set encryption key
                String key = lookupKeyStoreConfig();
                if (key != null) {
                    StringEncrypter.setEncryptionKey(key);
                }
                XMLEncrypter.encryptionConfiguration = lookupMessageEncryptionKeyStore(cmdLineArgs);

            } finally {
                StringEncrypter.resetEncryptionKey();
            }
            trGateway.startup();
            started = true;
            getServiceState().setState(ServiceState.State.STARTED);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_TR_GATEWAY), ex);
            shutdown(ex);
        } finally {
            System.getProperties().remove("COMPONENT_CPS");
        }

    }

    Map<String, ConfigurationProperty> managedProperties = null;

    protected void initializeLoggers() throws Exception {
        logManager = LoggerUtil.createLogHandlers(cmdLineArgs);
    }

    private void createInitialContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();

        env.put(Context.SECURITY_PRINCIPAL, cmdLineArgs.getUsername());
        env.put(Context.SECURITY_CREDENTIALS, cmdLineArgs.getPassword());
        env.put(Context.PROVIDER_URL, cmdLineArgs.getURL());
        if(cmdLineArgs.getParameter("-icf") == null){
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        }else{
            env.put(Context.INITIAL_CONTEXT_FACTORY, (String) cmdLineArgs.getParameter("-icf"));
        }
        if (cmdLineArgs.getBackupURL() != null)
            env.put(IMQConstants.BACKUP_URLS, cmdLineArgs.getBackupURL());

        if(cmdLineArgs.getSecurityProtocol() != null)
            env.put(FioranoJNDIContext.SSL_SECURITY_MANAGER, cmdLineArgs.getSecurityManager());
        if(cmdLineArgs.getSecurityManager() != null)
            env.put(Context.SECURITY_PROTOCOL, cmdLineArgs.getSecurityProtocol());
        String transportProtocol = cmdLineArgs.getTransportProtocol();
        if (transportProtocol != null)
            env.put(FioranoJNDIContext.TRANSPORT_PROTOCOL, transportProtocol);

        initialContext = new InitialContext(env);
    }

    private void createConnection() throws JMSException, NamingException {
        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup(cmdLineArgs.getConnectionFactory());
        connection = connectionFactory.createConnection(cmdLineArgs.getUsername(), cmdLineArgs.getPassword());
        connection.setClientID(cmdLineArgs.getApplicationName() + "__" + String.valueOf(cmdLineArgs.getApplicationVersion()).replace(".", "_") + "__" + cmdLineArgs.getServiceInstanceName() + "__CCP");
    }

    protected TrGateway getTrGateway() {
        return new TrGateway();
    }

    private HashMap<String, String> namedConfigurations;

    private String lookupKeyStoreConfig() {
        if(namedConfigurations != null)
            return (String) namedConfigurations.get(cmdLineArgs.getConnectionFactory() + "__" + NamedConfigConstants.RESOURCE_CONFIG_TYPE + "__" + CommonConstants.KEY_STORE_CONFIG);
        else
            return  null;
    }

    private MessageEncryptionConfiguration lookupMessageEncryptionKeyStore(CommandLineParameters cmdLineArgs) {
        if(namedConfigurations == null)
            return new MessageEncryptionConfiguration();
        try {
            String config = (String) namedConfigurations.get(
                    cmdLineArgs.getConnectionFactory() + "__" + NamedConfigConstants.RESOURCE_CONFIG_TYPE
                            + "__" + CommonConstants.MSG_ENCRYPTION_CONFIG);

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
     * Stop the Component
     *
     * @param hint any object value which can be used by the method
     */
    public void shutdown(Object hint) {
        // called when the components which are launched in memory are being stopped.
        synchronized (shutDownLock) {
            if (!shutDownHandled) {
                stop();
            }
            shutDownHandled = true;
        }
    }

    public void forceShutdown(Object hint) {
        if (trGateway != null && trGateway.getTransport() != null) {
            trGateway.getTransport().release();
        }
    }

    /**
     * Stop the Component - stop the transport
     */
    public void stop() {
        getServiceState().setState(ServiceState.State.STOPPING);
        if (trGateway != null) {
            trGateway.stop();
        }
        started = false;
        synchronized (lock) {
            lock.notifyAll();
        }
        try {
            if(isInMemory()){
                Thread.sleep(2000);
            }else{
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            //
        }
        getServiceState().setState(ServiceState.State.STOPPED);
        try {
            ccpEventManager.stop();
        } catch (Exception e) {
            //
        }
        if(connection != null){
            try {
                connection.close();
            } catch (JMSException e) {
                //
            }
        }
        if (!isInMemory()) {
            System.exit(-1);
        }else{
            if(logManager != null){
                logManager.destroyHandlers(cmdLineArgs.getApplicationName() + "__" + String.valueOf(cmdLineArgs.getApplicationVersion()).replace(".","_") + "." + cmdLineArgs.getServiceGUID()+ "." + cmdLineArgs.getServiceInstanceName());
            }
        }
    }

    private void setSecurityParameters(JMSTransportProperties trProperties, CommandLineParameters commandLineParams) {
        String securityProtocol = commandLineParams.getSecurityProtocol();

        if (securityProtocol == null) {
            return;
        }

        Hashtable<String, String> props = new Hashtable<>();

        props.put(Context.SECURITY_PROTOCOL, commandLineParams.getSecurityProtocol());
        props.put(FioranoJNDIContext.SSL_SECURITY_MANAGER, commandLineParams.getSecurityManager());
        trProperties.addProperties(props);
    }

    private void setTransportParameters(JMSTransportProperties trProperties, CommandLineParameters commandLineParams) {
        String transportProtocol = commandLineParams.getTransportProtocol();

        if (transportProtocol == null) {
            return;
        }

        Hashtable<String, String> props = new Hashtable<>();

        props.put(FioranoJNDIContext.TRANSPORT_PROTOCOL, transportProtocol);
        trProperties.addProperties(props);
    }

    private Configuration loadDefaultConfigurations(String componentRepoPath, String componentGUID,
                                                    String componentVersion, String componentInstanceName,
                                                    String specType, String specClassName, String appInstName)
            throws Exception {
        String fesbRaXMLFile = componentRepoPath + File.separator + componentGUID + File.separator + componentVersion
                + File.separator + "fesb-ra.xml";
        InputStream fesbRaIS = new FileInputStream(fesbRaXMLFile);

        FESBRAHandler rarFileHandler = new FESBRAHandler(Thread.currentThread().getContextClassLoader(), "BCLauncher",
                componentInstanceName, appInstName, componentGUID);
        rarFileHandler.setLoadMCF(true);

        SAXParserFactory parserFac = SAXParserFactory.newInstance();

        parserFac.setNamespaceAware(true);

        SAXParser parser = parserFac.newSAXParser();

        parser.parse(fesbRaIS, rarFileHandler);

        // initalize configuration
        JCAAdapterConfigurations adapterConfig = rarFileHandler.getConfig();

        // remove the interactionSpecs or activationSpecs other than the one specified.
        if (!StringUtil.isEmpty(specClassName)) {
            removeConfigObjects(adapterConfig, JMXUtil.TYPE_INTERACTION_SPEC, specClassName);
            removeConfigObjects(adapterConfig, JMXUtil.TYPE_ACTIVATION_SPEC, specClassName);
            //we don't need connection spec for JCA 1.5 semantics
            if (specType.equalsIgnoreCase(JMXUtil.TYPE_ACTIVATION_SPEC)) {
                removeConfigObjects(adapterConfig, JMXUtil.TYPE_CONNECTION_SPEC, specClassName);
            }
        }

        Configuration config = new Configuration();
        BCDKConfigurationInfo bcdkConfigInfo = new BCDKConfigurationInfo();

        bcdkConfigInfo.setJCAAdapterConfigObject(adapterConfig);
        config.setBCDKConfigurationInfo(bcdkConfigInfo);

        TrConfigurationInfo trConfigurationInfo = new TrConfigurationInfo();

        config.setTrConfigurationInfo(trConfigurationInfo);

        return config;
    }

    private void removeConfigObjects(JCAAdapterConfigurations adapterConfig, String specType, String specClassName) {
        List iSpecs = adapterConfig.getConfigurationObjects(specType);

        for (Object iSpec : iSpecs) {
            ConfigurationObject configObject = (ConfigurationObject) iSpec;

            if (configObject.getType().equalsIgnoreCase(specType) &&
                    !configObject.getImplClassName().getName().equalsIgnoreCase(specClassName)) {
                adapterConfig.removeConfigurationObject(configObject);
            }
        }
    }

    /**
     * Fetch all Configurations using CCP
     *
     * @param commandLineParams the command line arguments
     * @return the configuration of the component
     * @throws Exception if the configuration cannot be parsed
     */
    private Configuration fetchConfiguration(CommandLineParameters commandLineParams) throws Exception {

        //loading component configuration

        DataRequestEvent dataRequestEvent = new DataRequestEvent();

        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.PORT_CONFIGURATION);
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);
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

        MicroserviceConfiguration microserviceConfiguration = (MicroserviceConfiguration) data.get(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);

        try {
            if(microserviceConfiguration.getConfiguration()!= null && microserviceConfiguration.getConfiguration().trim().length() != 0){
                configuration.fromXML(microserviceConfiguration.getConfiguration());
            }else{
                configuration = loadDefaultConfigurations((String) commandLineParams.getParameter(CommandLineParameters.COMPONENT_REPO_PATH),
                        (String) commandLineParams.getParameter(
                                ESBConstants.COMPONENT_GUID),
                        (String) commandLineParams.getParameter(
                                ESBConstants.COMPONENT_VERSION),
                        commandLineParams.getServiceInstanceName(),
                        JMXUtil.TYPE_INTERACTION_SPEC,
                        (String) commandLineParams.getParameter(
                                JMXUtil.TYPE_INTERACTION_SPEC),
                        commandLineParams.getApplicationName());
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.LOADED_SUCCESSFULLY));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Not able to deserialize configuration");
            shutdown(e);
        }


        PortConfiguration portConfig = (PortConfiguration) data.get(DataRequestEvent.DataIdentifier.PORT_CONFIGURATION);
        Map<String, List<com.fiorano.openesb.application.application.PortInstance>> portInstances = portConfig.getPortInstances();


        //get the input port
        List<com.fiorano.openesb.application.application.PortInstance> inports = portInstances.get("IN_PORTS");
        if (inports != null && inports.size() != 0) {


            InputPortInstance inputPort = new InputPortInstance();
            Util.copy((com.fiorano.openesb.application.application.InputPortInstance) inports.get(0), inputPort);

            handleInPort(inputPort, configuration, commandLineParams);

        }

        boolean isSchedulingenabled = isSchedulingEnabled(configuration);
        configuration.getTrConfigurationInfo().setUseInputTRProperties(!isSchedulingenabled);
        configuration.getTrConfigurationInfo().setShareInputTRConnection(!isSchedulingenabled);
        configuration.getTrConfigurationInfo().setShareInputTRSession(!isSchedulingenabled);

        //clone inTrProps and set it to OutPort and errorPort
        // no need to do in case of scheduleing as there will be no input port
        if (!isSchedulingenabled) {
            configuration.getTrConfigurationInfo().setOutTransportProperties(
                    (TransportProperties) ((JMSTransportProperties) configuration.getTrConfigurationInfo().getInTransportProperties()).clone());
        }

        List<com.fiorano.openesb.application.application.PortInstance> outports = portInstances.get("OUT_PORTS");
        if (outports != null && outports.size() != 0) {

            com.fiorano.openesb.application.application.PortInstance output = null;

            for (com.fiorano.openesb.application.application.PortInstance port : outports) {
                if (!"ON_EXCEPTION".equalsIgnoreCase(port.getName())) {
                    output = port;
                    break;
                }
            }

            if (output != null) {
                OutputPortInstance outputPort = new OutputPortInstance();
                Util.copy((com.fiorano.openesb.application.application.OutputPortInstance) output, outputPort);

                handleOutPort(outputPort, configuration, commandLineParams);

            }
        }

        //set the error destination
        if (isSchedulingenabled) {
            configuration.getTrConfigurationInfo().setErrorTransportProperties(
                    (TransportProperties) ((JMSTransportProperties) configuration.getTrConfigurationInfo().getOutTransportProperties()).clone());
        } else {
            configuration.getTrConfigurationInfo().setErrorTransportProperties(
                    (TransportProperties) ((JMSTransportProperties) configuration.getTrConfigurationInfo().getInTransportProperties()).clone());
        }

        ((JMSTransportProperties) configuration.getTrConfigurationInfo().getErrorTransportProperties()).setDestinationName(commandLineParams.getApplicationName() + "__" + String.valueOf(commandLineParams.getApplicationVersion()).replace(".","_") + "__" + commandLineParams.getServiceInstanceName() + "__ON_EXCEPTION");
        ((JMSTransportProperties) configuration.getTrConfigurationInfo().getErrorTransportProperties()).setDestinationType(PortInstConstants.JMSDESTINATION_TOPIC);

        ManageableProperties manageablePropertiesData = (ManageableProperties) data.get(DataRequestEvent.DataIdentifier.MANAGEABLE_PROPERTIES);
        if(manageablePropertiesData != null && manageablePropertiesData.getManageableProperties() != null){
            managedProperties = new HashMap<>();
            if(manageablePropertiesData.getManageableProperties() != null){
                for(Map.Entry<String, com.fiorano.openesb.microservice.ccp.event.common.data.ConfigurationProperty> entry : manageablePropertiesData.getManageableProperties().entrySet()){
                    ConfigurationProperty cf = new ConfigurationProperty();
                    com.fiorano.openesb.microservice.ccp.event.common.data.ConfigurationProperty c = entry.getValue();
                    cf.setConfigurationType(c.getConfigurationType());
                    cf.setEncrypted(c.isEncrypted());
                    cf.setType(c.getType());
                    cf.setValue(c.getValue());
                }
            }

        }

        com.fiorano.openesb.microservice.ccp.event.common.data.NamedConfiguration namedConfigData
                = (com.fiorano.openesb.microservice.ccp.event.common.data.NamedConfiguration)
                data.get(DataRequestEvent.DataIdentifier.NAMED_CONFIGURATION);
        namedConfigurations = namedConfigData.getNamedConfigurations();
        return configuration;
    }


    private boolean isSchedulingEnabled(Configuration configuration) {
        return configuration.getSchedulerConfigurationInfo() != null
                && configuration.getSchedulerConfigurationInfo().isSchedulingEnabled();
    }

    private void handleInPort(InputPortInstance inportInst, Configuration configuration,
                              CommandLineParameters commandLineParams) {
        JMSTransportProperties trProperties = (JMSTransportProperties)
                configuration.getTrConfigurationInfo().getInTransportProperties();

        handleCommonTrProps(inportInst, trProperties, commandLineParams);

        trProperties.setProviderUrl(commandLineParams.getURL());
        if(cmdLineArgs.getParameter("-icf") == null){
            trProperties.setInitialContextFactory(INITIAL_CONTEXT_FACTORY);
        }else{
            trProperties.setInitialContextFactory((String)cmdLineArgs.getParameter("-icf"));
        }
        trProperties.setConnectionFactory(commandLineParams.getConnectionFactory());
        trProperties.setClientID(commandLineParams.getApplicationName() + "__" + String.valueOf(commandLineParams.getApplicationVersion()).replace(".","_") + "__" + commandLineParams.getServiceInstanceName() + "__In");
        trProperties.setTransacted(false);
        trProperties.setBackupProviderURLs(commandLineParams.getBackupURL());

        trProperties.setMaxSessions(String.valueOf(inportInst.getSessionCount()));
        trProperties.setRequestReplyEnabled(inportInst.isRequestReply());
        trProperties.setTransacted(inportInst.isTransacted());
        trProperties.setTransactionSize(inportInst.getTransactionSize());
        trProperties.setAcknowledgeMode(inportInst.getAcknowledgementMode());
        trProperties.setMessageSelector(inportInst.getMessageSelector());
        trProperties.setSubscriptionDurable(inportInst.isDurableSubscription());
        trProperties.setSubscriptionName(inportInst.getSubscriptionName());

    }

    private void handleOutPort(OutputPortInstance outportInst, Configuration configuration,
                               CommandLineParameters commandLineParams) {
        JMSTransportProperties trProperties = (JMSTransportProperties)
                configuration.getTrConfigurationInfo().getOutTransportProperties();

        handleCommonTrProps(outportInst, trProperties, commandLineParams);

        trProperties.setProviderUrl(commandLineParams.getURL());
        if(cmdLineArgs.getParameter("-icf") == null){
            trProperties.setInitialContextFactory(INITIAL_CONTEXT_FACTORY);
        }else{
            trProperties.setInitialContextFactory((String)cmdLineArgs.getParameter("-icf"));
        }
        trProperties.setConnectionFactory(commandLineParams.getConnectionFactory());
        trProperties.setClientID(commandLineParams.getApplicationName() + "__" + String.valueOf(commandLineParams.getApplicationVersion()).replace(".","_") + "__" + commandLineParams.getServiceInstanceName() + "__Out");

        trProperties.setMessagePriority(outportInst.getPriority());
        trProperties.setTtl(outportInst.getTimeToLive());
        trProperties.setEnableMsgPersistency(outportInst.isPersistent());
        trProperties.setEnableCompression(outportInst.isCompressMessages());
        trProperties.setServiceInstanceName(commandLineParams.getServiceInstanceName());
        trProperties.setEventProcessName(commandLineParams.getApplicationName());
        trProperties.setEventProcessVersion(String.valueOf(commandLineParams.getApplicationVersion()));
    }

    private void handleCommonTrProps(PortInstance portInst, JMSTransportProperties trProperties,
                                     CommandLineParameters commandLineParams) {
        trProperties.setUserName(commandLineParams.getUsername());
        trProperties.setJndiUserName(commandLineParams.getUsername());

        trProperties.setPassword(commandLineParams.getPassword());
        trProperties.setJndiPassword(commandLineParams.getPassword());
        if(cmdLineArgs.getParameter("-icf") == null){
            trProperties.setInitialContextFactory(INITIAL_CONTEXT_FACTORY);
        }else{
            trProperties.setInitialContextFactory((String)cmdLineArgs.getParameter("-icf"));
        }

        if (portInst.isSpecifiedDestinationUsed() && portInst.getDestination() != null) {
            trProperties.setDestinationType(portInst.getDestinationType() == PortInstance.DESTINATION_TYPE_QUEUE
                    ? PortInstConstants.JMSDESTINATION_QUEUE
                    : PortInstConstants.JMSDESTINATION_TOPIC);
            trProperties.setDestinationName(portInst.getDestination());
        }else{
            trProperties.setDestinationType(portInst.getDestinationType() == PortInstance.DESTINATION_TYPE_QUEUE
                    ? PortInstConstants.JMSDESTINATION_QUEUE
                    : PortInstConstants.JMSDESTINATION_TOPIC);
            trProperties.setDestinationName(cmdLineArgs.getApplicationName() + "__" + String.valueOf(commandLineParams.getApplicationVersion()).replace(".","_") + "__" + commandLineParams.getServiceInstanceName() + "__" + portInst.getName());
        }

        setSecurityParameters(trProperties, commandLineParams);
        setTransportParameters(trProperties, commandLineParams);
        trProperties.setMessageFilters(portInst.getMessageFilters());
    }

    public boolean isInMemory() {
        if(cmdLineArgs != null){
            return cmdLineArgs.isInmemoryLaunchable();
        }
        return false;
    }

    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }

    public CommandLineParameters getCommandLineParams() {
        return cmdLineArgs;
    }

    public Connection getConnection() {
        return connection;
    }

    public int waitFor() throws InterruptedException {
        if (!cmdLineArgs.isCCPEnabled()) {
            return 0;
        }
        if (started) {
            synchronized (lock) {
                lock.wait();
            }
        }
        return exitValue;
    }

    public int exitValue() {
        if (!cmdLineArgs.isCCPEnabled() || trGateway == null) {
            return 0;
        }
        if (!(trGateway.getServiceState().getState() == ServiceState.State.STOPPED || trGateway.getServiceState().getState() == ServiceState.State.SCHEDULER_STOPPED)) {
            throw new IllegalThreadStateException();
        } else {
            return exitValue;
        }
    }

    private void createCCPObjects() throws Exception {
        if (!cmdLineArgs.isCCPEnabled()) {
            return;
        }
        ccpEventManager = new CCPEventManager(this);
        ccpEventManager.start();
        getConnection().start();

    }

    /**
     * Returns the current state of the service.
     *
     * @return ServiceState - state of the service
     * @see com.fiorano.esb.common.service.ServiceState
     */
    public ServiceState getServiceState() {
        return serviceState;
    }

    public Logger getLogger() {
        return logger;
    }

    public void updateConfiguration(Map<DataRequestEvent.DataIdentifier, Data> data){
        this.data.putAll(data);
    }

    public void clearOutLogs() {
        logManager.clearOutLogs(cmdLineArgs.getApplicationName() + "__"
                + String.valueOf(cmdLineArgs.getApplicationVersion()) + "__"
                + cmdLineArgs.getServiceInstanceName(), getLogger());
    }

    public void clearErrLogs() {
        logManager.clearErrLogs(cmdLineArgs.getApplicationName() + "__"
                + String.valueOf(cmdLineArgs.getApplicationVersion()) + "__"
                + cmdLineArgs.getServiceInstanceName(), getLogger());
    }
}
