/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 10, 2010
 * Time: 1:04:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IModule {
    void create() throws ServiceExecutionException;

    void start() throws ServiceExecutionException;

    void stop() throws ServiceExecutionException;

    void destroy() throws ServiceExecutionException;

    String getName();

    State getState();

    Logger getLogger();

    void setLogger(Logger logger);

    List<? extends IModule> getChildren();

    void addChild(IModule child);

    IModule getParent();

    void addStateListener(StateListener stateListener);

    void removeStateListener(StateListener stateListener);

    enum State {
        CREATING,
        CREATED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        DESTROYING,
        DESTROYED,
        UNDEFINED
    }

    public static class StateChangeEvent {
        private Object source;
        private State oldState;
        private State newState;

        public StateChangeEvent(Object source, State oldState, State newState) {
            this.source = source;
            this.oldState = oldState;
            this.newState = newState;
        }

        public Object getSource() {
            return source;
        }

        public State getOldState() {
            return oldState;
        }

        public State getNewState() {
            return newState;
        }
    }

}
