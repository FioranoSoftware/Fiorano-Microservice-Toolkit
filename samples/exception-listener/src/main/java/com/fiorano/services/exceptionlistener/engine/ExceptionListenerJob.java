/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener.engine;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.Service;
import com.fiorano.services.exceptionlistener.Bundle;
import com.fiorano.services.exceptionlistener.Constants;
import com.fiorano.services.exceptionlistener.configuration.ExceptionListenerConfiguration;
import com.fiorano.services.libraries.jms.configuration.ConnectionConfiguration;
import com.fiorano.services.libraries.jms.configuration.ConsumerConfiguration;
import com.fiorano.services.libraries.jms.configuration.DestinationConfiguration;
import com.fiorano.services.libraries.jms.helper.ActiveMQHelper;
import com.fiorano.services.libraries.jms.helper.FioranoMQHelper;
import com.fiorano.services.libraries.jms.helper.MQHelper;
import fiorano.esb.utils.RBUtil;
import fiorano.jms.common.FioranoException;
import fiorano.jms.md.JMSMetaData;
import fiorano.jms.runtime.admin.MQAdminService;
import fiorano.jms.services.IFioranoConstants;
import fiorano.jms.services.admin.IMQAdminService;
import org.apache.activemq.broker.jmx.BrokerViewMBean;

