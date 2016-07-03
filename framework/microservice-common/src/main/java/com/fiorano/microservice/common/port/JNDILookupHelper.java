/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.port;

import com.fiorano.esb.wrapper.ILookupConfiguration;
import com.fiorano.util.StringUtil;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * JNDILookupHelper helper is a utility class which can be used to lookup ConnectionFactory, Destinations, Configuration of service and ports
 * during the runtime of the component
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class JNDILookupHelper {
    private final static String TRANSPORT_PROTOCOL = "TransportProtocol";
    private final static String SSL_SECURITY_MANAGER = "SecurityManager";
    private final static String BACKUP_URLS = "BackupConnectURLs";
    private Hashtable contextEnvironment;
    private InitialContext context;
    private boolean contextClosed = true;
    private boolean closeAfterEachOperation = false;
    private String connectionFactoryLookupName;

    /**
     * Populates all the parameters, required for creating environment for IntialContext, from the CommandLineParams object passed
     *
     * @param lookupConfiguration - command line parameters passed to the component when it is launched
     */
    public JNDILookupHelper(ILookupConfiguration lookupConfiguration) {
        Hashtable<String, String> props = new Hashtable<>();

        props.put(Context.SECURITY_PRINCIPAL, lookupConfiguration.getUsername());
        if (!StringUtil.isEmpty(lookupConfiguration.getBackupURL())) {
            props.put(BACKUP_URLS, lookupConfiguration.getBackupURL());
        }
        props.put(Context.SECURITY_CREDENTIALS, lookupConfiguration.getPassword());
        props.put(Context.PROVIDER_URL, lookupConfiguration.getURL());
        if(lookupConfiguration.getInitialContextFactory() == null){
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        }else{
            props.put(Context.INITIAL_CONTEXT_FACTORY, lookupConfiguration.getInitialContextFactory());
        }
        if (!StringUtil.isEmpty(lookupConfiguration.getSecurityProtocol())) {
            props.put(Context.SECURITY_PROTOCOL, lookupConfiguration.getSecurityProtocol());
        }
        if (!StringUtil.isEmpty(lookupConfiguration.getSecurityManager())) {
            props.put(SSL_SECURITY_MANAGER, lookupConfiguration.getSecurityManager());
        }
        if (!StringUtil.isEmpty(lookupConfiguration.getTransportProtocol())) {
            props.put(TRANSPORT_PROTOCOL, lookupConfiguration.getTransportProtocol());
        }
        Map additionalProps = lookupConfiguration.getAdditionalEnvProperties();
        if (additionalProps != null) {
            props.putAll(additionalProps);
        }
        contextEnvironment = props;
        connectionFactoryLookupName = lookupConfiguration.getConnectionFactory();
    }

    /**
     * Lazily creates and returns the InitialContext. Environment for the context created is held in memory, so after performing the required lookup
     * the InitialContext's resources can be released by calling close on this. Alternatively, InitialContext can be opened and closed for each
     * request by setting closeAfterEachOperation to true
     *
     * @return InitialContext created using the environment details provided from CommandLineParams
     * @throws NamingException any exception which is occured when InitialContext is being created
     * @see #close()
     */
    private InitialContext getInitialContext() throws NamingException {
        if (contextClosed) {
            context = new InitialContext(contextEnvironment);
            contextClosed = false;
        }
        return context;
    }

    /**
     * Looks up and returns the jms ConnectionFactory which can be used to create JMS objects. Lookup using
     * {@link fiorano.esb.util.CommandLineParams#getConnFactory()}
     *
     * @return looked up ConnectionFactory
     * @throws NamingException exception if lookup fails
     */
    public ConnectionFactory lookupConnectionFactory() throws NamingException {
        Object connectionFactory = null;
        try {
            connectionFactory = getInitialContext().lookup(connectionFactoryLookupName);
        } finally {
            if (closeAfterEachOperation) {
                close();
            }
        }
        return connectionFactory instanceof ConnectionFactory ? (ConnectionFactory) connectionFactory : null;
    }

    /**
     * Looks up and returns an object bound to JNDI
     *
     * @return looked up object
     * @throws NamingException exception if lookup fails
     */
    public Object lookup(String name) throws NamingException {
        try {
            return getInitialContext().lookup(name);
        } finally {
            if (closeAfterEachOperation) {
                close();
            }
        }
    }

    /**
     * Close InitialContext and release the resources
     *
     * @throws NamingException if InitialContext could not be closed
     */
    public void close() throws NamingException {
        try {
            if (context != null) {
                context.close();
            }
        } finally {
            context = null;
            contextClosed = true;
        }
    }

}
