/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener.configuration;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.services.common.annotations.NamedConfiguration;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.exceptionlistener.Bundle;
import com.fiorano.services.exceptionlistener.Constants;
import com.fiorano.services.libraries.jms.configuration.ConnectionConfiguration;
import com.fiorano.services.libraries.jms.configuration.JNDIConfiguration;
import com.fiorano.util.ErrorListener;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Property model class for Exception Listener.
 *
 * @fiorano.xmbean
 * @jmx.mbean
 * @jboss.xmbean
 */
public class ExceptionListenerConfiguration extends ConnectionlessServiceConfiguration {

    private Hashtable<String, String> topicRegex;
    private Integer timeSliceForPing = 30000;
    private boolean useDurableSubscriptions;
    private String jmsProvider = Constants.ACTIVE_MQ;
    private String serverJMXUrl = "service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root";
    private String connectorHost = "localhost";
    private String connectorPort = "1858";
    private String jmxRemoteUsername = "karaf";
    private String jmxRemotePassword = "karaf";
    private ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration();
    private JNDIConfiguration jndiConfiguration = new JNDIConfiguration();

    public ExceptionListenerConfiguration() {
        for (Object o : errorHandlingConfiguration.getErrorActionsMap().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Set actions = (Set) entry.getValue();
            boolean isJMSError = ServiceErrorID.TRANSPORT_ERROR.equals(entry.getKey());
            for (Object action : actions) {
                ErrorHandlingAction errorHandlingAction = (ErrorHandlingAction) action;
                if (errorHandlingAction.getId() == ErrorHandlingAction.LOG || errorHandlingAction.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                    errorHandlingAction.setEnabled(true);
                }
                if (isJMSError && errorHandlingAction.getId() == ErrorHandlingAction.STOP_SERVICE) {
                    errorHandlingAction.setEnabled(true);
                }
            }
        }
        if (topicRegex == null) {
            topicRegex = new Hashtable(5);
            topicRegex.put("[A-Za-z0-9_-]*ON_EXCEPTION", Boolean.TRUE.toString());
        }
        useDurableSubscriptions = false;
        jndiConfiguration.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        connectionConfiguration.getCfConfiguration().setJndiConfiguration(jndiConfiguration);
        connectionConfiguration.setUsername("karaf");
        connectionConfiguration.setPassword("karaf");
        connectionConfiguration.getCfConfiguration().setCfLookupName("ConnectionFactory");
        connectionConfiguration.getCfConfiguration().setUrl("tcp://localhost:61616");
    }