import javax.jms.*;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 17 Dec, 2010
 * Time: 4:16:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExceptionListenerJob {

    private Hashtable<String, MQAdminService> adminServices = new Hashtable<String, MQAdminService>();
    private HashSet<Connection> topicConnections = new HashSet<>();
//    private List mqAdminConnList = new ArrayList();

    private Logger logger;
    private String hostURL = null;
    private ExceptionListenerEngine exceptionListenerEngine;
//    private CommandLineParameters commandLineParameters;

    //    private Hashtable<String, MessageReceiver> peerMessageListenerMap = new Hashtable<String, MessageReceiver>();
    private ExceptionListenerConfiguration configuration = null;
    private EventPortPolling poll;
    //    private FioranoServiceProvider serviceProvider = null;
//    private boolean currentlyConnected = false;
//    private final Hashtable<String, Vector<String>> peerTopicsMap = new Hashtable<String, Vector<String>>();
    private String errorDest = null;
    private String outportTopicName;
    private HashSet<String> topicsSet = new HashSet<>();
    //    private String nodeNameActiveMq = "FioranoOpenESB";
    private MQHelper mqHelper;


    public ExceptionListenerJob(ExceptionListenerEngine exceptionListenerEngine) {

        this.configuration = (ExceptionListenerConfiguration) exceptionListenerEngine.getConfiguration();
        this.exceptionListenerEngine = exceptionListenerEngine;
        this.logger = exceptionListenerEngine.getLogger();
        if (Constants.ACTIVE_MQ.equalsIgnoreCase(configuration.getJmsProvider())) {
            mqHelper = new ActiveMQHelper(logger);
        } else {
            mqHelper = new FioranoMQHelper(logger);
        }
        errorDest = exceptionListenerEngine.getTransportManager().getErrorTransport().getConfiguration().getDestinationConfiguration().getName();
        hostURL = ((Service) exceptionListenerEngine.getParent()).getLaunchConfiguration().getURL();
        outportTopicName = exceptionListenerEngine.getTransportManager().getOutTransport().getConfiguration().getDestinationConfiguration().getName();
    }

    public void init() throws ServiceExecutionException {
        if (Constants.ACTIVE_MQ.equalsIgnoreCase(configuration.getJmsProvider())) {
            subscribeToActiveMqTopics();
        } else {
            subscribeToFMQTopics();
        }
        poll = new EventPortPolling(this);
        poll.start();
    }

    public void subscribeToFMQTopics() throws ServiceExecutionException {
        try {
            JMXServiceURL address = new JMXServiceURL(configuration.getServerJMXUrl());
            Map<String, Object> environment = new HashMap<>();
            environment.put(Context.PROVIDER_URL, "rmi://" + configuration.getConnectorHost() + ":" + configuration.getConnectorPort());
            String[] credentials = new String[]{configuration.getJmxRemoteUsername(), configuration.getJmxRemotePassword()};
            environment.put("jmx.remote.credentials", credentials);
            JMXConnector connector = JMXConnectorFactory.newJMXConnector(address, environment);
            connector.connect(environment);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            ObjectName adminService = new ObjectName("Fiorano.etc:ServiceType=AdminService,Name=AdminService");
            IMQAdminService mqAdminService = MBeanServerInvocationHandler.newProxyInstance(connection, adminService, IMQAdminService.class, true);
            Enumeration topics = mqAdminService.elements(IFioranoConstants.NAMED_TOPIC);
            while (topics.hasMoreElements()) {
                JMSMetaData metaData = (JMSMetaData) topics.nextElement();
                String topicName = metaData.getName();if (outportTopicName.equalsIgnoreCase(topicName) || errorDest.equalsIgnoreCase(topicName) || topicsSet.contains(topicName)) {
                    continue;
                }
                if (NameMatcher.matches(topicName, configuration.getTopicRegex())) {
                    MessageReceiver messageListener = new MessageReceiver(exceptionListenerEngine, topicsSet);
                    topicsSet.add(topicName);
                    subscribeToTopic(topicName, messageListener);
                }
            }

        } catch (IOException | FioranoException | MalformedObjectNameException e) {
            throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }

    public void subscribeToActiveMqTopics() throws ServiceExecutionException {
        try {
            Map<String, String[]> env = new HashMap<>();
            String[] credentials = new String[]{configuration.getJmxRemoteUsername(), configuration.getJmxRemotePassword()};
            env.put(JMXConnector.CREDENTIALS, credentials);
            String providerurl = configuration.getServerJMXUrl();
            JMXConnector connector = JMXConnectorFactory.newJMXConnector(new JMXServiceURL(providerurl), env);
            connector.connect();
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=amq-broker");
            BrokerViewMBean adminMBean = MBeanServerInvocationHandler.newProxyInstance(connection, activeMQ, BrokerViewMBean.class, true);

            ObjectName[] topics = adminMBean.getTopics();

            for (ObjectName topic : topics) {
                String topicName = topic.getKeyProperty("destinationName");
                if (outportTopicName.equalsIgnoreCase(topicName) || errorDest.equalsIgnoreCase(topicName) || topicsSet.contains(topicName)) {
                    continue;
                }
                if (NameMatcher.matches(topicName, configuration.getTopicRegex())) {
                    MessageReceiver messageListener = new MessageReceiver(exceptionListenerEngine, topicsSet);
                    topicsSet.add(topicName);
                    subscribeToTopic(topicName, messageListener);
                }

            }
        } catch (IOException | MalformedObjectNameException e) {
            throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }

    private void subscribeToTopic(String topicName, MessageReceiver messageListener) throws ServiceExecutionException {
        try {
            ConnectionConfiguration connectionConfiguration = configuration.getConnectionConfiguration();
            mqHelper.initialize(connectionConfiguration.getCfConfiguration());
            connectionConfiguration.setUnifiedDomainSupported(true);
            connectionConfiguration.setClientIDDefined(false);
            ConnectionFactory cf = mqHelper.fetchConnectionFactory(connectionConfiguration);
            Connection con = cf.createConnection(connectionConfiguration.getUsername(), connectionConfiguration.getPassword());
            con.start();
            Session session = con.createSession(true, 1);
            DestinationConfiguration configuration = new DestinationConfiguration();
            configuration.setDestinationType(DestinationConfiguration.DestinationType.TOPIC.toString());
            configuration.setName(topicName);
            Destination destination = mqHelper.fetchDestination(session, configuration);
            MessageConsumer consumer = mqHelper.createConsumer(session, destination, new ConsumerConfiguration());
            consumer.setMessageListener(messageListener);
            con.setExceptionListener(messageListener);
            topicConnections.add(con);
        } catch (Exception e) {
            throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public ExceptionListenerConfiguration getModel() {
        return configuration;
    }


    public void closeConnections() {
        try {
            if (poll != null) {
                poll.setRunning(false);
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.EXCP_WHILE_CLOSING_CONNECTIONS, new Object[]{ex.getLocalizedMessage()}), ex);
        }

        for (Connection connection : topicConnections) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.EXCP_WHILE_CLOSING_CONNECTIONS, new Object[]{e.getLocalizedMessage()}), e);
            }

        }

    }


    /**
     * @param Message
     */
    public void sendException(String Message, Throwable e) {

        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.EXCEP_OCC, new Object[]{Message == null ? e.getLocalizedMessage() : Message}), e);
        }
        try {
            if (e instanceof ServiceExecutionException)
                exceptionListenerEngine.getTransportManager().getErrorTransport().sendError((ServiceExecutionException) e, null);
            else
                exceptionListenerEngine.getTransportManager().getErrorTransport().sendError(ServiceErrorID.REQUEST_EXECUTION_ERROR.getName(), Message, e, null);
        } catch (ServiceExecutionException e1) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_EXCEP_ERROR), e1);
        }
    }


}
