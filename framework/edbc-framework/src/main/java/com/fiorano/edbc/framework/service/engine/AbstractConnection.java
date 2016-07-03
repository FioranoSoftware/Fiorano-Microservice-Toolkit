/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.engine;

import com.fiorano.edbc.framework.service.configuration.AbstractConnectionConfiguration;
import com.fiorano.edbc.framework.service.connection.ConnectionEvent;
import com.fiorano.edbc.framework.service.connection.IConnection;
import com.fiorano.edbc.framework.service.connection.IConnectionEventListener;
import com.fiorano.edbc.framework.service.connection.State;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>AbstractConnection</code> holds actual physical EIS connection. This is used
 * to create connection, chek validity of connection, close the connection and holdss connection
 * meta data
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractConnection<CC extends AbstractConnectionConfiguration, CO> implements IConnection<CC, CO> {

    private CO connection;
    private CC connectionConfiguration;
    private List<IConnectionEventListener> listeners = new ArrayList<IConnectionEventListener>();


    /**
     * Creates <code>AbstractConnection</code> with the given metadata <code>connectionConfiguration</code>.
     *
     * @param connectionConfiguration meta data for creating connection
     */
    protected AbstractConnection(CC connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    /**
     * Returns whether connection is valid or not.
     *
     * @return <code>true</code> if connection is valid, <code>false</code> otherwise
     */
    public boolean isValid() {
        return connection != null;
    }

    /**
     * Close / destroy physical connection
     *
     * @throws com.fiorano.edbc.framework.service.exception.ServiceExecutionException exception that might occur while trying to close a connection
     */
    public void destroy() throws ServiceExecutionException {
        connection = null;
    }

    /**
     * Returns physical connection created using {@link #getConnectionConfiguration()}
     *
     * @return physical connection
     */
    public CO getConnection() {
        return connection;
    }

    /**
     * Sets physical connection
     *
     * @param connection physical connection to EIS
     */
    public void setConnection(CO connection) {
        this.connection = connection;
    }

    /**
     * Returns configuration details (metadata) for creating connection
     *
     * @return metadata for cretaing connection
     */
    public CC getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * Sets configuration details (metadata) for creating connection
     *
     * @param connectionConfiguration metadata for cretaing connection
     */
    public void setConnectionConfiguration(CC connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    public synchronized void addListener(IConnectionEventListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(IConnectionEventListener listener) {
        listeners.remove(listener);
    }

    protected void fireConnectionCreated() {
        fireConnectionStateChanged(new ConnectionEvent(this, State.CREATED));
    }

    protected void fireConnectionClosed() {
        fireConnectionStateChanged(new ConnectionEvent(this, State.CLOSED));
    }

    public void fireConnectionError() {
        fireConnectionStateChanged(new ConnectionEvent(this, State.ERROR));
    }

    protected void fireConnectionValidated(boolean valid) {
        fireConnectionStateChanged(new ConnectionEvent(this, valid ? State.VALIDATED_VAID : State.VALIDATED_INVALID));
    }

    protected void fireConnectionStateChanged(ConnectionEvent event) {
        for (IConnectionEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
