/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.util.ESBConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 12, 2010
 * Time: 6:47:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Module implements IModule {
    protected IModule parent;
    protected Logger logger;
    protected LinkedList<IModule> children = new LinkedList<IModule>();
    private State state = State.DESTROYED;
    private ModuleObservable observable = new ModuleObservable();

    protected Module(IModule parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void create() throws ServiceExecutionException {
        if (!canGotoCreate()) {
            if (logger != null) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.MODULE_STATE_CHANGE_NOT_ALLOWED,
                        new String[]{getName(), State.CREATING.toString(), state.toString()}));
            }
            return;
        }
        changeState(State.CREATING);
        try {
            internalCreate();
        } catch (ServiceExecutionException e) {
            changeState(State.UNDEFINED);
            throw e;
        }
        changeState(State.CREATED);
    }

    protected void internalCreate() throws ServiceExecutionException {
        for (IModule child : children) {
            child.create();
        }
    }

    public void start() throws ServiceExecutionException {
        if (!canGotoStart()) {
            if (logger != null) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.MODULE_STATE_CHANGE_NOT_ALLOWED,
                        new String[]{getName(), State.STARTING.toString(), state.toString()}));
            }
            return;
        }
        changeState(State.STARTING);
        internalStart();
        changeState(State.STARTED);
    }

    protected void internalStart() throws ServiceExecutionException {
        for (IModule child : children) {
            child.start();
        }
    }

    public void stop() throws ServiceExecutionException {
        if (!canGotoStop()) {
            if (logger != null) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.MODULE_STATE_CHANGE_NOT_ALLOWED,
                        new String[]{getName(), State.STOPPING.toString(), state.toString()}));
            }
            return;
        }
        changeState(State.STOPPING);
        internalStop();
        changeState(State.STOPPED);
    }

    protected void internalStop() throws ServiceExecutionException {
        for (int i = children.size() - 1; i >= 0; --i) {
            children.get(i).stop();
        }
    }

    public void destroy() throws ServiceExecutionException {
        if (!canGotoDestroy()) {
            if (logger != null) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.MODULE_STATE_CHANGE_NOT_ALLOWED,
                        new String[]{getName(), State.DESTROYING.toString(), state.toString()}));
            }
            return;
        }
        changeState(State.DESTROYING);
        internalDestroy();
        changeState(State.DESTROYED);
        observable.deleteObservers();
    }

    protected void internalDestroy() throws ServiceExecutionException {
        while (!children.isEmpty()) {
            children.removeLast().destroy();
        }
    }

    public String getName() {
        return parent.getName() + ESBConstants.JNDI_CONSTANT + ClassUtil.getShortClassName(this.getClass());
    }

    public State getState() {
        return state;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public final List<? extends IModule> getChildren() {
        return children;
    }

    public IModule getParent() {
        return parent;
    }

    public final void addChild(IModule child) {
        children.add(child);
    }


    public void addStateListener(StateListener stateListener) {
        observable.addObserver(stateListener);
    }

    public void removeStateListener(StateListener stateListener) {
        observable.deleteObserver(stateListener);
    }

    protected final void changeState(State newState) {
        StateChangeEvent stateChangeEvent = new StateChangeEvent(this, this.state, this.state = newState);
        observable.setChanged();
        observable.notifyObservers(stateChangeEvent);
    }

    protected boolean canGotoCreate() {
        return State.DESTROYED.equals(state);
    }

    protected boolean canGotoStart() {
        return State.CREATED.equals(state) || State.STOPPED.equals(state);
    }

    protected boolean canGotoStop() {
        return State.STARTED.equals(state) || State.UNDEFINED.equals(state);
    }

    protected boolean canGotoDestroy() {
        return State.STOPPED.equals(state) || State.CREATED.equals(state);
    }

    static class ModuleObservable extends Observable {
        public void setChanged() {
            super.setChanged();
        }
    }

}