    /**
     * @jmx.managed-attribute access="read-write" description="connection_config_desc"
     * @jmx.descriptor name="displayName" value="connection_config_name"
     * @jmx.descriptor name="index" value="6"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.jmslib.cps.swt.editor.ConnectionConfigEditor"
     * @return connectionConfiguration
     */
    @NamedConfiguration
    public ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setConnectionConfiguration(ConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="jndi_config_desc"
     * @jmx.descriptor name="displayName" value="jndi_config_name"
     * @jmx.descriptor name="index" value="7"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.jmslib.cps.swt.editor.JNDIConfigEditor"
     * @return jndiConfiguration
     */
    @NamedConfiguration
    public JNDIConfiguration getJndiConfiguration() {
        return jndiConfiguration;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setJndiConfiguration(JNDIConfiguration jndiConfig) {
        this.jndiConfiguration = jndiConfig;
        connectionConfiguration.getCfConfiguration().setJndiConfiguration(jndiConfig);
    }

    /**
     * @jmx.managed-attribute access="read-write" description="jms_provider_desc"
     * @jmx.descriptor name="displayName" value="jms_provider_name"
     * @jmx.descriptor name="legalValues" value="Fiorano MQ,Active MQ"
     * @jmx.descriptor name="defaultValue" value="Active MQ"
     * @jmx.descriptor name="hidesProperties" value="true"
     * @jmx.descriptor name="index" value="0"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public String getJmsProvider() {
        return jmsProvider;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setJmsProvider(String provider) {
        jmsProvider = provider;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="server_jmx_url_desc"
     * @jmx.descriptor name="displayName" value="server_jmx_url_name"
     * @jmx.descriptor name="index" value="1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public String getServerJMXUrl() {
        return serverJMXUrl;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setServerJMXUrl(String serverJMXUrl) {
        this.serverJMXUrl = serverJMXUrl;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="jmx_host_desc"
     * @jmx.descriptor name="displayName" value="jmx_host_name"
     * @jmx.descriptor name="index" value="1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public String getConnectorHost() {
        return connectorHost;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setConnectorHost(String connectorHost) {
        this.connectorHost = connectorHost;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="jmx_port_desc"
     * @jmx.descriptor name="displayName" value="jmx_port_name"
     * @jmx.descriptor name="index" value="1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public String getConnectorPort() {
        return connectorPort;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setConnectorPort(String connectorPort) {
        this.connectorPort = connectorPort;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="jmx_username_desc"
     * @jmx.descriptor name="displayName" value="jmx_username_name"
     * @jmx.descriptor name="defaultValue" value="admin"
     * @jmx.descriptor name="index" value="2"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public String getJmxRemoteUsername() {
        return jmxRemoteUsername;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="jmx_password_desc"
     * @jmx.descriptor name="displayName" value="jmx_password_name"
     * @jmx.descriptor name="password" value="true"
     * @jmx.descriptor name="defaultValue" value="passwd"
     * @jmx.descriptor name="index" value="3"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public String getJmxRemotePassword() {
        return jmxRemotePassword;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="topic_regex_desc"
     * @jmx.descriptor name="displayName" value="topic_regex_name"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.adapter.jca.editors.HashtablePropertyEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.exceptionlistener.cps.editors.ExceptionListenerHashtableEditor"
     * @jmx.descriptor name="canEditAsText" value="false"
     * @jmx.descriptor name="index" value="11"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public Hashtable<String, String> getTopicRegex() {
        return topicRegex;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="time_slice_for_ping_desc"
     * @jmx.descriptor name="displayName" value="time_slice_for_ping_name"
     * @jmx.descriptor name="defaultValue" value="10000"
     * @jmx.descriptor name="index" value="12"
     * @jmx.descriptor name="unit" value=" milli seconds"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public Integer getTimeSliceForPing() {
        return timeSliceForPing;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="use_durable_subscriptions_desc"
     * @jmx.descriptor name="displayName" value="use_durable_subscriptions_name"
     * @jmx.descriptor name="primitive" value="false"
     * @jmx.descriptor name="index" value="15"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.exception.Bundle"
     */
    public boolean isUseDurableSubscriptions() {
        return useDurableSubscriptions;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setTimeSliceForPing(Integer val) {
        timeSliceForPing = val;
    }


    /**
     * @jmx.managed-attribute
     */
    public void setJmxRemoteUsername(String val) {
        jmxRemoteUsername = val;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setJmxRemotePassword(String val) {
        jmxRemotePassword = val;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setTopicRegex(Hashtable<String, String> val) {
        topicRegex = val;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setUseDurableSubscriptions(boolean useDurableSubscriptions) {
        this.useDurableSubscriptions = useDurableSubscriptions;
    }

    @Override
    public void test() throws ServiceException {
    }

    @Override
    public String getAsFormattedString() {
        return null;
    }


    @Override
    public List fetchHiddenProperties() {
        List list = super.fetchHiddenProperties();
        if (Constants.ACTIVE_MQ.equalsIgnoreCase(jmsProvider)) {
            list.add("ConnectorHost");
            list.add("ConnectorPort");
        }
        return list;
    }

    public void validate(ErrorListener listener) throws ServiceConfigurationException {
        super.validate(listener);
        validatePatterns(topicRegex);

    }

    private void validatePatterns(Hashtable patterns) throws ServiceConfigurationException {
        for (Object o : patterns.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            if (key == null || key.trim().length() == 0) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.NAME_EMPTY), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
            if (!"true".equalsIgnoreCase((String) entry.getValue()) && !"false".equalsIgnoreCase((String) entry.getValue())) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.TRUE_OR_FALSE, new Object[]{entry.getKey()}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }
    }

}
