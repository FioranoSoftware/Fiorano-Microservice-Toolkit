/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.connection;

import com.fiorano.edbc.framework.service.engine.AbstractConnection;

import java.util.EventObject;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ConnectionEvent extends EventObject {
    private State state;

    /**
     * Constructs a prototypical Event.
     *
     * @param connection The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ConnectionEvent(AbstractConnection connection, State state) {
        super(connection);
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
